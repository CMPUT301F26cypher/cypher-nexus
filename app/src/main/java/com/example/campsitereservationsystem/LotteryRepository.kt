package com.example.campsitereservationsystem

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId

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

    /*
    updates either a waitingList or a list of winners

    also converts from to a list type firebase can use
    */
    fun updateList(
        lottery: Lottery,
        list: List<User>,
        listType: String //listType options are: waitingList or winners
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

    fun updateStatus(lottery: Lottery, newStatus: String) {
        lotteriesRef.document(lottery.campsiteID)
            .update("status", newStatus)
            .addOnSuccessListener {
                lottery.status = newStatus
                println("Successfully updated lottery status to $newStatus")
            }
            .addOnFailureListener { exception ->
                println("Failed to update lottery status: ${exception.message}")
            }
    }

    fun deleteLottery(lottery: Lottery){
        lotteriesRef.document(lottery.campsiteID).delete()
    }
    fun convertToFirebaseTimestamp(
        month: String,
        day: String,
        year: String,
        time: String
    ): Timestamp? {
        return try {
            val parts = time.split(":")

            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            val dateTime = LocalDateTime.of(
                year.toInt(),
                month.toInt(),
                day.toInt(),
                hour,
                minute
            )

            val instant = dateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()

            Timestamp(instant.epochSecond, instant.nano)
        } catch (e: Exception) {
            null
        }
    }
}