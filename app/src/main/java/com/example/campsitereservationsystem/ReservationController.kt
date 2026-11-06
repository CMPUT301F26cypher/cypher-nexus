// Checking booking rules and updating the sample winner’s calendar for the entrant demo.

package com.example.campsitereservationsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.campsitereservationsystem.campsite.Campsite

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
    // Using the app’s shared role checks when deciding whether the guest can reserve this campsite.
    private val permissions = Permissions()

    // Keeping the decline state visible to the booking screen after confirmation.
    var hasDeclined by mutableStateOf(false)
        private set

    init {
        require(repository.localOnly) { "This prototype needs a local reservation repository." }
    }

    /**
     * Reads the latest dates, including changes saved on the owner screen.
     *
     * @return The campsite’s local schedule, or `null` if it has not been loaded.
     */
    fun availability(): AvailabilitySchedule? {
        return repository.localSchedule(campsite.id)
    }

    /**
     * Finds the booking made with this winning opportunity.
     *
     * @return The confirmed stay for this winner and campsite, or `null`.
     */
    fun confirmedStay(): Reservation? {
        for (stay in repository.reservations) {
            if (stay.campsiteID == campsite.id && stay.entrantID == winnerId &&
                stay.status == ReserveStatus.CONFIRMED) {
                return stay
            }
        }
        return null
    }

    /**
     * Checks the winner, the guest’s permission, and whether the offer is still open.
     *
     * @param guest The entrant trying to use the opportunity.
     * @return An empty string if the guest can book, or a message explaining why they cannot.
     */
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

    /**
     * Checks every night from check-in up to, but excluding, check-out.
     *
     * @param stayStart The check-in day in May 2027.
     * @param stayEnd The check-out day in May 2027.
     * @return An empty string for valid dates, or a message explaining the problem.
     */
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

    /**
     * Confirms the stay and marks its nights as booked in the shared demo calendar.
     *
     * @param guest The entrant confirming the stay.
     * @param stayStart The check-in day in May 2027.
     * @param stayEnd The check-out day in May 2027.
     * @param readRules Whether the guest accepted the rules and cancellation policy.
     * @return An empty string when the booking is saved, or an error message.
     */
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

        // Keeping the schedule used for validation so the repository can check it before saving.
        val beforeBooking = availability() ?: return "Availability is not loaded yet."
        val openNights = beforeBooking.availableDays.toMutableSet()
        val takenNights = beforeBooking.bookedDays.toMutableSet()
        // Moving only occupied nights from open to booked.
        for (night in stayStart until stayEnd) {
            openNights.remove(night)
            takenNights.add(night)
        }

        // Local receipt numbers only need to be unique within this demo session.
        // Giving the new stay a receipt number within the demo session.
        val receiptNumber = "LOCAL-SR-${repository.reservations.size + 1}"
        val stay = Reservation(
            reservationID = receiptNumber,
            campsiteID = campsite.id,
            entrantID = guest.id,
            startDate = mayDate(stayStart),
            endDate = mayDate(stayEnd),
            status = ReserveStatus.CONFIRMED,
            rulesAcknowledged = true
        )
        // Preparing a separate calendar so the current one changes only after a successful save.
        val afterBooking = beforeBooking.copy(
            availableDays = openNights.toSet(),
            bookedDays = takenNights.toSet()
        )

        // Check the same calendar is still current before storing both changes.
        val saved = repository.saveLocalBooking(
            campsite.id, beforeBooking, afterBooking, stay
        )
        if (!saved) {
            return "Availability changed. Please choose your dates again."
        }
        return ""
    }

    /**
     * Closes an unused winning opportunity and leaves the calendar alone.
     *
     * @param guest The entrant declining the offer.
     * @return An empty string when the offer is declined, or an error message.
     */
    fun declineOpportunity(guest: User): String {
        val problem = opportunityProblem(guest)
        if (problem.isNotEmpty()) {
            return problem
        }
        // Closing the offer once eligibility has been checked.
        hasDeclined = true
        return ""
    }

    /**
     * Lists this guest’s confirmed stays whose check-out dates are after today.
     *
     * @param guest The entrant whose stays are being shown.
     * @param today The comparison date in `yyyy-MM-dd` format. Defaults to the current date.
     * @return The active reservations for the guest.
     */
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
