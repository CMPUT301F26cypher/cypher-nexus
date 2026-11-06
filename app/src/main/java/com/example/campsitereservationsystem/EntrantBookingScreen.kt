// Taking a sample winner through date selection, review, confirmation, or declining the offer.

package com.example.campsitereservationsystem

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Keeping the booking steps inside one app destination.
private enum class BookingPage {
    DATES,
    REVIEW,
    CONFIRMED,
    RESERVATIONS
}

/**
 * Entrant views for US 01.04.01, 01.04.03, 01.05.01 and 01.06.01.
 * Keeps only form and navigation state here. The controller checks booking rules,
 * and the shared repository keeps records while this demo session is open.
 */
@Composable
fun EntrantBookingScreen(
    controller: ReservationController,
    guest: User,
    onBack: () -> Unit
) {
    // Starting at date selection.
    var page by remember { mutableStateOf(BookingPage.DATES) }
    var stayStart by remember { mutableStateOf<Int?>(null) }
    var stayEnd by remember { mutableStateOf<Int?>(null) }
    // Holding the checkbox value separately from the reservation.
    var acceptedRules by remember { mutableStateOf(false) }
    var showDecline by remember { mutableStateOf(false) }
    var bookingNote by remember { mutableStateOf("") }

    // Each page starts at the top instead of inheriting the previous scroll position.
    val pageScroll = remember(page) { ScrollState(0) }

    // Handling Back inside the booking flow first.
    BackHandler {
        bookingNote = ""
        // Returning from review to the selected dates.
        if (page == BookingPage.REVIEW) {
            page = BookingPage.DATES
        } else if (page == BookingPage.CONFIRMED) {
            page = BookingPage.RESERVATIONS
        } else {
            onBack()
        }
    }

    // Using the scaffold insets around the booking content.
    Scaffold { pagePadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pagePadding)
                .verticalScroll(pageScroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Providing a direct exit to the calling screen.
            TextButton(onClick = onBack) {
                Text("Close booking")
            }
            // Showing the sample campsite and calling out the local prototype before asking the guest to choose dates.
            Text("ENTRANT · LOCAL PROTOTYPE", style = MaterialTheme.typography.labelMedium)
            Text(controller.campsiteName, style = MaterialTheme.typography.headlineMedium)
            Text(controller.campsite.location)
            Text("Sample winning opportunity. This screen is not connected to lottery results yet.",
                style = MaterialTheme.typography.bodySmall)

            if (page == BookingPage.DATES) {
                // Reading the current opportunity and schedule from the controller.
                val opportunityNote = controller.opportunityProblem(guest)
                // Reading the shared owner calendar when showing dates.
                val calendar = controller.availability()

                if (opportunityNote.isEmpty() && calendar != null) {
                    Text("Choose your dates", style = MaterialTheme.typography.titleLarge)
                    Text("Tap an available check-in date, then a later check-out date.")
                    // Giving the date grid the current schedule and the selected range.
                    BookingCalendar(
                        schedule = calendar,
                        checkIn = stayStart,
                        checkOut = stayEnd,
                        onDatePicked = { calendarDay ->
                            val start = stayStart

                            // A new tap starts over once a complete stay has been selected.
                            // Starting a new range after a complete selection or a tap on an earlier date.
                            if (start == null || stayEnd != null || calendarDay <= start) {
                                // Requiring an open check-in night with room for a later check-out.
                                if (calendarDay in calendar.availableDays && calendarDay < 31) {
                                    stayStart = calendarDay
                                    stayEnd = null
                                    bookingNote = ""
                                } else {
                                    bookingNote = "Choose an available check-in night before 31 May."
                                }
                            } else {
                                // Checking each occupied night before setting the end date.
                                val problem = controller.dateProblem(start, calendarDay)
                                if (problem.isEmpty()) {
                                    // Keeping the chosen check-out only after the range passes validation.
                                    stayEnd = calendarDay
                                    bookingNote = ""
                                } else {
                                    bookingNote = problem
                                }
                            }
                        }
                    )

                    // Showing the two choices independently as they are made.
                    if (stayStart != null) {
                        Text("Check-in: $stayStart May 2027")
                    }
                    if (stayEnd != null) {
                        Text("Check-out: $stayEnd May 2027")
                    }
                    // Showing the reason beside the chosen dates so the guest can fix the selection before moving on.
                    if (bookingNote.isNotEmpty()) {
                        Text(bookingNote, color = MaterialTheme.colorScheme.error)
                    }

                    Button(
                        onClick = {
                            // Resetting the acknowledgement when entering review.
                            acceptedRules = false
                            bookingNote = ""
                            page = BookingPage.REVIEW
                        },
                        enabled = stayStart != null && stayEnd != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Review reservation")
                    }
                    OutlinedButton(
                        // Opening a confirmation dialog instead of immediately giving up the offer.
                        onClick = { showDecline = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Decline opportunity")
                    }
                } else if (controller.hasDeclined) {
                    // Explaining why the date picker is closed after a decline.
                    Text("Opportunity declined", style = MaterialTheme.typography.titleLarge)
                    Text("No reservation was created. This opportunity is now closed.")
                } else if (controller.confirmedStay() != null) {
                    Text("You have already booked", style = MaterialTheme.typography.titleLarge)
                    Text("Open My active reservations to see your stay.")
                } else {
                    // Using the controller’s reason when it is available.
                    Text(opportunityNote.ifEmpty { "Availability is not loaded yet." })
                }

                TextButton(
                    onClick = {
                        bookingNote = ""
                        // Opening active reservations from this flow even when there is no new opportunity to use.
                        page = BookingPage.RESERVATIONS
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("My active reservations")
                }
            }

            // Reading the selected dates again before rendering the review.
            if (page == BookingPage.REVIEW) {
                val start = stayStart
                val end = stayEnd
                if (start != null && end != null) {
                    Text("Review reservation", style = MaterialTheme.typography.titleLarge)
                    Text("Check-in: $start May 2027")
                    Text("Check-out: $end May 2027")
                    // Calculating the number of nights from the date difference.
                    Text("${end - start} nights")

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Sample campsite rules", style = MaterialTheme.typography.titleMedium)
                            // Reading the rules from the sample campsite rather than duplicating them in the form.
                            Text(controller.campsite.rules)
                            Text("Sample cancellation policy", style = MaterialTheme.typography.titleMedium)
                            Text(controller.cancellationPolicy)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Connecting the checkbox to the review state.
                        Checkbox(
                            checked = acceptedRules,
                            onCheckedChange = { acceptedRules = it }
                        )
                        Text("I have read the campsite rules and cancellation policy.",
                            modifier = Modifier.weight(1f))
                    }
                    if (bookingNote.isNotEmpty()) {
                        Text(bookingNote, color = MaterialTheme.colorScheme.error)
                    }

                    Button(
                        onClick = {
                            // Recheck current availability and permission at confirmation time.
                            // Submitting the selected nights and acknowledgement together.
                            val problem = controller.confirmBooking(guest, start, end, acceptedRules)
                            if (problem.isEmpty()) {
                                bookingNote = ""
                                // Advancing only when the controller returns an empty error string.
                                page = BookingPage.CONFIRMED
                            } else {
                                // Displaying the controller’s message in review when the stay cannot be saved.
                                bookingNote = problem
                            }
                        },
                        // Requiring acknowledgement in the screen as well as in the controller.
                        enabled = acceptedRules,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Confirm reservation")
                    }
                    OutlinedButton(
                        onClick = {
                            bookingNote = ""
                            page = BookingPage.DATES
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Returning to the calendar from review while retaining the current choices for editing.
                        Text("Change dates")
                    }
                }
            }

            if (page == BookingPage.CONFIRMED) {
                // Showing the confirmation only after a successful save.
                Text("Reservation confirmed", style = MaterialTheme.typography.titleLarge)
                // Reading the saved reservation so confirmation shows the assigned receipt number.
                val receipt = controller.confirmedStay()
                if (receipt != null) {
                    BookingReceipt(receipt, controller.campsiteName)
                }
                Text("Your nights are now booked in the shared owner calendar.")
                Text("Saved for this local session only.", style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = { page = BookingPage.RESERVATIONS },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View my reservations")
                }
            }

            if (page == BookingPage.RESERVATIONS) {
                Text("My active reservations", style = MaterialTheme.typography.titleLarge)
                // Letting the controller filter the saved reservations for this guest and the current date.
                val upcomingStays = controller.activeReservations(guest)
                if (upcomingStays.isEmpty()) {
                    Text("No active reservations yet.")
                }
                // Rendering one receipt per active stay and keeping the same receipt layout as confirmation.
                for (stay in upcomingStays) {
                    // Using the display name for the known sample campsite.
                    val campsiteName = if (stay.campsiteID == controller.campsite.id) {
                        controller.campsiteName
                    } else {
                        "Campsite ${stay.campsiteID}"
                    }
                    BookingReceipt(stay, campsiteName)
                }
                // Showing a way back to the date picker only while the guest can still use the opportunity.
                if (controller.opportunityProblem(guest).isEmpty()) {
                    OutlinedButton(
                        onClick = { page = BookingPage.DATES },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to available dates")
                    }
                }
            }
        }
    }

    // Keeping the decline action behind explicit confirmation.
    if (showDecline) {
        AlertDialog(
            onDismissRequest = { showDecline = false },
            title = { Text("Decline this opportunity?") },
            text = { Text("No reservation will be made. You cannot book through this opportunity afterwards.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Asking the controller to close the opportunity for this guest.
                        bookingNote = controller.declineOpportunity(guest)
                        showDecline = false
                        if (bookingNote.isEmpty()) {
                            stayStart = null
                            stayEnd = null
                        }
                    }
                ) {
                    Text("Yes, decline")
                }
            },
            // Letting the guest keep the opportunity and close the dialog without sending a decline request.
            dismissButton = {
                TextButton(onClick = { showDecline = false }) {
                    Text("Keep opportunity")
                }
            }
        )
    }
}

/** Uses the same stored record on the confirmation page and active list. */
@Composable
private fun BookingReceipt(stay: Reservation, campsiteName: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(campsiteName, style = MaterialTheme.typography.titleMedium)
            Text("Confirmation: ${stay.reservationID}")
            Text("Check-in: ${stay.startDate}")
            Text("Check-out: ${stay.endDate}")
            // Marking the displayed record as confirmed.
            Text("Status: Confirmed", color = MaterialTheme.colorScheme.primary)
            // Displaying the acknowledgement from the stored stay.
            if (stay.rulesAcknowledged) {
                Text("Rules and policy acknowledged", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
