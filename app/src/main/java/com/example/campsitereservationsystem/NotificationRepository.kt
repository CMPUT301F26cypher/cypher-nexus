package com.example.campsitereservationsystem

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.DocumentChange

/* API's/LIBRARIES NEEDED FOR Notification repository
    Firebase Firestore
    ListenerRegistration - allows stop listening to firebase
                           (Needed if we need to switch account on same device)
    DocumentChange - see which notifications were recently added

    class NotificationRepository:
        Stores new notifications, gathers received and sent notifications
        Detects new notifications and updates lists
        Notifies controller when a new notification is sent
 */
class NotificationRepository(
    private val userID: String,
    private val onNewNotification: (Notification) -> Unit){

    private val db = Firebase.firestore
    private val notificationsRef = db.collection("notifications")

    //_sentNotifications - list of sentNotifications from current user
    //_notifications - list of notifications current user received
    private val _notifications = mutableStateListOf<Notification>()
    private val _sentNotifications = mutableStateListOf<Notification>()

    //Stops old notifications from popping-up on app launch
    private var firstSnapshot = true
    private val listener: ListenerRegistration

    init {
        listener = notificationsRef
            .whereEqualTo("receiverID", userID)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null){
                    return@addSnapshotListener
                }

                _notifications.clear()

                snapshot.documents.forEach { document ->
                    val notification = document.toObject(Notification::class.java)

                    if (notification != null){
                        _notifications.add(notification)
                    }
                }
                if(!firstSnapshot){
                    //Only allow pop-ups for notifications that were recently sent
                    snapshot.documentChanges.forEach {change ->
                    if (change.type == DocumentChange.Type.ADDED){
                        val notification = change.document.toObject(Notification::class.java)
                        onNewNotification(notification)
                        }
                    }

                }
                firstSnapshot = false
            }

    }

    val notifications: List<Notification>
        get() = _notifications

    val sentNotifications: List<Notification>
        get() = _sentNotifications

    fun addNotification(notification: Notification){
        //Generate random ID for notifID with firestore document()
        val doc = notificationsRef.document()
        val newNotification = notification.copy(notifID = doc.id)

        doc.set(newNotification)
    }

    fun getSentNotifications(){
        //Collects all sent notifications
        notificationsRef
            .whereEqualTo("senderID", userID)
            .get()
            .addOnSuccessListener { snapshot ->
                _sentNotifications.clear()

                snapshot.documents.forEach {document ->
                    val notification = document.toObject(Notification::class.java)

                    if (notification != null){
                        _sentNotifications.add(notification)
                    }
                }
            }
    }

    fun stopListening() {
        //Stop receiving Firestore updates
        listener.remove()
        _notifications.clear()
        _sentNotifications.clear()
    }


}