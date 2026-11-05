package com.example.campsitereservationsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PolicyScreen(
    onContinue: (() -> Unit)? = null,   // show before account creation
    onBack: (() -> Unit)? = null  // can be opened from home screen
) {
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                TextButton(onClick = onBack) { Text("Back") }
            }
            Text("App Policy & Guidelines")
        }
        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
        ) {
            // Terms of service
            PolicyInfo(
                "Age Requirements",
                "Users must be at least 18 years old to participate in the campsite lottery."
            )
            // Kinda paraphrased from Instagram's app policy
            PolicyInfo(
                "Misconduct Policy",
                "You cannot impersonate others or provide inaccurate information.\n" +
                        "Do not use offensive or inappropriate names.\n" +
                        "Do not post inappropriate content or images.\n" +
                        "Admins may remove profiles that violate this policy."
            )
            //
            PolicyInfo(
                "Reservations and Cancellations",
                "Follow the campsite owner's rules and policies.\n" +
                        "Campsite owners have the right to reject/cancel reservations."
            )
            // User privacy
            PolicyInfo(
                "Your Information",
                "Your name, email, and phone number will only be used to manage reservations.\n" +
                        "You may edit or delete your profile at any time."
            )
        }
        if (onContinue != null) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) { Text("I understand and have read the policy & guidelines") }
        }
    }
}

@Composable
private fun PolicyInfo(title: String, text: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Text(text, style = MaterialTheme.typography.bodyMedium)
    Spacer(modifier = Modifier.height(16.dp))
}