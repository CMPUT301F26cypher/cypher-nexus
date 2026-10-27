package com.example.campsitereservationsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun LotteryScreen(onBack: () -> Unit, entrants: List<String>) {
    var drawLottery by remember {mutableStateOf(false)}

    if(drawLottery){
        val drawnEntrants = LotteryDraw(entrants)
        Column (
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text(drawnEntrants.joinToString(separator = ", "))
        }
        Button(
            onClick = {
                drawLottery = false
            }
        ){
            Text("Continue")
        }
    }else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //will need to let owner modify lottery draw amount
            Text("Lottery Screen!!!")

            Button(onClick = onBack) {
                Text("Back")
            }

            Button(
                onClick = {
                    drawLottery = true
                }
            ) {
                Text("Run lottery right now")
            }
        }
    }
}
