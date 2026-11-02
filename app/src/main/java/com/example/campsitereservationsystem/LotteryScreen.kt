package com.example.campsitereservationsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/*
Manages the Screen for Lottery
Allows Owners to setup a lottery. Determining amount of lottery winners and
how long it's open. After the lottery registration closes winners are
automatically randomly selected and winners and losers are notified on their result
via a push notification
 */
@Composable
fun LotteryScreen(onBack: () -> Unit, entrants: List<String>) {
    var drawLottery by remember {mutableStateOf(false)}
    var numLotteryWinners by remember {mutableStateOf("0")}

    if(drawLottery){
        val drawnEntrants = LotteryDraw(entrants, numLotteryWinners)
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
            OutlinedTextField(
                value = numLotteryWinners,
                onValueChange = {numLotteryWinners = it},
                label = { Text("How many lottery winners do you want?") }
            )

            Button(
                onClick = {
                    drawLottery = true
                }
            ) {
                Text("Run lottery right now")
            }

            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }
}
