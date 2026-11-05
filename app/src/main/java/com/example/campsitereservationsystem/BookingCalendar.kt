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
    schedule: AvailabilitySchedule,
    checkIn: Int?,
    checkOut: Int?,
    onDatePicked: (Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Text("May 2027", style = MaterialTheme.typography.titleMedium)

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
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(42) { square ->
            val calendarDay = square - 5 + 1

            if (calendarDay !in 1..31) {
                Spacer(modifier = Modifier.height(48.dp))
            } else {
                var boxColor = colors.surfaceVariant
                var textColor = colors.onSurfaceVariant
                var statusSymbol = "–"
                var status = "Unavailable night"

                if (calendarDay in schedule.bookedDays) {
                    statusSymbol = "B"
                    status = "Booked night"
                } else if (calendarDay in schedule.availableDays) {
                    boxColor = colors.primaryContainer
                    textColor = colors.onPrimaryContainer
                    statusSymbol = "A"
                    status = "Available night"
                }

                if (checkIn != null && checkOut != null) {
                    // Highlight overnight dates; check-out is shown separately.
                    if (calendarDay in checkIn until checkOut) {
                        boxColor = colors.primary
                        textColor = colors.onPrimary
                        statusSymbol = "✓"
                        status = "Selected night"
                    }
                }

                if (calendarDay == checkIn) {
                    boxColor = colors.primary
                    textColor = colors.onPrimary
                    statusSymbol = "IN"
                    status = "Check-in"
                } else if (calendarDay == checkOut) {
                    boxColor = colors.secondaryContainer
                    textColor = colors.onSecondaryContainer
                    statusSymbol = "OUT"
                    status = "Check-out, not an occupied night"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(boxColor, RoundedCornerShape(8.dp))
                        .clickable { onDatePicked(calendarDay) }
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

    Text("A = Available   B = Booked   – = Unavailable   IN / OUT = Your dates",
        style = MaterialTheme.typography.bodySmall)
    Text("Only overnight dates must be available. A booked date can be your check-out.",
        style = MaterialTheme.typography.bodySmall)
    Text("This demo keeps both check-in and check-out within May 2027.",
        style = MaterialTheme.typography.bodySmall)
}
