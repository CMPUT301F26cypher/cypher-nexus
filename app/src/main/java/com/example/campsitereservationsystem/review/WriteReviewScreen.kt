package com.example.campsitereservationsystem.EntrantPath

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsitereservationsystem.OwnerCampsitesPath.Campsite
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
) {


    val green = Color(0xFF819F5C)
    val darkGreen = Color(0xFF587437)
    val cardGray = Color(0xFFE0E0E0)

    val scope = rememberCoroutineScope()

    var isSaving by remember {
        mutableStateOf(false)
    }
    BackHandler {
        if (!isSaving) {
            onBack()
        }
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var rating by remember {
        mutableIntStateOf(0)
    }

    var comment by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .padding(16.dp)
    ) {

        Text(
            text = "Write a Review",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {

            // Campsite being reviewed
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = cardGray,
                    contentColor = Color.Black
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Reviewing campsite",
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = campsite.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Rating selection
            Text(
                text = "Your Rating",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                (1..5).forEach { star ->

                    Text(
                        text = if (star <= rating) "★" else "☆",
                        fontSize = 36.sp,
                        color = darkGreen,
                        modifier = Modifier
                            .clickable(enabled = !isSaving) {
                                rating = star
                            }
                            .padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (rating == 0) {
                    "Select your rating"
                } else {
                    "$rating/5 stars"
                },
                color = darkGreen,
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Review comment
            OutlinedTextField(
                value = comment,
                onValueChange = {
                    comment = it
                },
                label = {
                    Text("Your Review")
                },
                placeholder = {
                    Text("Share your experience...")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                minLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = green,
                    unfocusedBorderColor = green,
                    focusedLabelColor = darkGreen,
                    cursorColor = darkGreen
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save the review to Firebase
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

                                errorMessage = error.message
                                    ?: "Failed to save review"

                            } finally {

                                isSaving = false
                            }
                        }
                    }
                },
                enabled = rating > 0 &&
                        comment.isNotBlank() &&
                        !isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = green,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    if (isSaving) {
                        "Posting..."
                    } else {
                        "Post Review"
                    }
                )
            }

            errorMessage?.let { message ->

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Back button
        Button(
            onClick = onBack,
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = green,
                contentColor = Color.Black
            )
        ) {
            Text("Back")
        }
    }
}