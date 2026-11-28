// Showing the Lottery Details form (storyboard Frames 24-25, and 31-32 when updating) for the registration and booking periods (US 02.04.03, 02.04.04, 02.04.06).

package com.example.campsitereservationsystem

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.campsitereservationsystem.OwnerCampsitesPath.Campsite
import com.google.firebase.Timestamp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.time.*

/**
 * modified version of Arpit's LotteryDetailsScreen, will remove LotteryDetails
 * and such files when closer to deadline
 */


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
fun OwnerAddLotteryScreen(
    campsite: Campsite,
    lotteryRepository: LotteryRepository,
    repository: LotteryDetailsRepository,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // What the owner has typed so far. Each box updates one field of one period.
    var registration by remember { mutableStateOf(LotteryPeriod()) }
    var booking by remember { mutableStateOf(LotteryPeriod()) }
    var numWinners by remember { mutableStateOf("0") }
    var capacity by remember { mutableStateOf("0") }

    // True while the saved lottery is being loaded from Firestore.
    var loading by remember { mutableStateOf(true) }
    // True when this campsite already had a lottery, so we are updating it (US 02.04.06).
    var isUpdate by remember { mutableStateOf(false) }

    // When this is not empty, the Frame 25 error popup is shown with this message.
    var errorMessage by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    // TEST DATA FROM LOTTERY SCREEN

    /*
    var testUserList = remember{
        listOf(
            User(
                id = "010101", // Device ID
                name = "John Test",
                email = "John Test @ test.com",
                phone = "780-testnumber",
                notificationsEnabled = true
            ),
            User(
                id = "010102",
                name = "Jane Test",
                email = "Jane Test @ test.com",
                phone = "780-testnumber2",
                notificationsEnabled = true
            ),
            User(
                id = "010103",
                name = "Bob Test",
                email = "Bob Test @ test.com",
                phone = "780-testnumber3",
                notificationsEnabled = false
            ),
            User(
                id = "010104",
                name = "Alice Johnson",
                email = "alice.johnson@test.com",
                phone = "780-555-0104",
                notificationsEnabled = true
            ),
            User(
                id = "010105",
                name = "Michael Smith",
                email = "michael.smith@test.com",
                phone = "780-555-0105",
                notificationsEnabled = false
            ),
            User(
                id = "010106",
                name = "Sarah Williams",
                email = "sarah.williams@test.com",
                phone = "780-555-0106",
                notificationsEnabled = true
            ),
            User(
                id = "010107",
                name = "David Brown",
                email = "david.brown@test.com",
                phone = "780-555-0107",
                notificationsEnabled = true
            ),
            User(
                id = "010108",
                name = "Emily Davis",
                email = "emily.davis@test.com",
                phone = "780-555-0108",
                notificationsEnabled = false
            ),
            User(
                id = "010109",
                name = "James Wilson",
                email = "james.wilson@test.com",
                phone = "780-555-0109",
                notificationsEnabled = true
            ),
            User(
                id = "010110",
                name = "Olivia Martinez",
                email = "olivia.martinez@test.com",
                phone = "780-555-0110",
                notificationsEnabled = false
            ),
            User(
                id = "010111",
                name = "Daniel Anderson",
                email = "daniel.anderson@test.com",
                phone = "780-555-0111",
                notificationsEnabled = true
            ),
            User(
                id = "010112",
                name = "Sophia Taylor",
                email = "sophia.taylor@test.com",
                phone = "780-555-0112",
                notificationsEnabled = true
            ),
            User(
                id = "010113",
                name = "Matthew Thomas",
                email = "matthew.thomas@test.com",
                phone = "780-555-0113",
                notificationsEnabled = false
            )
        )
    }

     */

    /*
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
    */


    //***********NEED TO CHANGE THIS, this is the main thing to change I believe
    // as this is where saving happens so as long as I can add in the two new fields
    // and make sure its saving to Lottery not lotteryDetails, should be good

    //user LotteryPeriods, .toDateTime to turn the Lottery periods into timestamps

    // Checking the form, then saving it.
    fun saveLottery() {

        //makes a LotteryDetails object so its "problems" can be checked
        val details = LotteryDetails(registration = registration, booking = booking)
        val problem = details.problem(LocalDateTime.now(), isUpdate)
        if (problem.isNotEmpty()) {
            errorMessage = problem
            return
        }

        val newLottery = Lottery(
            status = "CLOSED",
            numWinners = numWinners.toIntOrNull() ?: 0,
            capacity = capacity.toIntOrNull() ?: 0,
            registrationStartTimestamp = registration.startTimestamp(),
            registrationEndTimestamp = registration.endTimestamp(),
            bookingStartTimestamp = booking.startTimestamp(),
            bookingEndTimestamp = booking.endTimestamp(),
            campsite = campsite,
            campsiteID = campsite.id,
            waitingList = mutableStateListOf<User>(),
            winners = mutableStateListOf<User>()
        )

        //is this line needed? Should just be here for testing.
        newLottery.status = "OPEN"

        //this line is to avoid issues with possible null values while doing LotteryWorker
        val registrationEnd = newLottery.registrationEndTimestamp

        if (registrationEnd != null) {

            lotteryRepository.addLottery(newLottery) {

                // Schedule automatic lottery draw
                lotteryRepository.scheduleLotteryWorker(
                    context = context,
                    campsiteID = newLottery.campsiteID,
                    endTime = registrationEnd.toDate().time
                )
            }
        }

        /*
        //this is just here to test if the lotteries will be automatically
        //drawn after a certain amount of time has passed
        //it adds the testUserList to the lotteries waiting list
        coroutineScope.launch {
            lotteryRepository.updateList(
                newLottery,
                testUserList,
                "waitingList"
            )
        }
         */
        onSaved()
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

        LotteryFields(
            registration = registration,
            booking = booking,
            numWinners = numWinners,
            capacity = capacity,
            onRegistrationChange = { registration = it },
            onBookingChange = { booking = it },
            onNumWinnersChange = { numWinners = it },
            onCapacityChange = { capacity = it }
        )

        Button(
            onClick = { saveLottery() },
            //enabled = !loading && !saving,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF819F5C)
            )

        ) {
            Text(
                text = if (saving) "Saving…" else "Save Lottery",
                color = Color.Black
            )
        }
        OutlinedButton(
            onClick = onCancel,
            enabled = !saving,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(
                width = 1.dp,
                color = Color(0xFF819F5C)
            )
        ) {
            Text(
                text = "Cancel",
                color = Color.Black
            )
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
    numWinners: String,
    capacity: String,
    onRegistrationChange: (LotteryPeriod) -> Unit,
    onBookingChange: (LotteryPeriod) -> Unit,
    onNumWinnersChange: (String) -> Unit,
    onCapacityChange: (String) -> Unit
) {
    LotteryField("Number of Winners", numWinners) {
        onNumWinnersChange(it)
    }

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
    LotteryField("Capacity (Optional)", capacity) {
        onCapacityChange(it)
    }

}

/**
 * Checks the whole form. The message is shown in the Frame 25 error popup.
 *
 * Rules, in the order they are checked:
 * 1. The registration period is filled in correctly.
 * 2. The booking period is filled in correctly.
 * 3. Creating only: registration ends in the future.
 *    (When updating, registration may already be over, for example when the owner
 *    only wants to move the booking end date, as in storyboard Frame 32.)
 * 4. Booking starts after registration ends (winners are drawn when registration ends).
 * 5. Booking ends in the future.
 *
 * @param now The current date and time.
 * @param isUpdate True when the owner is changing a lottery that is already saved (US 02.04.06).
 * @return An empty string if everything is fine, or a message explaining what to fix.
 */
fun problem(now: LocalDateTime, isUpdate: Boolean = false,registration: LotteryPeriod,
            booking: LotteryPeriod,): String {
    val registrationProblem = registration.problem("Registration")
    if (registrationProblem.isNotEmpty()) {
        return registrationProblem
    }

    val bookingProblem = booking.problem("Booking")
    if (bookingProblem.isNotEmpty()) {
        return bookingProblem
    }

    val registrationEnd = registration.end()
    val bookingStart = booking.start()
    val bookingEnd = booking.end()

    // Safety net: both periods were checked above, so this should never happen.
    if (registrationEnd == null || bookingStart == null || bookingEnd == null) {
        return "Please check the dates and times."
    }

    if (!isUpdate && !registrationEnd.isAfter(now)) {
        return "Registration end must be in the future."
    }
    if (bookingStart.isBefore(registrationEnd)) {
        return "Booking must start after registration ends."
    }
    if (!bookingEnd.isAfter(now)) {
        return "Booking end must be in the future."
    }
    return ""
}

/** One text box on the Lottery Details form. */
@Composable
private fun LotteryField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF819F5C),
            unfocusedBorderColor = Color(0xFF819F5C),
            focusedLabelColor = Color(0xFF819F5C)
        )
    )
}
