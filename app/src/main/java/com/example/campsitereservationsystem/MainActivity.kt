package com.example.campsitereservationsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.campsitereservationsystem.ui.theme.CampsiteReservationSystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampsiteReservationSystemTheme {
               MainMenuManager()
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
    } else {
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
        }
    }
}
