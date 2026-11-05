package com.example.campsitereservationsystem

data class Reservation(
    val reservationID: String = "",
    val campsiteID: String = "",
    val entrantID: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val status: ReserveStatus = ReserveStatus.AWAITING
)


