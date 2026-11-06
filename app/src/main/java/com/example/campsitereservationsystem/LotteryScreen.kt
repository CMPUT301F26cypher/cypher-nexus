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
import androidx.compose.ui.platform.LocalContext


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
    lotteryRepository: LotteryRepository,
    currentUser: User
) {
    val context = LocalContext.current

    val notificationController = remember(currentUser.id) {
        NotificationController(
            user = currentUser,
            context = context
        )
    }

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
            status = "CLOSED",
            numWinners = numLotteryWinners.toInt(),
            capacity = 0,
            startTimestamp = null,
            endTimestamp = null,
            campsite = campsite,
            campsiteID = campsite.campsiteID, //this will be the same campsiteID from campsite
            waitingList = mutableStateListOf<User>(),
            winners = mutableStateListOf<User>()
        )
    }

    var newUserForWaitingList = remember{
        User(
            id = "010100", // Device ID
            name = "Test",
            email = "Test @ test.com",
            phone = "780-test",
            notificationsEnabled = true
        )
    }

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




    if(drawLottery){
        val drawnEntrants = LotteryDraw(testUserList, newLottery, lotteryRepository, notificationController)
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
            Text("End Date (use digits)")

            FlowRow(
                modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){

                OutlinedTextField(
                    value = endMonth,
                    onValueChange = {endMonth = it},
                    label = { Text("Month") },
                    modifier = Modifier.width(90.dp)
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = {endDate = it},
                    label = { Text("Date") },
                    modifier = Modifier.width(90.dp)
                )
                OutlinedTextField(
                    value = endYear,
                    onValueChange = {endYear = it},
                    label = { Text("Year") },
                    modifier = Modifier.width(90.dp)
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
                    newLottery.status = "OPEN"
                    newLottery.numWinners = numLotteryWinners.toIntOrNull() ?: 0
                    newLottery.endTimestamp = lotteryRepository.convertToFirebaseTimestamp(
                        month = endMonth,
                        day = endDate,
                        year = endYear,
                        time = endTime
                    )

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
