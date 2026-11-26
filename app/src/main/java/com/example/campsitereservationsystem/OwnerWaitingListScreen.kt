package com.example.campsitereservationsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.campsitereservationsystem.OwnerCampsitesPath.Campsite

/*
    OwnerWaitingListScreen.kt

    Campsite owners can pick one of their campsites and see
    the entrants who joined its lottery waiting list.
    The entrants are loaded with the loadEntrants function that is passed in
    (LotteryRepository.getWaitingList), so this screen does not know about Firebase.

    NOTE: only shoes each entrant's name and email.
    The list is empty until a lottery exists for the campsite.


 */

// US 02.09.01
// As a campsite owner, I want to view a list of entrants who have joined the waiting list for my campsite lottery.

@Composable
fun OwnerWaitingListScreen(
    campsites: List<Campsite>,
    loadEntrants: (campsite: Campsite, onResult: (List<User>) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    var selected by remember { mutableStateOf<Campsite?>(null) }
    val campsite = selected
    var entrants by remember { mutableStateOf<List<User>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    // Load the waiting list anytime a campsite is chosen
    LaunchedEffect(campsite) {
        if (campsite != null) {
            loading = true
            loadEntrants(campsite) { result ->
                entrants = result
                loading = false
            }
        }
    }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = {
                    if (campsite != null) selected = null
                    else onBack() }
            ) {
                Text("Back")
            }
            Text(
                if (campsite == null) "My Campsites" else "Waiting List"
            )
        }
        Spacer(Modifier.height(16.dp))
        // Select a campsite
        if (campsite == null) {
            if (campsites.isEmpty()) {
                Text("No campsites found.")
            }
            else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {     // CHECK SPACING TO SEE IF ITS UGLY
                    items(campsites) { c ->
                        Button(
                            onClick = { selected = c },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(c.name)
                        }
                    }
                }
            }
        }
        // Show waiting list for selected campsite
        else {
            Text(campsite.name, style = MaterialTheme.typography.titleMedium)

            when {
                loading -> CircularProgressIndicator()
                entrants.isEmpty() -> Text("No entrants have joined yet!")

                else -> {
                    Text(" ${entrants.size} people on the waiting list ")
                    Spacer(Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(entrants) { e ->
                            Column(Modifier.fillMaxWidth()) {
                                Text(e.name)
                                Text(e.email)
                            }
                        }
                    }
                }
            }
        }
    }
}