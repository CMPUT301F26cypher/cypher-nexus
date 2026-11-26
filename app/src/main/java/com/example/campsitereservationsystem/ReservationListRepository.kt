/*
 * Firestore code for the Reservations screens
 * (Owner Frames 33–37 and Entrant Frames 61–65).
 * It only reads the collections the team already uses: reservations, campsites and users.
 */

package com.example.campsitereservationsystem

import com.example.campsitereservationsystem.OwnerCampsitesPath.Campsite
import com.example.campsitereservationsystem.OwnerCampsitesPath.CampsiteRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.UUID

/**
 * Loads, cancels and rejects reservations.
 *
 * @param db The Firestore database. Tests can pass a different one.
 */
class ReservationListRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val reservations = db.collection("reservations")
    private val users = db.collection("users")
    private val schedules = db.collection("availabilitySchedules")
    private val campsiteRepository = CampsiteRepository(db)

    /**
     * Owner side: every reservation made at one of this owner's campsites.
     * A campsite can be saved with the owner's user ID or device ID, so we check both.
     */
    suspend fun loadForOwner(owner: User): List<ReservationInfo> {
        val myCampsites = myCampsites(owner)
        val myCampsiteIds = myCampsites.map { it.id }

        val all = readReservations(reservations.get().await().documents)
        val mine = all.filter { it.campsiteID in myCampsiteIds }
        return addNames(mine, myCampsites)
    }

    /** Entrant side: every reservation this entrant made. */
    suspend fun loadForEntrant(entrant: User): List<ReservationInfo> {
        val documents = reservations
            .whereEqualTo("entrantID", entrant.id)
            .get()
            .await()
            .documents
        val mine = readReservations(documents)
        return addNames(mine, campsiteRepository.getAll())
    }

    /**
     * Frame 35 (owner Reject) and Frame 64 (entrant Cancel).
     * Marks the reservation as CANCELLED and gives its nights back to the campsite,
     * both in one transaction so they always happen together.
     */
    suspend fun cancel(reservation: Reservation) {
        val reservationRef = reservations.document(reservation.reservationID)
        val scheduleRef = schedules.document(reservation.campsiteID)

        db.runTransaction { transaction ->
            // Firestore needs all reads before any writes inside a transaction.
            val schedule = transaction.get(scheduleRef)
            val nights = nightsToGiveBack(reservation, schedule)

            if (nights.isNotEmpty()) {
                transaction.update(
                    scheduleRef,
                    "bookedDays", FieldValue.arrayRemove(*nights.toTypedArray()),
                    "availableDays", FieldValue.arrayUnion(*nights.toTypedArray())
                )
            }
            transaction.update(reservationRef, "status", ReserveStatus.CANCELLED.name)
        }.await()
    }

    /** Frame 35: tells the entrant that the owner rejected their reservation. */
    suspend fun tellEntrantAboutReject(owner: User, info: ReservationInfo) {
        val document = db.collection("notifications").document()
        val dates = ReservationLists.bookedDates(info.reservation)
        val notification = Notification(
            notifID = document.id,
            senderID = owner.id,
            receiverID = info.reservation.entrantID,
            message = "Your reservation at ${info.campsiteName} ($dates) was rejected by the campsite owner.",
            timestamp = System.currentTimeMillis()
        )
        document.set(notification).await()
    }

    /**
     * TEMPORARY : adds one active, one past and one
     * cancelled reservation at the owner's own campsites, so the owner lists
     * have something to show. The owner is used as the entrant.
     */
    suspend fun addTestReservationsForOwner(owner: User): String {
        val campsites = myCampsites(owner)
        if (campsites.isEmpty()) {
            return "Add a campsite in My Campsites first."
        }
        addTestReservations(owner, campsites)
        return "Added 3 test reservations."
    }

    /** TEMPORARY (delete before the demo): same as above, at any campsites, for the entrant. */
    suspend fun addTestReservationsForEntrant(entrant: User): String {
        val campsites = campsiteRepository.getAll()
        if (campsites.isEmpty()) {
            return "There are no campsites yet. An owner needs to add one first."
        }
        addTestReservations(entrant, campsites)
        return "Added 3 test reservations."
    }


    private suspend fun myCampsites(owner: User): List<Campsite> {
        return campsiteRepository.getAll().filter { campsite ->
            campsite.ownerId.isNotBlank() &&
                (campsite.ownerId == owner.id || campsite.ownerId == owner.deviceId)
        }
    }

    /** Turns Firestore documents into Reservation objects and skips any that cannot be read. */
    private fun readReservations(documents: List<DocumentSnapshot>): List<Reservation> {
        val result = mutableListOf<Reservation>()
        for (document in documents) {
            val reservation = try {
                document.toObject(Reservation::class.java)
            } catch (e: Exception) {
                null
            }
            if (reservation != null) {
                result.add(reservation.copy(reservationID = document.id))
            }
        }
        return result
    }

    /** Adds the campsite name, location and entrant name to each reservation. */
    private suspend fun addNames(
        list: List<Reservation>,
        campsites: List<Campsite>
    ): List<ReservationInfo> {
        val result = mutableListOf<ReservationInfo>()
        for (reservation in list) {
            val campsite = campsites.find { it.id == reservation.campsiteID }
            val info = ReservationInfo(
                reservation = reservation,
                campsiteName = campsite?.name ?: "Unknown campsite",
                location = campsite?.location ?: "",
                entrantName = entrantName(reservation.entrantID)
            )
            result.add(info)
        }
        return result
    }

    private suspend fun entrantName(entrantId: String): String {
        if (entrantId.isBlank()) {
            return "Unknown entrant"
        }
        val name = try {
            users.document(entrantId).get().await().getString("name")
        } catch (e: Exception) {
            null
        }
        if (name.isNullOrBlank()) {
            return "Unknown entrant"
        }
        return name
    }

    /** Only gives back nights that are really marked as booked in the campsite's schedule. */
    private fun nightsToGiveBack(reservation: Reservation, schedule: DocumentSnapshot): List<Int> {
        if (!schedule.exists()) {
            return emptyList()
        }
        val booked = (schedule.get("bookedDays") as? List<*>).orEmpty()
        val bookedNights = booked.mapNotNull { (it as? Number)?.toInt() }
        return ReservationLists.may2027Nights(reservation).filter { it in bookedNights }
    }

    private suspend fun addTestReservations(entrant: User, campsites: List<Campsite>) {
        val today = LocalDate.now()
        val first = campsites[0]
        val second = campsites.getOrElse(1) { first }
        val third = campsites.getOrElse(2) { first }

        saveTestReservation(first, entrant, today.plusDays(7), today.plusDays(9), ReserveStatus.CONFIRMED)
        saveTestReservation(second, entrant, today.minusDays(30), today.minusDays(28), ReserveStatus.CONFIRMED)
        saveTestReservation(third, entrant, today.plusDays(14), today.plusDays(16), ReserveStatus.CANCELLED)
    }

    private suspend fun saveTestReservation(
        campsite: Campsite,
        entrant: User,
        checkIn: LocalDate,
        checkOut: LocalDate,
        status: ReserveStatus
    ) {
        val id = "TEST-" + UUID.randomUUID()
        val reservation = Reservation(
            reservationID = id,
            campsiteID = campsite.id,
            entrantID = entrant.id,
            startDate = checkIn.toString(),
            endDate = checkOut.toString(),
            status = status,
            rulesAcknowledged = true
        )
        reservations.document(id).set(reservation).await()
    }
}
