package com.example.campsitereservationsystem

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember



/*
if given size of lottery is bigger than numLotteryWinners just select all entrants
otherwise randomly select numLotteryWinners amount of winners from entrants
and return it in a new array
 */
fun LotteryDraw(entrants: List<String>, numLotteryWinners: String): List<String>{
    //need to convert num from string to int
    var intNumLotteryWinners = numLotteryWinners.toIntOrNull()
    if (intNumLotteryWinners == null){
        intNumLotteryWinners = 0
    }

    if (entrants.size < intNumLotteryWinners){
        //simply select all entrants, so simply return entrants
        return entrants
    }else{
        val selectedEntrants = entrants.shuffled().take(intNumLotteryWinners)
        //generate 5 DIFFERENT random numbers in the range of 0 to size
        return selectedEntrants
    }
}