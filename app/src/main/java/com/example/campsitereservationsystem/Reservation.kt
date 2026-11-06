/*
 * Keeping the booking record shared by the reservation screens and repository.
 */

package com.example.campsitereservationsystem

/**
 * Stores one entrant’s stay at a campsite.
 *
 * @property reservationID Identifier for this reservation.
 * @property campsiteID Identifier of the campsite being booked.
 * @property entrantID Identifier of the entrant making the booking.
 * @property startDate Check-in date in `yyyy-MM-dd` format.
 * @property endDate Check-out date in `yyyy-MM-dd` format. This night is outside the stay.
 * @property status Current state of the reservation.
 * @property rulesAcknowledged Whether the entrant accepted the rules for this stay.
 */
data class Reservation(
    val reservationID: String = "",
    val campsiteID: String = "",
    val entrantID: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val status: ReserveStatus = ReserveStatus.AWAITING,
    // Keeping the default false until the entrant has accepted the rules
    // for this stay.
    val rulesAcknowledged: Boolean = false
)
