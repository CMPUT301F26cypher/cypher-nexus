package com.example.campsitereservationsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

data class Lottery (
    var open: Boolean = false, //true mean entrant can join the lottery
    var numWinners: Int = 0,
    var capacity: Int = 0,  //this is for final checkpoint, currently all values will be 0 and will not be considered
    var startDate: String = "",
    var endMonth: String = "",
    var endDate: String = "",
    var endYear: String = "",
    var endTime: String = "",
    var campsite: com.example.campsitereservationsystem.campsite.Campsite,
    var campsiteID: String = "", //this will be the same campsiteID from campsite

    //to add to the following lists use LotteryName.waitingList.add(User)
    var waitingList: MutableList<User> = mutableStateListOf(),
    var winners: MutableList<User> = mutableStateListOf()
)
