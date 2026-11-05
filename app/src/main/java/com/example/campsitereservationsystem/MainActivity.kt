package com.example.campsitereservationsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.example.campsitereservationsystem.ui.theme.CampsiteReservationSystemTheme

class MainActivity : ComponentActivity() {

    // private val repo = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampsiteReservationSystemTheme {
               TestApp()
            }
        }
    }
}

/*
The main menu, if need be later we modify how exactly this works to account for different user types
 */
@Composable
fun MainMenuManager(){
    var showLottery by remember { mutableStateOf(false) }
    var showImageUpload by remember {mutableStateOf(false)}
    var lotteryTestArray1 = remember {mutableStateListOf(
        "John Man", "Jane Woman", "Petey Person", "Bob", "Brad", "Billy", "Bartolomeu",
        "Barry", "Terry", "Tom", "Timmy", "Jimmy", "Jack", "Robert"
    )}
    var lotteryTestArray2 = remember {mutableStateListOf(
        "John Man", "Jane Woman", "Petey Person"
    )}

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
        }
    }
}

// For testing the create profile screen and editing the profile details
@Composable
fun TestApp() {
    var user by remember { mutableStateOf<User?>(null) } // null meaning no profile yet
    var showProfile by remember { mutableStateOf(false) }
    val current = user

    when {
        //
        current == null -> CreateAccountScreen { name, email, phone ->
            user = User(id = "test-device", name = name, email = email, phone = phone)
        }

        showProfile -> ProfileScreen(
            user = current,
            onSave = { updated -> user = updated },
            onDelete = { user = null; showProfile = false },
            onBack = { showProfile = false }
        )

        else -> Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome, ${current.name}")
            Text("Email: ${current.email.ifBlank { "(none)" }}")
            Text("Phone: ${current.phone.ifBlank { "(none)" }}")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showProfile = true }) { Text("View Profile") }
        }
    }
}

