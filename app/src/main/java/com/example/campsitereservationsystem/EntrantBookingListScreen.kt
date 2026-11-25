package com.example.campsitereservationsystem

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.DateFormat

private enum class BookingSelectionPage { CAMPSITES, DATES, REVIEW, CONFIRMED }

@Composable
fun EntrantBookingListScreen(
    onBack: () -> Unit,
    user: User?
) {
    if (user == null || user.id.isBlank() || Role.ENTRANT !in user.roles) {
        BackHandler(onBack = onBack)
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
            Text("Sign in as an entrant to book a campsite you have won.")
            Button(onClick = onBack) { Text("Back") }
        }
        return
    }
    key(user.id) { EntrantBookingContent(user, onBack) }
}

@Composable
private fun EntrantBookingContent(user: User, onBack: () -> Unit) {
    val repository = remember { LotteryBookingRepository() }
    val scope = rememberCoroutineScope()
    var page by remember { mutableStateOf(BookingSelectionPage.CAMPSITES) }
    var options by remember { mutableStateOf(emptyList<LotteryBookingOption>()) }
    var selected by remember { mutableStateOf<LotteryBookingOption?>(null) }
    var schedule by remember { mutableStateOf<AvailabilitySchedule?>(null) }
    var checkIn by remember { mutableStateOf<Int?>(null) }
    var checkOut by remember { mutableStateOf<Int?>(null) }
    var acceptedRules by remember { mutableStateOf(false) }
    var confirmed by remember { mutableStateOf<Reservation?>(null) }
    var busy by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun runAction(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        error = null
        scope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (problem: Exception) {
                val bookingProblem = generateSequence<Throwable>(problem) { it.cause }
                    .filterIsInstance<LotteryBookingProblem>().firstOrNull()
                error = bookingProblem?.message
                    ?: "Could not load or save your booking. Check your connection and try again."
            } finally {
                busy = false
            }
        }
    }

    fun refresh() = runAction {
        options = repository.availableBookings(user.id)
        loaded = true
    }

    fun goBack() {
        if (busy) return
        error = null
        page = when (page) {
            BookingSelectionPage.CAMPSITES -> { onBack(); return }
            BookingSelectionPage.DATES, BookingSelectionPage.CONFIRMED -> BookingSelectionPage.CAMPSITES
            BookingSelectionPage.REVIEW -> BookingSelectionPage.DATES
        }
    }

    LaunchedEffect(user.id) { refresh() }
    BackHandler { goBack() }

    val choice = selected
    val availability = schedule
    val start = checkIn
    val end = checkOut
    val receipt = confirmed
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT) }

    //A box is used to allow the back button to be in the bottom left at all times
    Box(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)
    ) {
        //LazyColumn for the list allows it to be scrollable
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(bottom = 72.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = when (page) {
                        BookingSelectionPage.CAMPSITES -> "Pick a Campsite"
                        BookingSelectionPage.DATES -> "Booking"
                        BookingSelectionPage.REVIEW -> "Review Booking"
                        BookingSelectionPage.CONFIRMED -> "Your reservation"
                    },
                    fontSize = 40.sp
                )
            }
            if (busy) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            error?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }

            when (page) {
                BookingSelectionPage.CAMPSITES -> {
                    item {
                        Text("Choose a campsite from a lottery you have won. Each win can be used once.")
                        TextButton(onClick = { refresh() }, enabled = !busy) { Text("Refresh") }
                    }
                    if (loaded && options.isEmpty()) {
                        item { Text("No won lotteries are currently available for booking.") }
                    }
                    items(options, key = { "${it.lottery.id}/${it.campsite.id}" }) { option ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(option.campsite.name, style = MaterialTheme.typography.titleMedium)
                                Text(option.campsite.location)
                                Text("Lottery: ${option.lottery.title}")
                                Text(option.lottery.bookingEnd?.let {
                                    "Booking deadline: ${dateFormat.format(it.toDate())}"
                                } ?: "No booking deadline specified")
                                Button(enabled = !busy, onClick = {
                                    runAction {
                                        val dates = repository.schedule(option.campsite.id)
                                        selected = option
                                        schedule = dates
                                        checkIn = null
                                        checkOut = null
                                        acceptedRules = false
                                        page = BookingSelectionPage.DATES
                                    }
                                }) { Text("Book") }
                            }
                        }
                    }
                }

                BookingSelectionPage.DATES -> if (choice != null && availability != null) {
                    item {
                        Text(choice.campsite.name, style = MaterialTheme.typography.titleMedium)
                        Text(choice.campsite.location)
                        TextButton(enabled = !busy, onClick = {
                            runAction {
                                schedule = repository.schedule(choice.campsite.id)
                                checkIn = null
                                checkOut = null
                                acceptedRules = false
                            }
                        }) { Text("Refresh availability") }
                        Text("Tap check-in, then a later check-out. Check-out is not an occupied night.")
                        BookingCalendar(availability, start, end) { day ->
                            if (!busy) {
                                error = null
                                acceptedRules = false
                                if (start == null || end != null || day <= start) {
                                    if (day < 31 && day in availability.availableDays && day !in availability.bookedDays) {
                                        checkIn = day
                                        checkOut = null
                                    } else {
                                        error = "Choose an available check-in night before 31 May."
                                    }
                                } else {
                                    val problem = lotteryDateProblem(availability, start, day)
                                    if (problem.isEmpty()) checkOut = day else error = problem
                                }
                            }
                        }
                        start?.let { Text("Check-in: $it May 2027") }
                        end?.let { Text("Check-out: $it May 2027") }
                        Button(enabled = !busy && start != null && end != null, onClick = {
                            error = null
                            acceptedRules = false
                            page = BookingSelectionPage.REVIEW
                        }) { Text("Review reservation") }
                    }
                }

                BookingSelectionPage.REVIEW -> if (choice != null && start != null && end != null) {
                    item {
                        Text(choice.campsite.name, style = MaterialTheme.typography.titleMedium)
                        Text(choice.campsite.location)
                        Text("$start May 2027 to $end May 2027 (${end - start} nights)")
                        Text("Campsite rules", style = MaterialTheme.typography.titleMedium)
                        Text(choice.campsite.rules.ifBlank { "No additional rules were provided by the owner." })
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = acceptedRules, enabled = !busy, onCheckedChange = { acceptedRules = it })
                            Text("I have read and agree to the campsite rules.", Modifier.weight(1f))
                        }
                        Button(enabled = acceptedRules && !busy, onClick = {
                            runAction {
                                confirmed = repository.confirm(choice.lottery, user, choice.campsite, start, end, acceptedRules)
                                // Remove the booked win; other lottery wins remain available.
                                options = options.filterNot { it.lottery.id == choice.lottery.id }
                                page = BookingSelectionPage.CONFIRMED
                            }
                        }) { Text("Confirm reservation") }
                        TextButton(enabled = !busy, onClick = { goBack() }) { Text("Change dates") }
                    }
                }

                BookingSelectionPage.CONFIRMED -> if (receipt != null) {
                    item {
                        Text("Reservation confirmed", style = MaterialTheme.typography.titleMedium)
                        choice?.let { Text(it.campsite.name) }
                        Text("Check-in: ${receipt.startDate}")
                        Text("Check-out: ${receipt.endDate}")
                        Text("Reservation: ${receipt.reservationID}")
                    }
                }
            }
        }
        Button(
            onClick = { goBack() },
            enabled = !busy,
            modifier = Modifier.align(Alignment.BottomStart).scale(1.2f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF819F5C))
        ) {
            Text(text = "Back", color = Color.Black)
        }
    }
}