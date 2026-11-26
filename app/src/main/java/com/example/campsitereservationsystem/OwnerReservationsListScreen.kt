/*
 * Owner Reservations (storyboard Frames 33–37).
 *   Frame 33: Reservations menu (Active, Past, Cancelled)
 *   Frame 34: Active Reservations, each with a Reject button
 *   Frame 35: "Reject Campsite?" popup
 *   Frame 36: Past Reservations
 *   Frame 37: Cancelled Reservations (cancelled by the entrant or rejected by the owner)
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

/** The four owner pages. MENU is Frame 33. */
private enum class OwnerReservationPage { MENU, ACTIVE, PAST, CANCELLED }

/**
 * @param user The signed-in owner, or null if nobody is signed in.
 * @param onBack Goes back to the Owner Home screen (Frame 13).
 */
@Composable
fun OwnerReservationsListScreen(
    user: User?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ReservationListRepository() }

    // rememberSaveable keeps the page when the user comes back to this screen.
    var page by rememberSaveable { mutableStateOf(OwnerReservationPage.MENU) }
    var reservations by remember { mutableStateOf<List<ReservationInfo>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    // The reservation waiting in the "Reject Campsite?" popup. Null = no popup.
    var toReject by remember { mutableStateOf<ReservationInfo?>(null) }

    // Back on a list goes to the menu. Back on the menu goes to Owner Home.
    fun goBack() {
        if (page == OwnerReservationPage.MENU) {
            onBack()
        } else {
            page = OwnerReservationPage.MENU
        }
    }
    BackHandler { goBack() }

    if (user == null || Role.OWNER !in user.roles) {
        ReservationPage(title = "Reservations", onBack = onBack) {
            Text("Sign in as a campsite owner to see your reservations.")
        }
        return
    }

    // Loads the list when the screen opens and again after every change (refresh++).
    LaunchedEffect(user.id, refresh) {
        loading = true
        error = null
        try {
            reservations = repository.loadForOwner(user)
        } catch (e: CancellationException) {
            throw e // the screen was closed, nothing to show
        } catch (e: Exception) {
            error = "Could not load reservations. Please go back and try again."
        }
        loading = false
    }

    val today = LocalDate.now()

    when (page) {
        // Frame 33
        OwnerReservationPage.MENU -> ReservationPage(title = "Reservations", onBack = { goBack() }) {
            Spacer(modifier = Modifier.height(60.dp))
            ReservationMenuButton("Active") { page = OwnerReservationPage.ACTIVE }
            Spacer(modifier = Modifier.height(25.dp))
            ReservationMenuButton("Past") { page = OwnerReservationPage.PAST }
            Spacer(modifier = Modifier.height(25.dp))
            ReservationMenuButton("Cancelled") { page = OwnerReservationPage.CANCELLED }

            // TEMPORARY: will be deleted.
            Spacer(modifier = Modifier.height(40.dp))
            TextButton(onClick = {
                scope.launch {
                    val message = try {
                        repository.addTestReservationsForOwner(user)
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

        // Frame 34
        OwnerReservationPage.ACTIVE -> ReservationPage(title = "Active Reservations", onBack = { goBack() }) {
            ReservationListBody(
                loading = loading,
                error = error,
                list = ReservationLists.active(reservations, today),
                emptyText = "No active reservations."
            ) { info ->
                ReservationCard(info = info, showEntrantName = true) {
                    ReservationSmallButton("Reject") { toReject = info }
                }
            }
        }

        // Frame 36
        OwnerReservationPage.PAST -> ReservationPage(title = "Past Reservations", onBack = { goBack() }) {
            ReservationListBody(
                loading = loading,
                error = error,
                list = ReservationLists.past(reservations, today),
                emptyText = "No past reservations."
            ) { info ->
                ReservationCard(info = info, showEntrantName = true)
            }
        }

        // Frame 37
        OwnerReservationPage.CANCELLED -> ReservationPage(title = "Cancelled Reservations", onBack = { goBack() }) {
            ReservationListBody(
                loading = loading,
                error = error,
                list = ReservationLists.cancelled(reservations),
                emptyText = "No cancelled reservations."
            ) { info ->
                ReservationCard(info = info, showEntrantName = true)
            }
        }
    }

    // Frame 35: the popup. Reject cancels the reservation, frees its nights,
    // and sends the entrant a notification. Then the list reloads.
    val rejecting = toReject
    if (rejecting != null) {
        ReservationConfirmPopup(
            title = "Reject Campsite?",
            message = "Are you sure you want to reject the reservation for " +
                    "${rejecting.campsiteName} from ${rejecting.entrantName}? " +
                    "This action cannot be undone.",
            cancelText = "Cancel",
            confirmText = "Reject",
            onCancel = { toReject = null },
            onConfirm = {
                toReject = null
                scope.launch {
                    try {
                        repository.cancel(rejecting.reservation)
                        repository.tellEntrantAboutReject(user, rejecting)
                        Toast.makeText(context, "Reservation rejected", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not reject. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                    refresh++
                }
            }
        )
    }
}
