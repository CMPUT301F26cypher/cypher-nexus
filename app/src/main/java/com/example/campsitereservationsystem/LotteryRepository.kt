package com.example.campsitereservationsystem

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.tasks.await
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class LotteryRepository {
    private val db = Firebase.firestore
    private val lotteriesRef = db.collection("lotteries")
    private val lotteries = mutableStateListOf<Lottery>()


    // Function for getting the waiting list as an Owner US 02.09.01
    fun getWaitingList(
        campsiteID: String,
        onResult: (List<User>) -> Unit,
        onError: () -> Unit = {}
    ) {
        lotteriesRef.document(campsiteID)
            .get()
            .addOnSuccessListener { document ->
                val raw = document.get("waitingList") as? List<*> ?: emptyList<Any>()
                onResult(raw.mapNotNull { item ->
                    val entry = item as? Map<*, *> ?: return@mapNotNull null
                    User(
                        id = entry["id"] as? String ?: "",
                        name = entry["name"] as? String ?: "",
                        email = entry["email"] as? String ?: "",
                        phone = entry["phone"] as? String ?: ""
                    )
                })
            }
            .addOnFailureListener { onError() }
    }


    fun addLottery(
        lottery: Lottery,
        onSuccess: () -> Unit = {}
    ) {
        lotteriesRef.document(lottery.campsiteID)
            .set(lottery)
            .addOnSuccessListener {
                println("Lottery successfully created")
                onSuccess()
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
    suspend fun updateList(
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
                "notificationsEnabled" to user.notificationsEnabled
            )
        }

        lotteriesRef.document(lottery.campsiteID)
            .update(listType, firestoreList)
            .await()
    }

    suspend fun updateStatus(
        lottery: Lottery,
        newStatus: String
    ) {
        lotteriesRef.document(lottery.campsiteID)
            .update("status", newStatus)
            .await()

        lottery.status = newStatus
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

    fun getLotteryByCampsiteID(
        campsiteID: String,
        onResult: (Lottery?) -> Unit
    ) {
        lotteriesRef.document(campsiteID)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val lottery = document.toObject(Lottery::class.java)
                    onResult(lottery)
                } else {
                    println("No lottery found for campsite $campsiteID")
                    onResult(null)
                }
            }
            .addOnFailureListener { exception ->
                println("Error retrieving lottery: ${exception.message}")
                onResult(null)
            }
    }

    fun scheduleLotteryWorker(
        context: Context,
        campsiteID: String,
        endTime: Long
    ) {
        // Calculate how long until lottery ends
        val delay = endTime - System.currentTimeMillis()

        val inputData = Data.Builder()
            .putString("campsiteID", campsiteID)
            .build()

        val lotteryWorkRequest =
            OneTimeWorkRequestBuilder<LotteryWorker>()
                .setInputData(inputData)
                .setInitialDelay(
                    delay.coerceAtLeast(0),
                    TimeUnit.MILLISECONDS
                )
                .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "Lottery_$campsiteID",
                androidx.work.ExistingWorkPolicy.REPLACE,
                lotteryWorkRequest
            )
    }


}