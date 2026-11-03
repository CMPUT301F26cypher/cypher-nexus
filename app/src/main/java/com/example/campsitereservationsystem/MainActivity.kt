package com.example.campsitereservationsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.widget.Toast
import com.example.campsitereservationsystem.ui.theme.CampsiteReservationSystemTheme

/** Opens the welcome page and keeps the existing feature test menu reachable. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CampsiteReservationSystemTheme {
                var showTestMenu by remember { mutableStateOf(false) }
                var showAvailability by remember { mutableStateOf(false) }

                // Keep saved dates here while moving between welcome and the calendar.
                var savedSchedule by remember { mutableStateOf(AvailabilitySchedule()) }

                if (showAvailability) {
                    AvailabilityScreen(
                        schedule = savedSchedule,
                        onSave = { savedSchedule = it },
                        onBack = { showAvailability = false }
                    )
                } else if (showTestMenu) {
                    MainMenuManager(onBack = { showTestMenu = false })
                } else {
                    WelcomeScreen(
                        onFindCampsite = {
                            Toast.makeText(this, "The entrant page is not connected yet.",
                                Toast.LENGTH_SHORT).show()
                        },
                        onManageCampsites = {
                            showAvailability = true
                        },
                        onPolicyClick = {
                            Toast.makeText(this, "The policy page is not connected yet.",
                                Toast.LENGTH_SHORT).show()
                        },
                        onOpenTestMenu = { showTestMenu = true }
                    )
                }
            }
        }
    }
}
/*
The main menu, if need be later we modify how exactly this works to account for different user types
 */
@Composable
fun MainMenuManager(onBack: () -> Unit){
    var showLottery by remember { mutableStateOf(false) }
    var showImageUpload by remember {mutableStateOf(false)}
    var lotteryTestArray1 = remember {mutableStateListOf(
        "John Man", "Jane Woman", "Petey Person", "Bob", "Brad", "Billy", "Bartolomeu",
        "Barry", "Terry", "Tom", "Timmy", "Jimmy", "Jack", "Robert"
    )}
    var lotteryTestArray2 = remember {mutableStateListOf(
        "John Man", "Jane Woman", "Petey Person"
    )}

    BackHandler {
        if (showLottery) {
            showLottery = false
        } else if (showImageUpload) {
            showImageUpload = false
        } else {
            onBack()
        }
    }

    //doing it this way avoid the "calling a composable function from
    // outside a composable function" error
    if (showLottery) {
        LotteryScreen(
            onBack = {showLottery = false},
            lotteryTestArray2
        )
    }else if (showImageUpload){
        ImageUploadTestScreen(
            onBack = {showImageUpload = false}
        )
    }else {
        //Lottery wip button
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    showLottery = true
                }
            ) {
                Text("Lottery WIP")
            }
            //IMAGE UPLOAD BUTTON
            Button( //Just for testing purposes rn
                onClick = {
                    showImageUpload = true
                }
            ) {
                Text("Image Upload Test Screen")
            }
            Button(onClick = onBack) {
                Text("Back to welcome")
            }
        }
    }
}
