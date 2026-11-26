package com.example.campsitereservationsystem.AdminPages

import com.example.campsitereservationsystem.Notification
import com.example.campsitereservationsystem.NotificationRepository
import com.example.campsitereservationsystem.UserRepository
import com.example.campsitereservationsystem.User
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.text.format.DateFormat


@Composable
fun AdminNotificationReview(
    userID: String,
    onBack: () -> Unit
) {
    val notificationRepo = remember(userID) { NotificationRepository(userID) {} }
    val userRepo = remember { UserRepository() }

    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }

    // Retrieve all notifications and users
    LaunchedEffect(notificationRepo) {
        notificationRepo.getAllNotifications {
            notifications = it.sortedByDescending { notification ->
                notification.timestamp }
        }

        userRepo.getAllUsers {
            users = it
        }
    }

    // Stop repository listener when leaving the screen
    DisposableEffect(notificationRepo) {
        onDispose { notificationRepo.stopListening() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Review Notifications", fontSize = 30.sp)

        Spacer(modifier = Modifier.height(25.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(notifications, key = { it.notifID }) { notification ->

                var sender = notification.senderID
                var receiver = notification.receiverID

                for (user in users){
                    if (user.id == notification.senderID){
                        sender = user.name
                    }
                    if (user.id == notification.receiverID){
                        receiver = user.name
                    }
                }

                AdminNotificationRow(
                    notification = notification,
                    sender = sender,
                    receiver = receiver
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        AdminMenuButtons("Back", onBack)
    }
}

@Composable
private fun AdminNotificationRow(
    notification: Notification,
    sender: String,
    receiver: String
) {
    val timestamp = DateFormat.format(
        "MMM dd, yyyy - hh:mm a",
        notification.timestamp
    ).toString()

    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Sender: $sender", fontSize = 16.sp)
            Text("Receiver: $receiver", fontSize = 16.sp)

            HorizontalDivider()

            Text(notification.message, fontSize = 16.sp)

            Text(timestamp, fontSize = 12.sp)
        }
    }
}