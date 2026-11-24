// Holding everything on the Lottery Details form and checking it before it is saved (US 02.04.03, 02.04.04, 02.04.06).

package com.example.campsitereservationsystem

import java.time.LocalDateTime

/**
 * What the owner fills in on the Lottery Details form (Frame 24, and Frames 31-32 when updating).
 *
 * Registration period (US 02.04.03): when entrants can join the lottery waiting list.
 * Booking period (US 02.04.04): when the winners can reserve, after the lottery has drawn winners.
 *
 * @property registration The registration period.
 * @property booking The booking period.
 */
data class LotteryDetails(
    val registration: LotteryPeriod = LotteryPeriod(),
    val booking: LotteryPeriod = LotteryPeriod()
) {

    /**
     * Checks the whole form. The message is shown in the Frame 25 error popup.
     *
     * Rules, in the order they are checked:
     * 1. The registration period is filled in correctly.
     * 2. The booking period is filled in correctly.
     * 3. Creating only: registration ends in the future.
     *    (When updating, registration may already be over, for example when the owner
     *    only wants to move the booking end date, as in storyboard Frame 32.)
     * 4. Booking starts after registration ends (winners are drawn when registration ends).
     * 5. Booking ends in the future.
     *
     * @param now The current date and time.
     * @param isUpdate True when the owner is changing a lottery that is already saved (US 02.04.06).
     * @return An empty string if everything is fine, or a message explaining what to fix.
     */
    fun problem(now: LocalDateTime, isUpdate: Boolean = false): String {
        val registrationProblem = registration.problem("Registration")
        if (registrationProblem.isNotEmpty()) {
            return registrationProblem
        }

        val bookingProblem = booking.problem("Booking")
        if (bookingProblem.isNotEmpty()) {
            return bookingProblem
        }

        val registrationEnd = registration.end()
        val bookingStart = booking.start()
        val bookingEnd = booking.end()

        // Safety net: both periods were checked above, so this should never happen.
        if (registrationEnd == null || bookingStart == null || bookingEnd == null) {
            return "Please check the dates and times."
        }

        if (!isUpdate && !registrationEnd.isAfter(now)) {
            return "Registration end must be in the future."
        }
        if (bookingStart.isBefore(registrationEnd)) {
            return "Booking must start after registration ends."
        }
        if (!bookingEnd.isAfter(now)) {
            return "Booking end must be in the future."
        }
        return ""
    }
}
