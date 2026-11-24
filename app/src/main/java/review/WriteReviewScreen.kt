
package com.example.campsitereservationsystem.qr

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.campsitereservationsystem.campsite.Campsite
import androidx.compose.runtime.rememberCoroutineScope
import com.example.campsitereservationsystem.review.Review
import com.example.campsitereservationsystem.review.ReviewRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

@Composable
fun WriteReviewScreen(
    campsite: Campsite,
    entrantId: String,
    reviewRepository: ReviewRepository,
    onBack: () -> Unit
){
    BackHandler(onBack = onBack)
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Write a Review",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = campsite.name)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your Rating",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            (1..5).forEach { star ->
                Text(
                    text = if (star <= rating) "★" else "☆",
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { rating = star }
                        .padding(end = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Your Review") },
            placeholder = { Text("Share your experience...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!isSaving) {
                    isSaving = true
                    errorMessage = null

                    scope.launch {
                        try {
                            val review = Review(
                                campsiteId = campsite.id,
                                entrantId = entrantId,
                                rating = rating,
                                comment = comment.trim()
                            )

                            reviewRepository.addReview(review)

                            onBack()

                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (error: Exception) {
                            errorMessage = error.message ?: "Failed to save review"
                        } finally {
                            isSaving = false
                        }
                    }
                }
            },
            enabled = rating > 0 && comment.isNotBlank() && !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSaving) "Posting..." else "Post Review")
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}