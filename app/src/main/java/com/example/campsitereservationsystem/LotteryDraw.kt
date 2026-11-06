package com.example.campsitereservationsystem

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.selects.select


/*
if given size of lottery is bigger than numLotteryWinners just select all entrants
otherwise randomly select numLotteryWinners amount of winners from entrants
and return it in a new array
 */
fun LotteryDraw(
    entrants: List<User>,
    lottery: Lottery,
    lotteryRepository: LotteryRepository,
    notificationController: NotificationController
): List<User>{

    /*should check if Lottery is already being drawn*/

    //select winners
    val selectedEntrants = if (entrants.size <= lottery.numWinners) {
        entrants
    } else {
        entrants.shuffled().take(lottery.numWinners)
    }

    // Save winners to Firestore
    lotteryRepository.updateList(
        lottery,
        selectedEntrants,
        "winners"
    )

    // Notify everyone on the waiting list
    entrants.forEach { entrant ->

        if (selectedEntrants.any { winner -> winner.id == entrant.id }) {

            // User won
            notificationController.sendNotification(
                receiverID = entrant.id,
                message = "Congratulations ${entrant.name}! You have won the campsite lottery!"
            )

        } else {

            // User lost
            notificationController.sendNotification(
                receiverID = entrant.id,
                message = "Unfortunately ${entrant.name}, you were not selected in this campsite lottery. Better luck next time!"
            )
        }
    }

    notificationController.stopNotifications()

    return selectedEntrants
}