package com.example.campsitereservationsystem

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

fun LotteryDraw(entrants: List<String>): List<String>{
    //if given size of lottery is bigger than size of entrants just select all entrants

    //5 will be replaced with whatever owner selects, 5 is just for testing rn
    if (entrants.size < 5){
        //simply select all entrants, so simply return entrants
        return entrants
    }else{
        val selectedEntrants = entrants.shuffled().take(5)//don't know if this needs to be a mutable
        //generate 5 DIFFERENT random numbers in the range of 0 to size
        return selectedEntrants
    }
}