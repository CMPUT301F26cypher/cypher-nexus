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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.campsitereservationsystem.review.Review

/*
    AdminCampsiteBrowseScreen.kt

    Read-only list of campsites for admins.
    Only available if the user is an admin.
 */

// US 03.04.01
@Composable
fun AdminCampsiteBrowseScreen(
    load: suspend () -> List<Campsite>,
    onRemoveCampsite: suspend (Campsite) -> Unit = {},
    onRemoveOwner: (String) -> Unit = {},
    loadReviews: suspend (String) -> List<Review> = { emptyList() },
    onRemoveReview: suspend (Review) -> Unit = {},
    onRemoveRating: suspend (Review) -> Unit = {},
    onBack: () -> Unit
) {
    var campsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var attempt by remember { mutableStateOf(0) }

    var message by remember { mutableStateOf<String?>(null) }
    var toRemove by remember { mutableStateOf<Pair<Campsite, Boolean>?>(null) }
    var request by remember { mutableStateOf<Pair<Campsite, Boolean>?>(null) }
    var reviewing by remember { mutableStateOf<Campsite?>(null) }  // campsite whose reviews are open

    // Runs once, and again each time "Try again" changes attempt
    LaunchedEffect(attempt) {
        loading = true
        failed = false
        try {
            campsites = load()
        } catch (e: Exception) {
            failed = true
        }
        loading = false
    }

    // Runs when request is set (after the admin confirms)
    LaunchedEffect(request) {
        val current = request
        if (current != null) {
            val (campsite, ownerOnly) = current
            try {
                // Only ONE thing is removed
                if (ownerOnly) onRemoveOwner(campsite.ownerId) else onRemoveCampsite(campsite)
                message = null
            } catch (e: Exception) {
                message = "Could not remove it. Please try again."
            }
            request = null
            attempt++
        }
    }

    // Reviews of one campsite
    val selected = reviewing
    if (selected != null) {
        AdminReviewsScreen(
            campsiteName = selected.name,
            load = { loadReviews(selected.id) },
            onRemoveReview = onRemoveReview,
            onRemoveRating = onRemoveRating,
            onBack = { reviewing = null }
        )
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Text("Browse Campsites", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            loading -> CircularProgressIndicator()

            failed -> {
                Text("Failed to load campsites.")
                Button(onClick = { attempt++ }) {
                    Text("Try again")
                }
            }

            (campsites.isEmpty()) -> Text("No campsites found.")

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(campsites) { campsite ->
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text(campsite.name, style = MaterialTheme.typography.titleMedium)
                            Text("Owner: ${campsite.ownerId}")
                            Text("Location: ${campsite.location}")
                            Text("Max Occupancy: ${campsite.maxOccupancy}")
                            Text("Description: ${campsite.description}")
                            Row {
                                OutlinedButton(onClick = {
                                    reviewing = campsite
                                }) { Text("Reviews") }
                                OutlinedButton(onClick = {
                                    toRemove = Pair(campsite, false)
                                }) { Text("Remove Campsite") }
                                OutlinedButton(onClick = {
                                    toRemove = Pair(campsite, true)
                                }) { Text("Remove Owner") }
                            }
                        }
                    }
                }
            }
        }

        toRemove?.let { (campsite, ownerOnly) ->
            AlertDialog(
                onDismissRequest = { toRemove = null },
                title = { Text(if (ownerOnly) "Remove owner" else "Remove campsite") },
                text = {
                    Text(
                        if (ownerOnly) "Remove the owner's profile (${campsite.ownerId})? Their campsites stay. This cannot be undone."
                        else "Remove \"${campsite.name}\"? The owner and reviews stay. This cannot be undone."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        request = Pair(campsite, ownerOnly)
                        toRemove = null
                    }) { Text("Remove") }
                },
                dismissButton = { TextButton(onClick = { toRemove = null }) { Text("Cancel") } }
            )
        }
    }
}