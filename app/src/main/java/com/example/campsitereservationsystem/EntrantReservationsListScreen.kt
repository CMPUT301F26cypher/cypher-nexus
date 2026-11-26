/*
 * Entrant Reservations (storyboard Frames 61–65).
 *   Frame 61: Reservations menu (Active, Past)
 *   Frame 62: Active Reservations, each with View Campsite and Cancel Reservation
 *   Frame 63: View Campsite opens the team's existing campsite details screen
 *   Frame 64: "Cancel Reservation?" popup
 *   Frame 65: Past Reservations
 */

package com.example.campsitereservationsystem

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.LocalDate

/** The three entrant pages. MENU is Frame 61. */
private enum class EntrantReservationPage { MENU, ACTIVE, PAST }

/**
 * @param user The signed-in entrant, or null if nobody is signed in.
 * @param onViewCampsite Opens the campsite details screen (Frame 63) for this campsite ID.
 * @param onBack Goes back to the Entrant Home screen (Frame 39).
 */
@Composable
fun EntrantReservationsListScreen(
    user: User?,
    onViewCampsite: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ReservationListRepository() }

    // rememberSaveable brings the user back to Active after viewing a campsite (Frame 63).
    var page by rememberSaveable { mutableStateOf(EntrantReservationPage.MENU) }
    var reservations by remember { mutableStateOf<List<ReservationInfo>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    // The reservation waiting in the "Cancel Reservation?" popup. Null = no popup.
    var toCancel by remember { mutableStateOf<ReservationInfo?>(null) }

    // Back on a list goes to the menu. Back on the menu goes to Entrant Home.
    fun goBack() {
        if (page == EntrantReservationPage.MENU) {
            onBack()
        } else {
            page = EntrantReservationPage.MENU
        }
    }
    BackHandler { goBack() }

    if (user == null || user.id.isBlank()) {
        ReservationPage(title = "Reservations", onBack = onBack) {
            Text("Sign in to see your reservations.")
        }
        return
    }

    // Loads the list when the screen opens and again after every change (refresh++).
    LaunchedEffect(user.id, refresh) {
        loading = true
        error = null
        try {
            reservations = repository.loadForEntrant(user)
        } catch (e: CancellationException) {
            throw e // the screen was closed, nothing to show
        } catch (e: Exception) {
            error = "Could not load reservations. Please go back and try again."
        }
        loading = false
    }

    val today = LocalDate.now()

    when (page) {
        // Frame 61
        EntrantReservationPage.MENU -> ReservationPage(title = "Reservations", onBack = { goBack() }) {
            Spacer(modifier = Modifier.height(80.dp))
            ReservationMenuButton("Active") { page = EntrantReservationPage.ACTIVE }
            Spacer(modifier = Modifier.height(25.dp))
            ReservationMenuButton("Past") { page = EntrantReservationPage.PAST }

            // TEMPORARY: will be deleted.
            Spacer(modifier = Modifier.height(40.dp))
            TextButton(onClick = {
                scope.launch {
                    val message = try {
                        repository.addTestReservationsForEntrant(user)
                    } catch (e: Exception) {
                        "Could not add test reservations."
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    refresh++
                }
            }) {
                Text("Add test reservations (TEMP)")
            }
        }

        // Frame 62
        EntrantReservationPage.ACTIVE -> ReservationPage(title = "Active Reservations", onBack = { goBack() }) {
            ReservationListBody(
                loading = loading,
                error = error,
                list = ReservationLists.active(reservations, today),
                emptyText = "You have no active reservations."
            ) { info ->
                ReservationCard(info = info, showEntrantName = false) {
                    ReservationSmallButton("View Campsite") { onViewCampsite(info.reservation.campsiteID) }
                    ReservationSmallButton("Cancel Reservation") { toCancel = info }
                }
            }
        }

        // Frame 65
        EntrantReservationPage.PAST -> ReservationPage(title = "Past Reservations", onBack = { goBack() }) {
            ReservationListBody(
                loading = loading,
                error = error,
                list = ReservationLists.past(reservations, today),
                emptyText = "You have no past reservations."
            ) { info ->
                ReservationCard(info = info, showEntrantName = false)
            }
        }
    }

    // Frame 64: the popup. "Cancel" cancels the reservation and frees its nights,
    // "Back" just closes the popup. Then the list reloads.
    val cancelling = toCancel
    if (cancelling != null) {
        ReservationConfirmPopup(
            title = "Cancel Reservation?",
            message = "Are you sure you want to cancel the reservation for " +
                    "${cancelling.campsiteName}? This action cannot be undone.",
            cancelText = "Back",
            confirmText = "Cancel",
            onCancel = { toCancel = null },
            onConfirm = {
                toCancel = null
                scope.launch {
                    try {
                        repository.cancel(cancelling.reservation)
                        Toast.makeText(context, "Reservation cancelled", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not cancel. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                    refresh++
                }
            }
        )
    }
}
