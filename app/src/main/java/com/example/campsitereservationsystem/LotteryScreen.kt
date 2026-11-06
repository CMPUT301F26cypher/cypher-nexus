package com.example.campsitereservationsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import kotlin.String
import com.example.campsitereservationsystem.campsite.Campsite

/*
Manages the Screen for Lottery
Allows Owners to setup a lottery. Determining amount of lottery winners and
how long it's open. After the lottery registration closes winners are
automatically randomly selected and winners and losers are notified on their result
via a push notification
 */
@Composable
fun LotteryScreen(
    onBack: () -> Unit,
    entrants: List<String>,
    campsite: Campsite,
    lotteryRepository: LotteryRepository
) {
    var drawLottery by remember {mutableStateOf(false)}
    var numLotteryWinners by remember {mutableStateOf("0")}
    var startDate by remember {mutableStateOf("")}
    //var endDate by remember {mutableStateOf("")}

    var endMonth by remember {mutableStateOf("")}
    var endDate by remember {mutableStateOf("")}
    var endYear by remember {mutableStateOf("")}
    var endTime by remember {mutableStateOf("")}

    //=============
    //TEST DATA
    //============
    var newLottery = remember {
        Lottery(
            open = true,
            numWinners = numLotteryWinners.toInt(),
            capacity = 0,
            startDate = "Current Day",
            endMonth = endMonth,
            endDate = endDate,
            endYear = endYear,
            endTime = endTime,
            campsite = campsite,
            campsiteID = campsite.id, //this will be the same campsiteID from campsite
            waitingList = mutableStateListOf<User>(),
            winners = mutableStateListOf<User>()
        )
    }

    var newUserForWaitingList = remember {
        User(
            id = "010101", // Device ID
            name = "John Test",
            email = "John Test @ test.com",
            phone = "780-testnumber",
            notificationsEnabled = true
        )
    }



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

            Spacer(modifier = Modifier.width(16.dp))

            FlowRow(
                modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){
                Text("End Date")

                OutlinedTextField(
                    value = endMonth,
                    onValueChange = {endMonth = it},
                    label = { Text("Month") }
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = {endDate = it},
                    label = { Text("Date") }
                )
                OutlinedTextField(
                    value = endYear,
                    onValueChange = {endYear = it},
                    label = { Text("Year") }
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = {endTime = it},
                    label = { Text("Time in 24 hour clock") }
                )
            }

            Button(
                onClick = {
                    drawLottery = true
                }
            ) {
                Text("Run lottery right now")
            }

            Button(
                onClick = {
                    newLottery.numWinners = numLotteryWinners.toIntOrNull() ?: 0
                    newLottery.endMonth = endMonth
                    newLottery.endDate = endDate
                    newLottery.endYear = endYear
                    newLottery.endTime = endTime

                    lotteryRepository.addLottery(newLottery)
                }
            ) {
                Text("Add lottery to firebase")
            }
            //this button is here to test adding users to a waitinglist on firebase
            Button(
                onClick = {
                    newLottery.waitingList.add(newUserForWaitingList)
                    lotteryRepository.updateList(
                        newLottery,
                        newLottery.waitingList,
                        "waitingList")
                }
            ) {
                Text("Add user to waiting list on firebase")
            }

            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }
}
