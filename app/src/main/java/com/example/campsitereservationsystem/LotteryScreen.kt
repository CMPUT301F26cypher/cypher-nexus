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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import kotlin.String
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog


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

    //var drawLottery by remember {mutableStateOf(false)}

    //ERROR MESSAGES
    var showDateError by remember { mutableStateOf(false) }
    var showLotteryError by remember { mutableStateOf(false) }

    var numLotteryWinners by remember {mutableStateOf("0")}


    var startMonth by remember {mutableStateOf("")}
    var startDate by remember {mutableStateOf("")}
    var startYear by remember {mutableStateOf("")}
    var startTime by remember {mutableStateOf("")}

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
    //Error popup on incorrect lottery dates
    if (showDateError) {
        AlertDialog(
            onDismissRequest = {
                showDateError = false
            },
            title = {
                Text("Invalid Dates")
            },
            text = {
                Text("The start date must be before the end date. Please correct your dates.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDateError = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }else if (showLotteryError) {
        AlertDialog(
            onDismissRequest = {
                showLotteryError = false
            },
            title = {
                Text("Lottery cannot be run")
            },
            text = {
                Text("This lottery has either not been started, or is already being drawn/completed")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLotteryError = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    //displays lottery results, only here for testing purposes
    /*
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

     */
   //}else {
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
            Text("Start Date (use digits)")
            Spacer(modifier = Modifier.width(16.dp))

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){

                OutlinedTextField(
                    value = startMonth,
                    onValueChange = {startMonth = it},
                    label = { Text("Month") },
                    modifier = Modifier.width(90.dp)
                )
                OutlinedTextField(
                    value = startDate,
                    onValueChange = {startDate = it},
                    label = { Text("Date") },
                    modifier = Modifier.width(90.dp)
                )
                OutlinedTextField(
                    value = startYear,
                    onValueChange = {startYear = it},
                    label = { Text("Year") },
                    modifier = Modifier.width(90.dp)
                )
                OutlinedTextField(
                    value = startTime,
                    onValueChange = {startTime = it},
                    label = { Text("Time in 24 hour clock") },
                    modifier = Modifier.width(150.dp)

                )
            }

            Spacer(modifier = Modifier.width(16.dp))
            Text("End Date (use digits)")
            Spacer(modifier = Modifier.width(16.dp))

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
                    label = { Text("Time in 24 hour clock") },
                    modifier = Modifier.width(150.dp)

                )
            }
            Button(
                onClick = {
                    newLottery.status = "OPEN"
                    newLottery.numWinners = numLotteryWinners.toIntOrNull() ?: 0
                    val startTimestamp = lotteryRepository.convertToFirebaseTimestamp(
                        month = startMonth,
                        day = startDate,
                        year = startYear,
                        time = startTime
                    )
                    val endTimestamp = lotteryRepository.convertToFirebaseTimestamp(
                        month = endMonth,
                        day = endDate,
                        year = endYear,
                        time = endTime
                    )
                    if (startTimestamp == null || endTimestamp == null) {
                        showDateError = true
                    } else if (startTimestamp < endTimestamp) {
                        newLottery.status = "OPEN"
                        newLottery.numWinners = numLotteryWinners.toIntOrNull() ?: 0
                        newLottery.startTimestamp = startTimestamp
                        newLottery.endTimestamp = endTimestamp

                        lotteryRepository.addLottery(newLottery)
                    } else {
                        showDateError = true
                    }
                }
            ) {
                Text("Create Lottery")
            }
            Button(
                onClick = {
                    //check if lottery has been drawn already,
                    // if not set as being drawn and then draw lottery
                    if (newLottery.status == "OPEN" || newLottery.status == "FULL"){
                        newLottery.status = "DRAWING"
                        lotteryRepository.updateStatus(newLottery, newLottery.status)

                        LotteryDraw(testUserList, newLottery, lotteryRepository, notificationController)
                    }else{
                        showLotteryError = true
                    }
                    //drawLottery = true
                }
            ) {
                Text("Run lottery right now")
            }

            /*this button is here to test adding users to a waitinglist on firebase
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

             */
            Button(onClick = onBack) {
                Text("Back")
            }


        }
    //}
}
