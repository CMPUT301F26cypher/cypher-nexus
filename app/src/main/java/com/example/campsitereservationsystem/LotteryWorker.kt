package com.example.campsitereservationsystem

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await


//extends the WorkManager class
class LotteryWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams){
    override suspend fun doWork(): Result {

        val campsiteID = inputData.getString("campsiteID")
            ?: return Result.failure()

        return try {

            val db = Firebase.firestore

            //create lotteryRepository
            val lotteryRepository = LotteryRepository()

            // Retrieve lottery from Firestore
            val document = db.collection("lotteries")
                .document(campsiteID)
                .get()
                .await()

            if (!document.exists()) {
                return Result.failure()
            }

            val lottery = document.toObject(Lottery::class.java)
                ?: return Result.failure()

            // Check lottery status
            if (lottery.status != "OPEN" &&
                lottery.status != "FULL") {
                //note that this is success because its the lottery that fails here,
                //not the WorkManager task itself
                return Result.success()
            }

            // Check whether the lottery has actually expired
            val endTime = lottery.registrationEndTimestamp?.toDate()?.time
                ?: return Result.failure()

            val currentTime = System.currentTimeMillis()

            Log.d("LotteryWorker", "Current time: $currentTime")
            Log.d("LotteryWorker", "End time: $endTime")
            Log.d("LotteryWorker", "Time remaining: ${endTime - currentTime} ms")

            if (currentTime < endTime) {
                Log.d("LotteryWorker", "Lottery has not expired. Retrying.")
                return Result.retry()
            }

            // Lottery has expired.
            // Perform lottery draw here.

            val lotteryCampsite = lottery.campsite
            if (lotteryCampsite != null) {
                val ownerDocument = db.collection("users")
                    .document(lotteryCampsite.ownerId)
                    .get()
                    .await()

                val owner = ownerDocument.toObject(User::class.java)
                    ?.copy(id = ownerDocument.id)
                    ?: return Result.failure()

                // Create NotificationController using campsite owner
                val notificationController = NotificationController(
                    user = owner,
                    context = applicationContext
                )


                LotteryDraw(lottery.waitingList, lottery, lotteryRepository, notificationController)

                notificationController.stopNotifications()
                Result.success()
            }else{
                Result.failure()
            }

        } catch (e: Exception) {
            Log.e("LotteryWorker", "Worker failed: ${e.message}", e)
            Result.retry()
        }
    }
}