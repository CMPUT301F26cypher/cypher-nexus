package com.example.campsitereservationsystem

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Timestamp


data class Lottery (
    var status: String = "CLOSED",
    //options are:
    // CLOSED --> Lottery has not been started yet
    // OPEN --> Lottery can be joined
    // FULL --> Lottery is full and cannot be joined
    // DRAWING --> Lottery is currently being processed, ensures a lottery draw does not happen twice
    // COMPLETED --> Winners have been selected
    var numWinners: Int = 0,
    var capacity: Int = 0,  //0 should be the default value and will also be used to indicate no max capacity for a lottery
    var registrationStartTimestamp: Timestamp? = null,
    var registrationEndTimestamp: Timestamp? = null,
    var bookingStartTimestamp: Timestamp? = null,
    var bookingEndTimestamp: Timestamp? = null,
    var campsite: com.example.campsitereservationsystem.OwnerCampsitesPath.Campsite? = null,
    var campsiteID: String = "", //this will be the same campsiteID from campsite

    //to add to the following lists use LotteryName.waitingList.add(User)
    //followed by lotteryRepositoryRefrence.updateList(arguments) to send changes to firebase
    var waitingList: MutableList<User> = mutableListOf(),
    var winners: MutableList<User> = mutableListOf()
)