package com.example.campsitereservationsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Booking rules for one winning opportunity in the local May 2027 prototype.
 * Uses the shared User, Campsite, Reservation and ReservationRepository classes.
 * The test menu supplies a sample winner until the team's lottery exposes winner IDs.
 */
class ReservationController(
    val campsite: Campsite,
    val campsiteName: String,
    val cancellationPolicy: String,
    private val winnerId: String,
    private val repository: ReservationRepository,
    val isWinner: Boolean = false
) {
    private val permissions = Permissions()

    var hasDeclined by mutableStateOf(false)
        private set

    init {
        require(repository.localOnly) { "This prototype needs a local reservation repository." }
    }

    /** Reads the latest dates, including changes made on the owner screen. */
    fun availability(): AvailabilitySchedule? {
        return repository.localSchedule(campsite.campsiteID)
    }

    /** Finds the confirmed booking for this opportunity, if it has already been used. */
    fun confirmedStay(): Reservation? {
        for (stay in repository.reservations) {
            if (stay.campsiteID == campsite.campsiteID && stay.entrantID == winnerId &&
                stay.status == ReserveStatus.CONFIRMED) {
                return stay
            }
        }
        return null
    }

    /** Returns an empty message when this user can use the winning opportunity. */
    fun opportunityProblem(guest: User): String {
        if (!isWinner || guest.id.isBlank() || guest.id != winnerId) {
            return "There is no winning opportunity for this guest."
        }
        if (!permissions.isAllowed(guest.roles, Action.RESERVE_CAMPSITE)) {
            return "This profile does not have permission to reserve a campsite."
        }
        if (hasDeclined) {
            return "This opportunity has been declined."
        }
        if (confirmedStay() != null) {
            return "This opportunity has already been booked."
        }
        return ""
    }

    /** Checks every overnight date. Check-out itself is not an occupied night. */
    fun dateProblem(stayStart: Int, stayEnd: Int): String {
        if (stayStart !in 1..31 || stayEnd !in 1..31) {
            return "Choose check-in and check-out within May 2027."
        }
        if (stayEnd <= stayStart) {
            return "Check-out must be after check-in."
        }
        val calendar = availability() ?: return "Availability is not loaded yet."
        for (night in stayStart until stayEnd) {
            if (night in calendar.bookedDays) {
                return "$night May is already booked. Choose another stay."
            }
            if (night !in calendar.availableDays) {
                return "$night May is unavailable. Choose another stay."
            }
        }
        return ""
    }

    /** Confirms a valid stay, returning a message if the booking cannot be made. */
    fun confirmBooking(guest: User, stayStart: Int, stayEnd: Int, readRules: Boolean): String {
        val opportunityNote = opportunityProblem(guest)
        if (opportunityNote.isNotEmpty()) {
            return opportunityNote
        }
        if (!readRules) {
            return "Please read and accept the campsite rules and cancellation policy."
        }
        val dateNote = dateProblem(stayStart, stayEnd)
        if (dateNote.isNotEmpty()) {
            return dateNote
        }

        val beforeBooking = availability() ?: return "Availability is not loaded yet."
        val openNights = beforeBooking.availableDays.toMutableSet()
        val takenNights = beforeBooking.bookedDays.toMutableSet()
        for (night in stayStart until stayEnd) {
            openNights.remove(night)
            takenNights.add(night)
        }

        // Local receipt numbers only need to be unique within this demo session.
        val receiptNumber = "LOCAL-SR-${repository.reservations.size + 1}"
        val stay = Reservation(
            reservationID = receiptNumber,
            campsiteID = campsite.campsiteID,
            entrantID = guest.id,
            startDate = mayDate(stayStart),
            endDate = mayDate(stayEnd),
            status = ReserveStatus.CONFIRMED,
            rulesAcknowledged = true
        )
        val afterBooking = beforeBooking.copy(
            availableDays = openNights.toSet(),
            bookedDays = takenNights.toSet()
        )

        // Check the same calendar is still current before storing both changes.
        val saved = repository.saveLocalBooking(
            campsite.campsiteID, beforeBooking, afterBooking, stay
        )
        if (!saved) {
            return "Availability changed. Please choose your dates again."
        }
        return ""
    }

    /** Declines the unused opportunity without creating or cancelling a reservation. */
    fun declineOpportunity(guest: User): String {
        val problem = opportunityProblem(guest)
        if (problem.isNotEmpty()) {
            return problem
        }
        hasDeclined = true
        return ""
    }

    /** Lists this guest's confirmed stays that have not reached their check-out date. */
    fun activeReservations(guest: User, today: String = todayStamp()): List<Reservation> {
        val upcomingStays = mutableListOf<Reservation>()
        for (stay in repository.reservations) {
            // ISO dates sort in calendar order; a stay ends on its check-out date.
            if (stay.entrantID == guest.id && stay.status == ReserveStatus.CONFIRMED &&
                stay.endDate > today) {
                upcomingStays.add(stay)
            }
        }
        return upcomingStays.toList()
    }
}

/** Formats a day using the same date format as the shared Reservation model. */
private fun mayDate(day: Int): String {
    return "2027-05-" + day.toString().padStart(2, '0')
}

/** Uses APIs supported by the project's minimum Android version. */
private fun todayStamp(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
