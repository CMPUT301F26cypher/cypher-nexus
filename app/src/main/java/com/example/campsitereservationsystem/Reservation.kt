package com.example.campsitereservationsystem

/**
 * A reservation shared by the booking screens and repository.
 * Dates use yyyy-MM-dd so they can be compared and stored consistently.
 * rulesAcknowledged belongs to this reservation, rather than to the whole app.
 */
data class Reservation(
    val reservationID: String = "",
    val campsiteID: String = "",
    val entrantID: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val status: ReserveStatus = ReserveStatus.AWAITING,
    val rulesAcknowledged: Boolean = false
)
