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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.campsitereservationsystem.campsite.Campsite

/*
    OwnerWaitingListScreen.kt

    Campsite owners can pick one of their campsites and see
    the entrants who joined its waiting list.
    The screen does not know where entrants
    come from; they are supplied through the 'entrantsFor' function.


 */

// US 02.09.01
// As a campsite owner, I want to view a list of entrants who have joined the waiting list for my campsite lottery.

@Composable
fun OwnerWaitingListScreen(
    campsites: List<Campsite>,
    entrantsFor: (Campsite) -> List<User>,
    onBack: () -> Unit
) {
    var selected by remember { mutableStateOf<Campsite?>(null) }
    val campsite = selected

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
            val entrants = entrantsFor(campsite)
            Text(campsite.name)
            Text(" ${entrants.size} people on the waiting list ")
            Spacer(Modifier.height(16.dp))

            if (entrants.isEmpty()) {
                Text("No entrants have joined yet!")
            }
            else {
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