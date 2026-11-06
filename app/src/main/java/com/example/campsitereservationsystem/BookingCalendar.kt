// Showing the May 2027 calendar and passing the entrant’s date choices to the booking screen.

package com.example.campsitereservationsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Read-only availability calendar with check-in/check-out selection.
 * The month matches the current AvailabilitySchedule model: May 2027.
 */
@Composable
fun BookingCalendar(
    // Receiving the latest schedule and both selected dates from the parent.
    schedule: AvailabilitySchedule,
    checkIn: Int?,
    checkOut: Int?,
    onDatePicked: (Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Text("May 2027", style = MaterialTheme.typography.titleMedium)

    // Lining up the weekday labels with the seven calendar columns.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (name in listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")) {
            Text(
                name,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }

    // May starts on Saturday.
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth().height(308.dp),
        // Giving vertical scrolling to the booking page.
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(42) { square ->
            val calendarDay = square - 5 + 1

            if (calendarDay !in 1..31) {
                // Filling the unused slots with a spacer instead of making them clickable.
                Spacer(modifier = Modifier.height(48.dp))
            } else {
                // Starting each valid date with the unavailable style.
                var boxColor = colors.surfaceVariant
                var textColor = colors.onSurfaceVariant
                var statusSymbol = "–"
                var status = "Unavailable night"

                // Showing the schedule’s existing bookings before applying the entrant’s highlights.
                if (calendarDay in schedule.bookedDays) {
                    statusSymbol = "B"
                    status = "Booked night"
                } else if (calendarDay in schedule.availableDays) {
                    boxColor = colors.primaryContainer
                    textColor = colors.onPrimaryContainer
                    statusSymbol = "A"
                    status = "Available night"
                }

                // Highlighting a range only after both ends have been chosen.
                if (checkIn != null && checkOut != null) {
                    // Highlight overnight dates; check-out is shown separately.
                    if (calendarDay in checkIn until checkOut) {
                        boxColor = colors.primary
                        textColor = colors.onPrimary
                        statusSymbol = "✓"
                        status = "Selected night"
                    }
                }

                // Giving the two ends distinct markers after colouring the range.
                if (calendarDay == checkIn) {
                    boxColor = colors.primary
                    textColor = colors.onPrimary
                    statusSymbol = "IN"
                    status = "Check-in"
                } else if (calendarDay == checkOut) {
                    boxColor = colors.secondaryContainer
                    textColor = colors.onSecondaryContainer
                    // Marking check-out separately because it does not occupy that night.
                    statusSymbol = "OUT"
                    status = "Check-out, not an occupied night"
                }

                // Keeping each date as one tap target with a consistent height.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(boxColor, RoundedCornerShape(8.dp))
                        // Passing each tap back so the booking screen can validate the chosen day.
                        .clickable { onDatePicked(calendarDay) }
                        // Announcing the day and its status together for screen readers.
                        .semantics(mergeDescendants = true) {
                            contentDescription = "$calendarDay May 2027, $status"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$calendarDay", color = textColor, fontWeight = FontWeight.SemiBold)
                        Text(statusSymbol, color = textColor, fontSize = 10.sp)
                    }
                }
            }
        }
    }

    // Explaining the date symbols below the grid.
    Text("A = Available   B = Booked   – = Unavailable   IN / OUT = Your dates",
        style = MaterialTheme.typography.bodySmall)
    Text("Only overnight dates must be available. A booked date can be your check-out.",
        style = MaterialTheme.typography.bodySmall)
    Text("This demo keeps both check-in and check-out within May 2027.",
        style = MaterialTheme.typography.bodySmall)
}
