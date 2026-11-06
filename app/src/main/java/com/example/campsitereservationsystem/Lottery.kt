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
    var capacity: Int = 0,  //this is for final checkpoint, currently all values will be 0 and will not be considered
    var startTimestamp: Timestamp? = null,
    var endTimestamp: Timestamp? = null,
    var campsite: com.example.campsitereservationsystem.campsite.Campsite? = null,
    var campsiteID: String = "", //this will be the same campsiteID from campsite

    //to add to the following lists use LotteryName.waitingList.add(User)
    //followed by lotteryRepositoryRefrence.updateList(arguments) to send changes to firebase
    var waitingList: MutableList<User> = mutableListOf(),
    var winners: MutableList<User> = mutableListOf()
)