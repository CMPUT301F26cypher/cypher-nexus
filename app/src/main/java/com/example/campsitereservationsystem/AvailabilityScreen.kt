// Showing the owner’s May 2027 calendar and saving edits for the current demo session.

package com.example.campsitereservationsystem

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Owner-only sample calendar. Edits stay in a draft until Save is pressed.
 * Cancel and Back discard the draft. The parent keeps saved dates in memory.
 * Real campsites, other months and persistent storage still need connecting.
 */
@Composable
fun AvailabilityScreen(
    // Receiving the saved schedule from the caller.
    schedule: AvailabilitySchedule,
    onSave: (AvailabilitySchedule) -> Unit,
    onBack: () -> Unit
) {
    // Keeping edits in a draft so the entrant calendar continues to use the saved dates.
    var draftSchedule by remember(schedule) { mutableStateOf(schedule) }
    // Starting in read-only mode.
    var editing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    // Holding selected days separately from the schedule.
    val selectedDays = remember { mutableStateListOf<Int>() }

    // Using the same exit action for system Back and the screen button.
    BackHandler(onBack = onBack)

    // Applying the scaffold padding to keep the calendar below the system bars.
    Scaffold { pagePadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pagePadding)
                // Keeping the page scroll position while date selections change.
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Leaving the editor through the callback supplied by the navigation graph.
            TextButton(onClick = onBack) {
                Text("Back to welcome")
            }

            // Identifying the sample campsite and month before showing the dates.
            Text("Availability", style = MaterialTheme.typography.headlineMedium)
            Text("OWNER · SAMPLE CAMPSITE", style = MaterialTheme.typography.labelMedium)
            Text("Spruce Ridge", style = MaterialTheme.typography.titleLarge)
            Text("Jasper area, Alberta")
            Text("May 2027", style = MaterialTheme.typography.titleMedium)

            // Drawing from the draft so the owner can preview changes immediately.
            OwnerCalendar(
                schedule = draftSchedule,
                selectedDays = selectedDays,
                editing = editing,
                onDateClick = { day ->
                    if (day in selectedDays) {
                        selectedDays.remove(day)
                    } else {
                        selectedDays.add(day)
                    }
                    message = ""
                }
            )

            // Explaining the calendar symbols alongside the colours.
            Text("A = Available   B = Booked   – = Unavailable   ✓ = Selected",
                style = MaterialTheme.typography.bodySmall)
            Text("Booked nights cannot be edited. Check-out is not an occupied night.",
                style = MaterialTheme.typography.bodySmall)
            // Counting nights from the draft so the totals reflect the edits currently shown in the grid.
            Text("${draftSchedule.availableDays.size} available nights · " +
                    "${draftSchedule.bookedDays.size} booked nights")

            // Showing edit controls only after the owner has chosen to edit the schedule.
            if (editing) {
                Text("Tap dates, then choose Available or Unavailable.")
                Text("${selectedDays.size} dates selected")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            // Opening the selected nights on a copy of the schedule.
                            draftSchedule = draftSchedule.withAvailability(selectedDays, makeAvailable = true)
                            selectedDays.clear()
                            message = "Marked available. Save changes when ready."
                        },
                        // Keeping the status action disabled while there is no selection to apply it to.
                        enabled = selectedDays.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Available")
                    }

                    OutlinedButton(
                        onClick = {
                            // Closing the selected nights by removing them from availability.
                            draftSchedule = draftSchedule.withAvailability(selectedDays, makeAvailable = false)
                            selectedDays.clear()
                            message = "Marked unavailable. Save changes when ready."
                        },
                        enabled = selectedDays.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Unavailable")
                    }
                }

                // Prompting the owner to apply a status before saving.
                if (selectedDays.isNotEmpty()) {
                    Text("Choose a status for the selected dates before saving.",
                        style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        // Saving the finished draft so both calendars can use the updated dates.
                        onSave(draftSchedule)
                        editing = false
                        message = "Availability saved for this demo session."
                    },
                    // Enabling Save only when the dates changed and there is no unfinished selection.
                    enabled = draftSchedule != schedule && selectedDays.isEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save changes")
                }

                OutlinedButton(
                    onClick = {
                        // Restoring the last saved schedule when the owner cancels.
                        draftSchedule = schedule
                        selectedDays.clear()
                        editing = false
                        message = "Unsaved changes discarded."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel editing")
                }
            } else {
                Button(
                    onClick = {
                        draftSchedule = schedule
                        selectedDays.clear()
                        // Starting a fresh edit with the saved dates and an empty selection.
                        editing = true
                        message = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit availability")
                }
            }

            if (message.isNotEmpty()) {
                Text(message, color = MaterialTheme.colorScheme.primary)
            }

            Text("Demo only. Restarting the app or rotating the screen resets saved dates.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Draws the fixed sample month; selection is handled by the screen. */
@Composable
private fun OwnerCalendar(
    schedule: AvailabilitySchedule,
    selectedDays: List<Int>,
    editing: Boolean,
    onDateClick: (Int) -> Unit
) {
    // Aligning each weekday label with a column of the Monday-first grid.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (name in listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")) {
            Text(name, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall)
        }
    }

    // Six rows of 48 dp cells with five 4 dp gaps. May starts on Saturday.
    // Keeping the calendar at a fixed height inside the scrolling page.
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth().height(308.dp),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Making a six-week grid so each row has seven cells.
        items(42) { cell ->
            // Converting the grid index into a day number.
            val day = cell - 5 + 1
            if (day in 1..31) {
                // Passing the date’s saved status and its temporary selection as separate values.
                DayBox(
                    day = day,
                    available = day in schedule.availableDays,
                    booked = day in schedule.bookedDays,
                    selected = day in selectedDays,
                    editing = editing,
                    onClick = { onDateClick(day) }
                )
            } else {
                // Leaving the out-of-month cells empty while keeping their height aligned with the actual dates.
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

/** Shows a date's status through text and colour. Booked dates stay disabled. */
@Composable
private fun DayBox(
    day: Int,
    available: Boolean,
    booked: Boolean,
    selected: Boolean,
    editing: Boolean,
    onClick: () -> Unit
) {
    // Choosing colours from the same theme as the rest of the screen.
    val colors = MaterialTheme.colorScheme
    // Starting with the unavailable style for days outside both lists.
    var status = "Unavailable"
    var statusSymbol = "–"
    var boxColor = colors.surface
    var textColor = colors.onSurface

    // Giving a booked night its own label and colours.
    if (booked) {
        status = "Booked"
        statusSymbol = "B"
        boxColor = colors.surfaceVariant
        textColor = colors.onSurfaceVariant
    } else if (available) {
        status = "Available"
        statusSymbol = "A"
        boxColor = colors.primaryContainer
        textColor = colors.onPrimaryContainer
    }

    // Applying the check mark after setting the base status.
    if (selected) {
        statusSymbol = "✓"
        boxColor = colors.primary
        textColor = colors.onPrimary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(boxColor, RoundedCornerShape(8.dp))
            // Accepting taps only in edit mode and only for nights that are free to change.
            .clickable(enabled = editing && !booked, onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = "$day May 2027, $status"
                // Announcing the selection separately from whether the date is open, unavailable, or booked.
                stateDescription = if (selected) "Selected" else "Not selected"
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Centring the labels inside the cell so the number and symbol are easy to scan.
            Text("$day", color = textColor, fontWeight = FontWeight.SemiBold)
            Text(statusSymbol, color = textColor, fontSize = 10.sp)
        }
    }
}
