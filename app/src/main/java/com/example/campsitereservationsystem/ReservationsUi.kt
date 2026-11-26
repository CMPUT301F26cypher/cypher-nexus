/*
 * Shared look for the Reservations screens, copied from the storyboard:
 * Used by OwnerReservationsListScreen (Frames 33–37)
 * and EntrantReservationsListScreen (Frames 61–65).
 */

package com.example.campsitereservationsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

// Colours taken from the storyboard frames.
private val ReservationGreen = Color(0xFF819F5C)
private val ReservationCardGrey = Color(0xFF676564)

/**
 * The layout every Reservations frame uses.
 *
 * @param title Text at the top, for example "Active Reservations".
 * @param onBack Called by the small Back button.
 * @param content Whatever goes under the title (menu buttons or the list of cards).
 */
@Composable
fun ReservationPage(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp) // leaves room for the Back button
        ) {
            Text(
                text = title,
                fontSize = 30.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }

        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.BottomStart),
            colors = ButtonDefaults.buttonColors(containerColor = ReservationGreen)
        ) {
            Text(text = "Back", color = Color.Black)
        }
    }
}

/** Frames 33 and 61: the big green Active / Past / Cancelled buttons. */
@Composable
fun ReservationMenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = ReservationGreen),
        shape = RoundedCornerShape(5.dp),
        modifier = Modifier.size(width = 240.dp, height = 60.dp)
    ) {
        Text(text = text, color = Color.Black, fontSize = 25.sp)
    }
}

/** The small green buttons on a card: Reject, View Campsite, Cancel Reservation. */
@Composable
fun ReservationSmallButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = ReservationGreen),
        shape = RoundedCornerShape(5.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = Color.Black, fontSize = 14.sp)
    }
}

/**
 * One grey card: campsite name, entrant name (owner side only),
 * location and booked dates, with optional buttons underneath.
 */
@Composable
fun ReservationCard(
    info: ReservationInfo,
    showEntrantName: Boolean,
    buttons: @Composable RowScope.() -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = ReservationCardGrey,
            contentColor = Color.Black
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = info.campsiteName, fontSize = 20.sp)
            if (showEntrantName) {
                Text(text = info.entrantName, fontSize = 13.sp)
            }
            Text(text = info.location.ifBlank { "No location given" }, fontSize = 13.sp)
            Text(text = ReservationLists.bookedDates(info.reservation), fontSize = 13.sp)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                buttons()
            }
        }
    }
}

/**
 * Shows one of four things: "Loading...", an error, an empty message, or the cards.
 *
 * @param card How to draw one reservation (so owner and entrant can add their own buttons).
 */
@Composable
fun ReservationListBody(
    loading: Boolean,
    error: String?,
    list: List<ReservationInfo>,
    emptyText: String,
    card: @Composable (ReservationInfo) -> Unit
) {
    if (loading) {
        Text("Loading reservations...")
        return
    }
    if (error != null) {
        Text(text = error, color = Color.Red)
        return
    }
    if (list.isEmpty()) {
        Text(emptyText)
        return
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(list, key = { it.reservation.reservationID }) { info ->
            card(info)
        }
    }
}

/**
 * Frames 35 and 64: a yes/no popup.
 * Tapping outside does nothing, so the user has to pick one of the two buttons.
 */
@Composable
fun ReservationConfirmPopup(
    title: String,
    message: String,
    cancelText: String,
    confirmText: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(dismissOnClickOutside = false),
        containerColor = Color.White,
        title = { Text(text = title, color = Color.Black) },
        text = { Text(text = message, color = Color.Black) },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = cancelText, color = Color.Black)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText, color = Color.Black)
            }
        }
    )
}
