
/*
 * LotteryRepository.kt
 *
 * Here we manage the lotteries and their information in Firebase.
 * We also use this class to let entrants join one or multiple waiting lists.
 *
 * Outstanding issue: We still need to test the WOW Factor with Firebase
 * and make sure all the registration timestamps use the same fields.
 */

package com.example.campsitereservationsystem

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * This class manages the lotteries, waiting lists and winners in Firebase.
 */
class LotteryRepository {

    private val db = Firebase.firestore
    private val lotteriesRef = db.collection("lotteries")
    private val lotteries = mutableStateListOf<Lottery>()

    /**
     * This function gets the waiting list of a campsite.
     *
     * @param campsiteID The ID of the campsite.
     * @param onResult Returns the users in the waiting list.
     * @param onError Called if we cannot get the waiting list.
     */
    // US 02.09.01
    fun getWaitingList(
        campsiteID: String,
        onResult: (List<User>) -> Unit,
        onError: () -> Unit = {}
    ) {
        lotteriesRef.document(campsiteID)
            .get()
            .addOnSuccessListener { document ->
                val raw = document.get("waitingList") as? List<*>
                    ?: emptyList<Any>()

                onResult(raw.mapNotNull { item ->
                    val entry = item as? Map<*, *>
                        ?: return@mapNotNull null

                    User(
                        id = entry["id"] as? String ?: "",
                        name = entry["name"] as? String ?: "",
                        email = entry["email"] as? String ?: "",
                        phone = entry["phone"] as? String ?: ""
                    )
                })
            }
            .addOnFailureListener {
                onError()
            }
    }

    /**
     * This function creates a new lottery in Firebase.
     *
     * @param lottery The lottery we want to create.
     * @param onSuccess Called when the lottery is saved.
     */
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

    /**
     * This function updates an existing lottery.
     *
     * @param oldLottery The original lottery.
     * @param updatedLottery The lottery with the new information.
     */
    fun updateLottery(
        oldLottery: Lottery,
        updatedLottery: Lottery
    ) {
        lotteriesRef.document(oldLottery.campsiteID)
            .set(updatedLottery)
    }

    /**
     * This function updates the waiting list or the list of winners.
     *
     * @param lottery The lottery we want to update.
     * @param list The users we want to save.
     * @param listType Either waitingList or winners.
     */
    suspend fun updateList(
        lottery: Lottery,
        list: List<User>,
        listType: String
    ) {

        // We convert the users into a format Firebase can use
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

    /**
     * This function updates the status of a lottery.
     *
     * @param lottery The lottery we want to update.
     * @param newStatus The new status of the lottery.
     */
    suspend fun updateStatus(
        lottery: Lottery,
        newStatus: String
    ) {
        lotteriesRef.document(lottery.campsiteID)
            .update("status", newStatus)
            .await()

        lottery.status = newStatus
    }

    /**
     * This function deletes a lottery from Firebase.
     *
     * @param lottery The lottery we want to delete.
     */
    fun deleteLottery(lottery: Lottery) {
        lotteriesRef.document(lottery.campsiteID).delete()
    }

    /**
     * This function converts a date and time into a Firebase Timestamp.
     *
     * @param month The month.
     * @param day The day.
     * @param year The year.
     * @param time The time.
     * @return The timestamp or null if the date is not valid.
     */
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

            Timestamp(
                instant.epochSecond,
                instant.nano
            )

        } catch (e: Exception) {
            null
        }
    }

    /**
     * This function gets the lottery using the campsite ID.
     *
     * @param campsiteID The ID of the campsite.
     * @param onResult Returns the lottery or null if we cannot get it.
     */
    fun getLotteryByCampsiteID(
        campsiteID: String,
        onResult: (Lottery?) -> Unit
    ) {
        lotteriesRef.document(campsiteID)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {
                    val lottery = document.toObject(
                        Lottery::class.java
                    )

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

    /**
     * This function schedules the lottery draw.
     *
     * @param context The application context.
     * @param campsiteID The campsite of the lottery.
     * @param endTime When the lottery registration ends.
     */
    fun scheduleLotteryWorker(
        context: Context,
        campsiteID: String,
        endTime: Long
    ) {

        // We calculate how long until the lottery ends
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

    // WOW Factor

    /**
     * This function allows an entrant to join a waiting list.
     *
     * @param campsiteID The campsite the entrant wants to join.
     * @param user The entrant we want to register.
     * @return True if the entrant was added to the waiting list.
     */
    suspend fun joinWaitingList(
        campsiteID: String,
        user: User
    ): Boolean {

        // We check if the user has permission to join
        if (user.id.isBlank() ||
            !Permissions().isAllowed(
                user.roles,
                Action.JOIN_WAITLIST
            )
        ) {
            return false
        }

        if (campsiteID.isBlank()) {
            return false
        }

        val lotteryRef = lotteriesRef.document(campsiteID)

        // We use a transaction so users can join at the same time
        return db.runTransaction { transaction ->

            val document = transaction.get(lotteryRef)

            // We check if the lottery exists
            if (!document.exists()) {
                return@runTransaction false
            }

            // The lottery needs to be open
            if (document.getString("status") != "OPEN") {
                return@runTransaction false
            }

            val now = Timestamp.now()

            // We check both field names used in the project
            val start = document.getTimestamp(
                "registrationStartTimestamp"
            ) ?: document.getTimestamp("startTimestamp")

            val end = document.getTimestamp(
                "registrationEndTimestamp"
            ) ?: document.getTimestamp("endTimestamp")

            // We check if the registration period is active
            if (start == null ||
                end == null ||
                end <= start ||
                now < start ||
                now >= end
            ) {
                return@runTransaction false
            }

            val rawWaitingList = document.get("waitingList")

            val waitingList = when (rawWaitingList) {
                null -> emptyList<Any>()
                is List<*> -> rawWaitingList
                else -> throw IllegalStateException(
                    "Invalid waiting list"
                )
            }

            // We check if the entrant is already in the waiting list
            val alreadyJoined = waitingList.any { entry ->
                (entry as? Map<*, *>)?.get("id") == user.id
            }

            if (alreadyJoined) {
                return@runTransaction false
            }

            // If capacity is 0, there is no maximum capacity
            val capacity = document.getLong("capacity") ?: 0L

            if (capacity < 0) {
                return@runTransaction false
            }

            if (capacity > 0 && waitingList.size >= capacity) {
                return@runTransaction false
            }

            // We use the same user format as updateList
            val newEntrant = mapOf(
                "id" to user.id,
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "roles" to user.roles.map { it.name },
                "notificationsEnabled" to user.notificationsEnabled
            )

            val updatedList = waitingList + newEntrant

            // Here we save the entrant in Firebase
            transaction.update(
                lotteryRef,
                "waitingList",
                updatedList
            )

            // We change the status if the lottery becomes full
            if (capacity > 0 && updatedList.size >= capacity) {
                transaction.update(
                    lotteryRef,
                    "status",
                    "FULL"
                )
            }

            true

        }.await()
    }

    /**
     * This function allows an entrant to join all the eligible
     * waiting lists that match the selected filters.
     *
     * @param campsiteIDs The campsites that match the filters.
     * @param user The entrant we want to register.
     * @return The number of waiting lists successfully joined.
     */
    suspend fun joinMatchingWaitlists(
        campsiteIDs: List<String>,
        user: User
    ): Int {

        var joinedCount = 0

        // We go through all the matching campsites
        for (campsiteID in campsiteIDs.distinct()) {

            try {

                // We reuse the function to join each waiting list
                if (joinWaitingList(campsiteID, user)) {
                    joinedCount++
                }

            } catch (cancelled: CancellationException) {

                throw cancelled

            } catch (error: Exception) {

                // If one fails, we continue with the other campsites
                println(
                    "Could not join lottery $campsiteID: ${error.message}"
                )
            }
        }

        return joinedCount
    }

    /**
     * Here we get all the lotteries the entrant has joined.
     *
     * @param userID The ID of the current entrant.
     * @return The lotteries where the entrant is registered.
     */
    suspend fun getJoinedLotteries(
        userID: String
    ): List<Lottery> {

        if (userID.isBlank()) {
            return emptyList()
        }

        val documents = lotteriesRef.get().await()

        // We check if the entrant is in each waiting list
        return documents.documents.mapNotNull { document ->

            val waitingList =
                document.get("waitingList") as? List<*>
                    ?: emptyList<Any>()

            val hasJoined = waitingList.any { entry ->
                (entry as? Map<*, *>)?.get("id") == userID
            }

            if (hasJoined) {
                document.toObject(Lottery::class.java)
            } else {
                null
            }
        }
    }

}
