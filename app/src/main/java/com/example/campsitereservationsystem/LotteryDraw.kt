package com.example.campsitereservationsystem

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.selects.select


/*
if given size of lottery is bigger than numLotteryWinners just select all entrants
otherwise randomly select numLotteryWinners amount of winners from entrants
and return it in a new array
 */
fun LotteryDraw(entrants: List<User>, lottery: Lottery, lotteryRepository: LotteryRepository): List<User>{
    //need to convert num from string to int

    if (entrants.size < lottery.numWinners){
        //simply select all entrants, so simply return entrants
        //********need to add winners to firestore here *******
        lotteryRepository.updateList(lottery, entrants, "winners")

        return entrants
    }else{
        val selectedEntrants = entrants.shuffled().take(lottery.numWinners)
        //generate 5 DIFFERENT random numbers in the range of 0 to size
        //********need to add winners to firestore here *******
        lotteryRepository.updateList(lottery, selectedEntrants, "winners")
        return selectedEntrants
    }

    //the notification part of it

    //general idea is you make the winners list, then make the losers list
    // then send notifications down the line
}