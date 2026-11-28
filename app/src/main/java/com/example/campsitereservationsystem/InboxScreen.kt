package com.example.campsitereservationsystem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import android.text.format.DateFormat

@Composable
fun InboxScreen(
    userID: String,
    onBack: () -> Unit
) {
    val notificationRepo = remember(userID) {
        NotificationRepository(userID) {}
    }
    // Retrieve received notifications, newest first
    val notifications = notificationRepo.notifications
        .sortedByDescending { it.timestamp }

    val userRepo = remember {UserRepository()}
    var users by remember{mutableStateOf<List<User>>(emptyList())}

    LaunchedEffect(userRepo) {
        userRepo.getAllUsers {
            users = it
        }
    }

    DisposableEffect(notificationRepo) {
        onDispose {
            notificationRepo.stopListening()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Inbox", fontSize = 30.sp)

        Spacer(modifier = Modifier.height(25.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(notifications, key = { it.notifID }) { notification ->

                var sender = notification.senderID

                for (user in users){
                    if (user.id == notification.senderID){
                        sender = user.name
                        break
                    }
                }
                NotificationRow(notification = notification, sender = sender)
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF819F5C)
            )
        ) {
            Text("Back", color = Color.Black)
        }
    }
}

//Reusable Notification row for inbox and admin review
@Composable
fun NotificationRow(
    notification: Notification,
    sender: String,
    receiver: String? = null
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

            if (receiver != null) {
                Text("Receiver: $receiver", fontSize = 16.sp)
            }

            HorizontalDivider()


            Text(notification.message, fontSize = 16.sp)

            Text(timestamp, fontSize = 12.sp)
        }
    }
}