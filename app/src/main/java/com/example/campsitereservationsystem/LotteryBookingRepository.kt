package com.example.campsitereservationsystem

import com.example.campsitereservationsystem.campsite.Campsite
import com.example.campsitereservationsystem.campsite.CampsiteRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

internal data class WonLotteryOpportunity(
    val id: String,
    val title: String,
    val bookingEnd: Timestamp?
)

internal data class LotteryBookingOption(
    val lottery: WonLotteryOpportunity,
    val campsite: Campsite
)

internal class LotteryBookingProblem(message: String) : Exception(message)

/* Adds remote booking without changing local demo controller. */
internal class LotteryBookingRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val lotteries = db.collection("lotteries")
    private val campsites = CampsiteRepository(db)

    suspend fun availableBookings(userID: String): List<LotteryBookingOption> {
        if (userID.isBlank()) return emptyList()
        val now = Timestamp.now()
        val usedReservationIDs = db.collection("reservations")
            .whereEqualTo("entrantID", userID).get().await().documents.map { it.id }.toSet()
        val documents = lotteries.get().await().documents
        val options = mutableListOf<LotteryBookingOption>()

        for (document in documents) {
            if (document.getString("status") != "COMPLETED" ||
                !document.hasUser("winners", userID) ||
                bookingPeriodProblem(document, now) != null ||
                reservationID(document.id, userID) in usedReservationIDs) continue

            val name = (document.get("campsite") as? Map<*, *>)?.get("name") as? String
            val lottery = WonLotteryOpportunity(
                id = document.id,
                title = name?.takeIf { it.isNotBlank() } ?: "Lottery ${document.id}",
                bookingEnd = document.getTimestamp("bookingEndTimestamp")
            )
            val campsite = campsites.getById(document.campsiteID()) ?: continue
            options.add(LotteryBookingOption(lottery, campsite))
        }
        return options.sortedWith(compareBy<LotteryBookingOption> {
            it.lottery.bookingEnd?.seconds ?: Long.MAX_VALUE
        }.thenBy { it.campsite.name })
    }

    suspend fun schedule(campsiteID: String): AvailabilitySchedule {
        return readSchedule(db.collection("availabilitySchedules").document(campsiteID).get().await())
    }

    suspend fun confirm(
        lottery: WonLotteryOpportunity,
        user: User,
        campsite: Campsite,
        checkIn: Int,
        checkOut: Int,
        acceptedRules: Boolean
    ): Reservation {
        if (user.id.isBlank() || !Permissions().isAllowed(user.roles, Action.RESERVE_CAMPSITE)) {
            throw LotteryBookingProblem("Sign in as an entrant to reserve a campsite.")
        }
        if (!acceptedRules) throw LotteryBookingProblem("Please accept the campsite rules.")

        val lotteryRef = lotteries.document(lottery.id)
        val campsiteRef = db.collection("campsites").document(campsite.id)
        val scheduleRef = db.collection("availabilitySchedules").document(campsite.id)
        val reservationRef = db.collection("reservations")
            .document(reservationID(lottery.id, user.id))

        return db.runTransaction { transaction ->
            val currentLottery = transaction.get(lotteryRef)
            if (currentLottery.getString("status") != "COMPLETED" ||
                !currentLottery.hasUser("winners", user.id)) {
                throw LotteryBookingProblem("This lottery does not have a winning opportunity for you.")
            }
            if (campsite.id != currentLottery.campsiteID()) {
                throw LotteryBookingProblem("This campsite does not belong to this lottery.")
            }

            val existing = transaction.get(reservationRef).toObject(Reservation::class.java)
            if (existing != null) {
                throw LotteryBookingProblem("This win has already been used. Go back and refresh the Booking list.")
            }
            checkBookingPeriod(currentLottery)

            val currentCampsite = transaction.get(campsiteRef)
            if (!currentCampsite.exists()) throw LotteryBookingProblem("This campsite is no longer available.")
            if (currentCampsite.getString("rules").orEmpty() != campsite.rules) {
                throw LotteryBookingProblem("The campsite rules changed. Choose the campsite again to review them.")
            }
            val availability = readSchedule(transaction.get(scheduleRef))
            val problem = lotteryDateProblem(availability, checkIn, checkOut)
            if (problem.isNotEmpty()) throw LotteryBookingProblem(problem)

            val nights = (checkIn until checkOut).toSet()
            val reservation = Reservation(
                reservationID = reservationRef.id,
                campsiteID = campsite.id,
                entrantID = user.id,
                startDate = "2027-05-${checkIn.toString().padStart(2, '0')}",
                endDate = "2027-05-${checkOut.toString().padStart(2, '0')}",
                status = ReserveStatus.CONFIRMED,
                rulesAcknowledged = true
            )

            transaction.update(scheduleRef, mapOf(
                "availableDays" to (availability.availableDays - nights).sorted(),
                "bookedDays" to (availability.bookedDays + nights).sorted()
            ))
            transaction.set(reservationRef, reservation)
            reservation
        }.await()
    }
}

/* Matches LotteryRepository, which stores a lottery under its campsite ID. */
private fun DocumentSnapshot.campsiteID(): String {
    return getString("campsiteID")?.takeIf { it.isNotBlank() } ?: id
}

private fun DocumentSnapshot.hasUser(field: String, userID: String): Boolean {
    return userID.isNotBlank() && (get(field) as? List<*>).orEmpty().any {
        (it as? Map<*, *>)?.get("id") == userID
    }
}

private fun reservationID(lotteryID: String, userID: String): String {
    return "LOTTERY-" + UUID.nameUUIDFromBytes("$lotteryID/$userID".toByteArray(Charsets.UTF_8))
}

private fun readSchedule(document: DocumentSnapshot): AvailabilitySchedule {
    if (!document.exists()) throw LotteryBookingProblem("The owner has not published availability for this campsite.")
    fun days(field: String): Set<Int> {
        val values = document.get(field) as? List<*>
            ?: throw LotteryBookingProblem("This campsite's availability could not be read.")
        return values.map {
            val number = it as? Number
                ?: throw LotteryBookingProblem("This campsite's availability could not be read.")
            val day = number.toInt()
            if (day !in 1..31 || number.toDouble() != day.toDouble()) {
                throw LotteryBookingProblem("This campsite's availability is outside the supported May 2027 calendar.")
            }
            day
        }.toSet()
    }
    val available = days("availableDays")
    val booked = days("bookedDays")
    if (available.any { it in booked }) {
        throw LotteryBookingProblem("The owner needs to correct this campsite's availability.")
    }
    return AvailabilitySchedule(availableDays = available, bookedDays = booked)
}

private fun bookingPeriodProblem(document: DocumentSnapshot, now: Timestamp): String? {
    val start = document.getTimestamp("bookingStartTimestamp")
    val end = document.getTimestamp("bookingEndTimestamp")
    if (start == null && end == null)
        return null
    if (start == null || end == null || end <= start) {
        return "The owner needs to complete the lottery's booking period."
    }
    if (now < start) return "Booking for this lottery has not opened yet."
    if (now >= end) return "The booking deadline has passed."
    return null
}

private fun checkBookingPeriod(document: DocumentSnapshot) {
    bookingPeriodProblem(document, Timestamp.now())?.let { throw LotteryBookingProblem(it) }
}

internal fun lotteryDateProblem(schedule: AvailabilitySchedule, checkIn: Int, checkOut: Int): String {
    if (checkIn !in 1..31 || checkOut !in 1..31 || checkOut <= checkIn) {
        return "Choose a check-in and a later check-out within May 2027."
    }
    return if ((checkIn until checkOut).all { night ->
            night in schedule.availableDays && night !in schedule.bookedDays
        }) "" else "Some selected nights are unavailable. Please choose another stay."
}
