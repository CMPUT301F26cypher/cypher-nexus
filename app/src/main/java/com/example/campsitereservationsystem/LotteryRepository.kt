package com.example.campsitereservationsystem

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class LotteryRepository {
    private val db = Firebase.firestore
    private val lotteriesRef = db.collection("lotteries")
    private val lotteries = mutableStateListOf<Lottery>()


    fun addLottery(lottery: Lottery) {
        lotteriesRef.document(lottery.campsiteID)
            .set(lottery)
            .addOnSuccessListener {
                println("Lottery successfully created")
            }
            .addOnFailureListener { exception ->
                println("Error creating lottery: ${exception.message}")
            }
    }

    fun updateLottery(oldLottery: Lottery, updatedLottery: Lottery) {
        lotteriesRef.document(oldLottery.campsiteID).set(updatedLottery)
    }

    //can be used for Waiting list and winners list
    //listType options are:
    //waitingList
    //winners
    fun updateList(
        lottery: Lottery,
        list: List<User>,
        listType: String
    ) {
        // Convert User objects into Firestore-compatible Maps
        val firestoreList = list.map { user ->
            mapOf(
                "id" to user.id,
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "roles" to user.roles.map { it.name },
                "notificationsEnabled" to user.notificationsEnabled,
                "profileImage" to user.profileImage
            )
        }

        lotteriesRef.document(lottery.campsiteID)
            .update(listType, firestoreList)
            .addOnSuccessListener {
                println("Successfully updated $listType")
            }
            .addOnFailureListener { exception ->
                println("Failed to update $listType: ${exception.message}")
            }
    }

    fun deleteLottery(lottery: Lottery){
        lotteriesRef.document(lottery.campsiteID).delete()
    }
}