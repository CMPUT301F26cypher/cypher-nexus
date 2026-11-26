/*
 * Sorting reservations into the Active, Past and Cancelled lists
 * (Owner Frames 34, 36, 37 and Entrant Frames 62, 65).
 * There is no Firebase in this file, so it is easy to unit test.
 */

package com.example.campsitereservationsystem

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * One reservation plus the names we show on its grey card.
 *
 * @property reservation The record saved in the "reservations" collection.
 * @property campsiteName Name of the booked campsite.
 * @property location Location of the booked campsite.
 * @property entrantName Name of the entrant who made the reservation.
 */
data class ReservationInfo(
    val reservation: Reservation,
    val campsiteName: String,
    val location: String,
    val entrantName: String
)

/** Small rules that decide which list a reservation belongs in. */
object ReservationLists {

    private val shownFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    /**
     * Active = not cancelled, and the check-out date is today or later.
     * Once the check-out date has passed, the reservation moves to Past (Frame 34).
     */
    fun isActive(reservation: Reservation, today: LocalDate): Boolean {
        if (reservation.status == ReserveStatus.CANCELLED) {
            return false
        }
        val checkOut = toDate(reservation.endDate) ?: return false
        return !checkOut.isBefore(today)
    }

    /** Past = not cancelled, and the check-out date is before today. */
    fun isPast(reservation: Reservation, today: LocalDate): Boolean {
        if (reservation.status == ReserveStatus.CANCELLED) {
            return false
        }
        val checkOut = toDate(reservation.endDate) ?: return false
        return checkOut.isBefore(today)
    }

    /** Cancelled = cancelled by the entrant or rejected by the owner (Frame 37). */
    fun isCancelled(reservation: Reservation): Boolean {
        return reservation.status == ReserveStatus.CANCELLED
    }

    /** Active list, the soonest stay first. */
    fun active(list: List<ReservationInfo>, today: LocalDate): List<ReservationInfo> {
        return list
            .filter { isActive(it.reservation, today) }
            .sortedBy { it.reservation.startDate }
    }

    /** Past list, the most recent stay first. */
    fun past(list: List<ReservationInfo>, today: LocalDate): List<ReservationInfo> {
        return list
            .filter { isPast(it.reservation, today) }
            .sortedByDescending { it.reservation.startDate }
    }

    /** Cancelled list, the most recent stay first. */
    fun cancelled(list: List<ReservationInfo>): List<ReservationInfo> {
        return list
            .filter { isCancelled(it.reservation) }
            .sortedByDescending { it.reservation.startDate }
    }

    /** "Booked Dates" text on a card, for example "05/14/2027 - 05/16/2027". */
    fun bookedDates(reservation: Reservation): String {
        val checkIn = toDate(reservation.startDate)
        val checkOut = toDate(reservation.endDate)
        if (checkIn == null || checkOut == null) {
            return "${reservation.startDate} - ${reservation.endDate}"
        }
        return checkIn.format(shownFormat) + " - " + checkOut.format(shownFormat)
    }

    /** Reads a saved "yyyy-MM-dd" date. Returns null if the text is not a real date. */
    fun toDate(text: String): LocalDate? {
        return try {
            LocalDate.parse(text)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * The nights of a stay as May 2027 day numbers, because that is how the
     * booking calendar stores nights today (availabilitySchedules → bookedDays).
     * Example: 2027-05-14 to 2027-05-16 gives [14, 15]. The check-out day is not a night.
     *
     *
     * When the team moves to real calendar dates, I will change this function changes.
     */
    fun may2027Nights(reservation: Reservation): List<Int> {
        val checkIn = toDate(reservation.startDate) ?: return emptyList()
        val checkOut = toDate(reservation.endDate) ?: return emptyList()
        val nights = mutableListOf<Int>()
        var night = checkIn
        while (night.isBefore(checkOut)) {
            if (night.year == 2027 && night.monthValue == 5) {
                nights.add(night.dayOfMonth)
            }
            night = night.plusDays(1)
        }
        return nights
    }
}
