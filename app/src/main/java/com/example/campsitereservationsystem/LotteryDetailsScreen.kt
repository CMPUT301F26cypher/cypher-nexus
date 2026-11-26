// Showing the Lottery Details form (storyboard Frames 24-25, and 31-32 when updating) for the registration and booking periods (US 02.04.03, 02.04.04, 02.04.06).

package com.example.campsitereservationsystem

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.campsitereservationsystem.OwnerCampsitesPath.Campsite
import java.time.LocalDateTime

/**
 * Lottery Details form from the storyboard (Frame 24), with the error popup from Frame 25.
 * It has the registration fields (US 02.04.03) and the booking fields (US 02.04.04).
 * If the campsite already has a lottery, the saved values are shown so the owner can
 * update them (US 02.04.06, Frames 31-32).
 *
 * Outstanding issues:
 * - Number of Winners and Capacity (US 02.04.05, US 02.04.07) are not on this form yet.
 * - Save and Cancel go back one screen. Once My Lotteries (Frame 20) and
 *   Pick a Campsite (Frame 21) exist, they should go to those screens as the storyboard says.
 *
 * @param campsite The campsite this lottery is for.
 * @param repository Loads and saves the form in Firestore.
 * @param onSaved Called after the lottery is saved.
 * @param onCancel Called when the owner presses Cancel or Back.
 */
@Composable
fun LotteryDetailsScreen(
    campsite: Campsite,
    repository: LotteryDetailsRepository,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    // What the owner has typed so far. Each box updates one field of one period.
    var registration by remember { mutableStateOf(LotteryPeriod()) }
    var booking by remember { mutableStateOf(LotteryPeriod()) }

    // True while the saved lottery is being loaded from Firestore.
    var loading by remember { mutableStateOf(true) }
    // True when this campsite already had a lottery, so we are updating it (US 02.04.06).
    var isUpdate by remember { mutableStateOf(false) }

    // When this is not empty, the Frame 25 error popup is shown with this message.
    var errorMessage by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    // Loading the saved lottery once, when the screen opens (Frame 31: "current values should be shown").
    LaunchedEffect(campsite.id) {
        repository.loadLotteryDetails(
            campsiteId = campsite.id,
            onLoaded = { saved ->
                if (saved != null) {
                    registration = saved.registration
                    booking = saved.booking
                    isUpdate = true
                }
                loading = false
            },
            onError = {
                errorMessage = "The saved lottery could not be loaded. Check your connection and try again."
                loading = false
            }
        )
    }

    // Checking the form, then saving it.
    fun saveLottery() {
        val details = LotteryDetails(registration = registration, booking = booking)
        val problem = details.problem(LocalDateTime.now(), isUpdate)
        if (problem.isNotEmpty()) {
            errorMessage = problem
            return
        }

        saving = true
        repository.saveLotteryDetails(campsite.id, details) { saved ->
            saving = false
            if (saved) {
                val message = if (isUpdate) "Lottery updated" else "Lottery saved"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                onSaved()
            } else {
                errorMessage = "The lottery could not be saved. Check your connection and try again."
            }
        }
    }

    BackHandler(onBack = onCancel)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Lottery Details",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = campsite.name,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Waiting for Firestore, so saved values are not overwritten by what the owner types.
        if (loading) {
            Text("Loading lottery…", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        } else {
            LotteryFields(
                registration = registration,
                booking = booking,
                onRegistrationChange = { registration = it },
                onBookingChange = { booking = it }
            )
        }

        Button(
            onClick = { saveLottery() },
            enabled = !loading && !saving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (saving) "Saving…" else "Save Lottery")
        }
        OutlinedButton(
            onClick = onCancel,
            enabled = !saving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }

    // Frame 25: the error popup. The rest of the screen cannot be used until OK is pressed.
    if (errorMessage.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { errorMessage = "" },
            properties = DialogProperties(dismissOnClickOutside = false),
            title = { Text("An Error was encountered!") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { errorMessage = "" }) {
                    Text("OK")
                }
            }
        )
    }
}

/** The eight boxes of the form, with the same labels and order as Frame 24. */
@Composable
private fun LotteryFields(
    registration: LotteryPeriod,
    booking: LotteryPeriod,
    onRegistrationChange: (LotteryPeriod) -> Unit,
    onBookingChange: (LotteryPeriod) -> Unit
) {
    // Registration fields (US 02.04.03).
    LotteryField("Registration Start Date (MM/DD/YYYY)", registration.startDate) {
        onRegistrationChange(registration.copy(startDate = it))
    }
    LotteryField("Registration Start Time (24 hour clock)", registration.startTime) {
        onRegistrationChange(registration.copy(startTime = it))
    }
    LotteryField("Registration End Date (MM/DD/YYYY)", registration.endDate) {
        onRegistrationChange(registration.copy(endDate = it))
    }
    LotteryField("Registration End Time (24 hour clock)", registration.endTime) {
        onRegistrationChange(registration.copy(endTime = it))
    }

    // A small gap between the two groups, like Frame 24.
    Spacer(modifier = Modifier.height(4.dp))

    // Booking fields (US 02.04.04).
    LotteryField("Booking Start Date (MM/DD/YYYY)", booking.startDate) {
        onBookingChange(booking.copy(startDate = it))
    }
    LotteryField("Booking Start Time (24 hour clock)", booking.startTime) {
        onBookingChange(booking.copy(startTime = it))
    }
    LotteryField("Booking End Date (MM/DD/YYYY)", booking.endDate) {
        onBookingChange(booking.copy(endDate = it))
    }
    LotteryField("Booking End Time (24 hour clock)", booking.endTime) {
        onBookingChange(booking.copy(endTime = it))
    }
}

/** One text box on the Lottery Details form. */
@Composable
private fun LotteryField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
