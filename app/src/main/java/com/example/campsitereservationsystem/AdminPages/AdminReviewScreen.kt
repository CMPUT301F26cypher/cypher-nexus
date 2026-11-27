package com.example.campsitereservationsystem

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
import com.example.campsitereservationsystem.review.Review

/*
     AdminReviewsScreen.kt

     Lets an admin moderate the reviews of ONE campsite: remove a whole review
     (US 03.10.01) or remove only its star rating (US 03.09.01 / US 03.07.01).
     Opened from AdminCampsiteBrowseScreen. Only admins should ever reach it.
 *
 */


@Composable
fun AdminReviewsScreen(
    campsiteName: String,
    load: suspend () -> List<Review>,
    onRemoveReview: suspend (Review) -> Unit,
    onRemoveRating: suspend (Review) -> Unit,
    onBack: () -> Unit
) {
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableStateOf(0) }

    // Buttons only set one of these. The LaunchedEffects below do the work.
    var askRemoveReview by remember { mutableStateOf<Review?>(null) }  // waiting for confirm
    var removingReview by remember { mutableStateOf<Review?>(null) }
    var removingRating by remember { mutableStateOf<Review?>(null) }

    // Runs once, and again whenever attempt changes (after a removal)
    LaunchedEffect(attempt) {
        loading = true
        try {
            reviews = load()
        } catch (e: Exception) {
            message = "Failed to load reviews."
        }
        loading = false
    }

    // Runs when removingReview is set to a review
    LaunchedEffect(removingReview) {
        val review = removingReview
        if (review != null) {
            try {
                onRemoveReview(review)
            } catch (e: Exception) {
                message = "Could not remove the review."
            }
            removingReview = null
            attempt++
        }
    }

    // Runs when removingRating is set to a review
    LaunchedEffect(removingRating) {
        val review = removingRating
        if (review != null) {
            try {
                onRemoveRating(review)
            } catch (e: Exception) {
                message = "Could not remove the rating."
            }
            removingRating = null
            attempt++
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("Back") }
            Text("Reviews: $campsiteName", style = MaterialTheme.typography.titleLarge)
        }
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(8.dp))

        when {
            loading -> CircularProgressIndicator()
            reviews.isEmpty() -> Text("No reviews for this campsite.")
            else -> LazyColumn {
                items(reviews) { review ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        val stars = review.rating.coerceIn(0, 5)
                        Text(if (stars == 0) "No rating" else "★".repeat(stars) + "☆".repeat(5 - stars))
                        Text(review.comment)
                        Text("By: ${review.entrantId}", style = MaterialTheme.typography.bodySmall)
                        Row {
                            if (review.rating > 0) {
                                OutlinedButton(onClick = { removingRating = review }) {
                                    Text("Remove Rating")
                                }
                            }
                            OutlinedButton(onClick = { askRemoveReview = review }) {
                                Text("Remove Review")
                            }
                        }
                    }
                }
            }
        }
    }

    askRemoveReview?.let { review ->
        AlertDialog(
            onDismissRequest = { askRemoveReview = null },
            title = { Text("Remove review") },
            text = { Text("Remove this review? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    removingReview = review
                    askRemoveReview = null
                }) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { askRemoveReview = null }) { Text("Cancel") } }
        )
    }
}