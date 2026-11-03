package com.example.campsitereservationsystem

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ReservationRepository {
    private val db = Firebase.firestore
    private val reservationsRef =
        db.collection("reservations")
    private val schedulesRef =
        db.collection("availabilitySchedules")
    private val _reservations =
        mutableStateListOf<Reservation>()
    val reservations: List<Reservation>
        get() = _reservations
    private val _availabilitySchedules =
        mutableStateListOf<AvailabilitySchedule>()
    val availabilitySchedules: List<AvailabilitySchedule>
        get() = _availabilitySchedules
    init {
        reservationsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _reservations.clear()
            snapshot?.documents?.forEach { document ->
                val reservation =
                    document.toObject(Reservation::class.java)
                if (reservation != null) {
                    _reservations.add(reservation.copy(reservationID = document.id))
                }
            }
        }
        schedulesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _availabilitySchedules.clear()
            snapshot?.documents?.forEach { document ->
                val schedule =
                    document.toObject(AvailabilitySchedule::class.java)
                if (schedule != null) {
                    // TODO: fix upon availabilitySchedule implementation
                }
            }
        }
    }
    fun saveAvailabilitySchedule(
        documentId: String,
        schedule: AvailabilitySchedule
    ) {
        schedulesRef.document(documentId).set(schedule)
    }
    fun saveReservation(
        documentId: String,
        reservation: Reservation
    ) {
        reservationsRef.document(documentId).set(reservation)
    }
    fun updateReservation(
        documentId: String,
        updatedReservation: Reservation
    ) {
        reservationsRef.document(documentId).set(updatedReservation)
    }
}
