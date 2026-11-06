package com.example.campsitereservationsystem

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Shared storage for reservations and availability.
 * The existing Firebase mode is still the default. The booking demo uses localOnly
 * so it can run without contacting Firebase. Local records reset with the activity.
 */
class ReservationRepository(val localOnly: Boolean = false) {
    // Local mode must not initialise Firebase just to open the demo screens.
    private val db = if (localOnly) null else Firebase.firestore
    private val reservationsRef = db?.collection("reservations")
    private val schedulesRef = db?.collection("availabilitySchedules")

    private val storedReservations = mutableStateListOf<Reservation>()
    private val storedSchedules = mutableStateMapOf<String, AvailabilitySchedule>()
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
        if (!localOnly) {
            watchReservations()
            watchSchedules()
        }
    }

    /** Keeps the team's existing Firestore reservation listener. */
    private fun watchReservations() {
        reservationsRef?.addSnapshotListener { snapshot, error ->
            if (error == null) {
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


    /** Gets the local calendar for one campsite. */
    fun localSchedule(campsiteId: String): AvailabilitySchedule? {
        return localCalendars[campsiteId]
    }

    fun scheduleFor(campsiteId: String): AvailabilitySchedule? {
        if (localOnly) {
            return localCalendars[campsiteId]
        }
        return storedSchedules[campsiteId]
    }

    /** Saves dates locally for the demo, or uses the existing Firebase write. */
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

    /** Saves a reservation under the supplied ID in the selected storage mode. */
    fun saveReservation(documentId: String, reservation: Reservation) {
        if (localOnly) {
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

    /** Updates an existing reservation through the same save operation. */
    fun updateReservation(documentId: String, updatedReservation: Reservation) {
        saveReservation(documentId, updatedReservation)
    }

    /**
     * Stores a local booking and its dates together on the UI thread.
     * Rejects an outdated calendar or duplicate ID. This is not a server transaction.
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
        localCalendars[campsiteId] = changedSchedule
        storedReservations.add(reservation)
        return true
    }
}
