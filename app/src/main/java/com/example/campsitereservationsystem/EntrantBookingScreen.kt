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
    var page by remember { mutableStateOf(BookingPage.DATES) }
    var stayStart by remember { mutableStateOf<Int?>(null) }
    var stayEnd by remember { mutableStateOf<Int?>(null) }
    var acceptedRules by remember { mutableStateOf(false) }
    var showDecline by remember { mutableStateOf(false) }
    var bookingNote by remember { mutableStateOf("") }

    // Each page starts at the top instead of inheriting the previous scroll position.
    val pageScroll = remember(page) { ScrollState(0) }

    BackHandler {
        bookingNote = ""
        if (page == BookingPage.REVIEW) {
            page = BookingPage.DATES
        } else if (page == BookingPage.CONFIRMED) {
            page = BookingPage.RESERVATIONS
        } else {
            onBack()
        }
    }

    Scaffold { pagePadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pagePadding)
                .verticalScroll(pageScroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("Close booking")
            }
            Text("ENTRANT · LOCAL PROTOTYPE", style = MaterialTheme.typography.labelMedium)
            Text(controller.campsiteName, style = MaterialTheme.typography.headlineMedium)
            Text(controller.campsite.location)
            Text("Sample winning opportunity. This screen is not connected to lottery results yet.",
                style = MaterialTheme.typography.bodySmall)

            if (page == BookingPage.DATES) {
                val opportunityNote = controller.opportunityProblem(guest)
                val calendar = controller.availability()

                if (opportunityNote.isEmpty() && calendar != null) {
                    Text("Choose your dates", style = MaterialTheme.typography.titleLarge)
                    Text("Tap an available check-in date, then a later check-out date.")
                    BookingCalendar(
                        schedule = calendar,
                        checkIn = stayStart,
                        checkOut = stayEnd,
                        onDatePicked = { calendarDay ->
                            val start = stayStart

                            // A new tap starts over once a complete stay has been selected.
                            if (start == null || stayEnd != null || calendarDay <= start) {
                                if (calendarDay in calendar.availableDays && calendarDay < 31) {
                                    stayStart = calendarDay
                                    stayEnd = null
                                    bookingNote = ""
                                } else {
                                    bookingNote = "Choose an available check-in night before 31 May."
                                }
                            } else {
                                val problem = controller.dateProblem(start, calendarDay)
                                if (problem.isEmpty()) {
                                    stayEnd = calendarDay
                                    bookingNote = ""
                                } else {
                                    bookingNote = problem
                                }
                            }
                        }
                    )

                    if (stayStart != null) {
                        Text("Check-in: $stayStart May 2027")
                    }
                    if (stayEnd != null) {
                        Text("Check-out: $stayEnd May 2027")
                    }
                    if (bookingNote.isNotEmpty()) {
                        Text(bookingNote, color = MaterialTheme.colorScheme.error)
                    }

                    Button(
                        onClick = {
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
                        onClick = { showDecline = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Decline opportunity")
                    }
                } else if (controller.hasDeclined) {
                    Text("Opportunity declined", style = MaterialTheme.typography.titleLarge)
                    Text("No reservation was created. This opportunity is now closed.")
                } else if (controller.confirmedStay() != null) {
                    Text("You have already booked", style = MaterialTheme.typography.titleLarge)
                    Text("Open My active reservations to see your stay.")
                } else {
                    Text(opportunityNote.ifEmpty { "Availability is not loaded yet." })
                }

                TextButton(
                    onClick = {
                        bookingNote = ""
                        page = BookingPage.RESERVATIONS
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("My active reservations")
                }
            }

            if (page == BookingPage.REVIEW) {
                val start = stayStart
                val end = stayEnd
                if (start != null && end != null) {
                    Text("Review reservation", style = MaterialTheme.typography.titleLarge)
                    Text("Check-in: $start May 2027")
                    Text("Check-out: $end May 2027")
                    Text("${end - start} nights")

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Sample campsite rules", style = MaterialTheme.typography.titleMedium)
                            Text(controller.campsite.rules)
                            Text("Sample cancellation policy", style = MaterialTheme.typography.titleMedium)
                            Text(controller.cancellationPolicy)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                            val problem = controller.confirmBooking(guest, start, end, acceptedRules)
                            if (problem.isEmpty()) {
                                bookingNote = ""
                                page = BookingPage.CONFIRMED
                            } else {
                                bookingNote = problem
                            }
                        },
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
                        Text("Change dates")
                    }
                }
            }

            if (page == BookingPage.CONFIRMED) {
                Text("Reservation confirmed", style = MaterialTheme.typography.titleLarge)
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
                val upcomingStays = controller.activeReservations(guest)
                if (upcomingStays.isEmpty()) {
                    Text("No active reservations yet.")
                }
                for (stay in upcomingStays) {
                    val campsiteName = if (stay.campsiteID == controller.campsite.campsiteID) {
                        controller.campsiteName
                    } else {
                        "Campsite ${stay.campsiteID}"
                    }
                    BookingReceipt(stay, campsiteName)
                }
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

    if (showDecline) {
        AlertDialog(
            onDismissRequest = { showDecline = false },
            title = { Text("Decline this opportunity?") },
            text = { Text("No reservation will be made. You cannot book through this opportunity afterwards.") },
            confirmButton = {
                TextButton(
                    onClick = {
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
            Text("Status: Confirmed", color = MaterialTheme.colorScheme.primary)
            if (stay.rulesAcknowledged) {
                Text("Rules and policy acknowledged", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
