package com.example.campsitereservationsystem

import com.example.campsitereservationsystem.campsite.Campsite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/*
    AdminCampsiteBrowseScreen.kt

    Read-only list of campsites for admins.
    Only available if the user is an admin.
 */

// US 03.04.01
@Composable
fun AdminCampsiteBrowseScreen(
    campsites: List<Campsite>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Text("Browse Campsites")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (campsites.isEmpty()) {
            Text("No campsites found.")
        }
        else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(campsites) { campsite ->
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("Name: ${campsite.name}")
                        Text("Campsite ID: ${campsite.id}")
                        Text("Location: ${campsite.location}")
                        Text("Description: ${campsite.description}")
                    }
                }
            }
        }
    }
}
