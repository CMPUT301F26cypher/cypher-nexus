// Storing demo records in memory and loading Firestore schedules by campsite ID in remote mode.

package com.example.campsitereservationsystem

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Keeps the booking screen and availability calendar working with the same records.
 * Local records reset when the activity is recreated.
 *
 * @property localOnly True to keep records in memory, false to use Firestore.
 */
class ReservationRepository(val localOnly: Boolean = false) {
    // Local mode must not initialise Firebase just to open the demo screens.
    private val db = if (localOnly) null else Firebase.firestore
    private val reservationsRef = db?.collection("reservations")
    private val schedulesRef = db?.collection("availabilitySchedules")

    private val storedReservations = mutableStateListOf<Reservation>()
    private val storedSchedules = mutableStateMapOf<String, AvailabilitySchedule>()
    // Indexing the demo schedules by campsite ID so booking and owner availability can read the same calendar.
    private val localCalendars = mutableStateMapOf<String, AvailabilitySchedule>()

    val reservations: List<Reservation>
        get() = storedReservations

    val availabilitySchedules: List<AvailabilitySchedule>
        get() {
            if (localOnly) {
                return localCalendars.values.toList()
            }
            return storedSchedules.values.toList()
        }

    init {
        // Starting Firestore listeners only in the mode that is using remote storage.
        if (!localOnly) {
            watchReservations()
            watchSchedules()
        }
    }

    /** Keeps the team's existing Firestore reservation listener. */
    private fun watchReservations() {
        reservationsRef?.addSnapshotListener { snapshot, error ->
            if (error == null) {
                // Replacing the local view with the contents of the latest Firestore snapshot.
                storedReservations.clear()
                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        val reservation = document.toObject(Reservation::class.java)
                        if (reservation != null) {
                            storedReservations.add(reservation.copy(reservationID = document.id))
                        }
                    }
                }
            }
        }
    }

    /** Use campsiteId as documentId to load schedules*/
    private fun watchSchedules() {
        schedulesRef?.addSnapshotListener { snapshot, error ->
            if (error == null) {
                storedSchedules.clear()
                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        val available = (document.get("availableDays") as? List<*>).orEmpty()
                        val booked = (document.get("bookedDays") as? List<*>).orEmpty()
                        val schedule = AvailabilitySchedule(
                            availableDays = available.map { day ->
                                (day as Number).toInt()
                            }.toSet(),
                            bookedDays = booked.map { day ->
                                (day as Number).toInt()
                            }.toSet()
                        )
                        storedSchedules[document.id] = schedule
                        }
                    }
                }
            }
        }


    /**
     * Looks up the demo calendar for a campsite.
     *
     * @param campsiteId The ID used when saving the schedule.
     * @return The local schedule, or `null` if none has been stored.
     */
    fun localSchedule(campsiteId: String): AvailabilitySchedule? {
        return localCalendars[campsiteId]
    }

    /**
     * Looks up a schedule in the active storage mode.
     *
     * @param campsiteId The key used to store the campsite schedule.
     * @return The loaded schedule, or `null` if it is missing.
     */
    fun scheduleFor(campsiteId: String): AvailabilitySchedule? {
        if (localOnly) {
            return localCalendars[campsiteId]
        }
        return storedSchedules[campsiteId]
    }

    /**
     * Stores the schedule in memory or starts a Firestore write.
     *
     * @param documentId The campsite key in local mode or the Firestore document ID.
     * @param schedule The schedule to save.
     */
    fun saveAvailabilitySchedule(documentId: String, schedule: AvailabilitySchedule) {
        if (localOnly) {
            localCalendars[documentId] = schedule
        } else {
            val data = mapOf(
                "availableDays" to schedule.availableDays.toList(),
                "bookedDays" to schedule.bookedDays.toList()
            )
            schedulesRef?.document(documentId)?.set(data)
        }
    }

    /**
     * Saves a reservation in memory or starts a Firestore write under the given ID.
     *
     * @param documentId The ID to save the reservation under.
     * @param reservation The booking record to store.
     */
    fun saveReservation(documentId: String, reservation: Reservation) {
        if (localOnly) {
            // Using the requested document ID as the local record key.
            val record = reservation.copy(reservationID = documentId)
            for (position in storedReservations.indices) {
                if (storedReservations[position].reservationID == documentId) {
                    storedReservations[position] = record
                    return
                }
            }
            storedReservations.add(record)
        } else {
            reservationsRef?.document(documentId)?.set(reservation)
        }
    }

    /**
     * Saves the replacement record using the same operation as [saveReservation].
     *
     * @param documentId The ID of the reservation being updated.
     * @param updatedReservation The replacement record.
     */
    fun updateReservation(documentId: String, updatedReservation: Reservation) {
        saveReservation(documentId, updatedReservation)
    }

    /**
     * Saves a demo booking and the new calendar together on the UI thread.
     * Checks for a changed calendar, a different campsite ID, or a duplicate booking ID.
     *
     * @param campsiteId The campsite being booked.
     * @param expectedSchedule The calendar used when checking the dates.
     * @param changedSchedule The calendar with the new booking marked.
     * @param reservation The confirmed booking to save.
     * @return True if both records were stored. False if a check failed or local mode is off.
     */
    fun saveLocalBooking(
        campsiteId: String,
        expectedSchedule: AvailabilitySchedule,
        changedSchedule: AvailabilitySchedule,
        reservation: Reservation
    ): Boolean {
        if (!localOnly || localCalendars[campsiteId] != expectedSchedule) {
            return false
        }
        if (reservation.campsiteID != campsiteId) {
            return false
        }
        for (record in storedReservations) {
            if (record.reservationID == reservation.reservationID) {
                return false
            }
        }

        // These updates happen in one local action, after the controller checks the stay.
        // Saving both parts of the local booking after all checks have passed.
        localCalendars[campsiteId] = changedSchedule
        storedReservations.add(reservation)
        return true
    }
}
