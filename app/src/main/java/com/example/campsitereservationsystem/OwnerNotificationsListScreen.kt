package com.example.campsitereservationsystem

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import java.text.DateFormat
import java.util.Date

/* Sent notification history for the signed-in owner. This screen only reads data. */
@Composable
fun OwnerNotificationsListScreen(
    user: User?,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (user == null || user.id.isBlank() || Role.OWNER !in user.roles) {
            Text("Sign in as a campsite owner to view your sent notifications.")
            Spacer(Modifier.weight(1f))
        } else {
            OwnerSentNotificationHistory(user.id, Modifier.weight(1f))
        }

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF819F5C),
                contentColor = Color.Black
            )
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun OwnerSentNotificationHistory(ownerID: String, modifier: Modifier) {
    val db = remember { FirebaseFirestore.getInstance() }
    var notifications by remember(ownerID) { mutableStateOf<List<Notification>>(emptyList()) }
    val recipientNames = remember(ownerID) { mutableStateMapOf<String, String>() }
    var loading by remember(ownerID) { mutableStateOf(true) }
    var error by remember(ownerID) { mutableStateOf<String?>(null) }
    var refresh by remember(ownerID) { mutableIntStateOf(0) }
    val dateFormat = remember {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    }

    LaunchedEffect(ownerID, refresh) {
        loading = true
        error = null
        try {
            val snapshot = db.collection("notifications")
                .whereEqualTo("senderID", ownerID)
                .get()
                .await()
            notifications = snapshot.documents.mapNotNull { document ->
                document.toObject(Notification::class.java)?.copy(notifID = document.id)
            }.sortedByDescending { it.timestamp }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            notifications = emptyList()
            error = "Could not load sent notifications. Please try Refresh."
        } finally {
            loading = false
        }

        for (receiverID in notifications.map { it.receiverID }.filter { it.isNotBlank() }.distinct()) {
            val name = try {
                db.collection("users").document(receiverID).get().await()
                    .getString("name")?.takeIf { it.isNotBlank() }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
            recipientNames[receiverID] = name ?: "User $receiverID"
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(
            onClick = { refresh++ },
            enabled = !loading,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Refresh")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                loading -> item { Text("Loading sent notifications...") }
                error != null -> item {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
                notifications.isEmpty() -> item { Text("You have not sent any notifications yet.") }
                else -> items(notifications, key = { it.notifID }) { notification ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE0E0E0),
                            contentColor = Color.Black
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = recipientNames[notification.receiverID]
                                    ?: notification.receiverID.takeIf { it.isNotBlank() }
                                        ?.let { "User $it" }
                                    ?: "Unknown recipient",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (notification.timestamp > 0L) {
                                    dateFormat.format(Date(notification.timestamp))
                                } else {
                                    "Sent time unavailable"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(notification.message)
                        }
                    }
                }
            }
        }
    }
}