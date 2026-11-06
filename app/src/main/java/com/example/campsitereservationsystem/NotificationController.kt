package com.example.campsitereservationsystem

import android.Manifest
import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import android.os.Build
/* API's/LIBRARIES NEEDED FOR Notifications
     Firebase Firestore
     ListenerRegistration - allows stop listening to firebase
                           (Needed if we need to switch account on same device)
     DocumentChange - see which notifications were recently added

   class NotificationController:
     Sends and displays notifications
     Check user notification preferences and android permissions
     Uses NotificationRepository functions to store and
     gather notifications
*/


class NotificationController(
    private val user: User,
    private val context: Context
) {
    private val channelID = "cypherNotifications"

    private val manager = context.getSystemService(
        Context.NOTIFICATION_SERVICE)
            as NotificationManager
    private val repository = NotificationRepository(user.id){notification ->
        displayNotification(notification.message)
    }

    init {
        //Channels are required for Android 8.0+ or API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelID,
                "Campsite Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
    }

    //Access list of notifications
    val notifications: List<Notification>
        get() = repository.notifications
    val sentNotifications: List<Notification>
        get() = repository.sentNotifications

    fun displayNotification(message: String){
        //Checks user preference
        if (!user.notificationsEnabled){
            return
        }

        //Checks Notification permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ){
            return
        }

        //Create Notification(Appearance and content)
        val notification = NotificationCompat.Builder(context, channelID)
            .setContentTitle("Campsite Notifications")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        //Notify() displays notification
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun sendNotification(receiverID: String, message: String){
        //create notification record
        val notification = Notification(
            notifID = "",
            senderID = user.id,
            receiverID = receiverID,
            message = message,
            timestamp = System.currentTimeMillis()
        )
        //saves notification
        repository.addNotification(notification)

    }

    fun getSentNotifications(){
        //Gather all sent notifications from current user
        repository.getSentNotifications()
    }

    fun stopNotifications(){
        //stops detecting for notifications
        repository.stopListening()
    }

}