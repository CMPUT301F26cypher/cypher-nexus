package com.example.campsitereservationsystem.qr

import androidx.activity.compose.BackHandler
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
import com.example.campsitereservationsystem.campsite.Campsite
import com.example.campsitereservationsystem.review.Review
import com.example.campsitereservationsystem.review.ReviewRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

@Composable
fun EntrantCampsiteDetailsScreen(
    campsite: Campsite,
    isLotteryActive: Boolean,
    reviewRepository: ReviewRepository,
    onJoinWaitingList: () -> Unit,
    onWriteReview: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val green = Color(0xFF819F5C)
    val darkGreen = Color(0xFF587437)
    val cardGray = Color(0xFFE0E0E0)

    // Store the reviews retrieved from Firebase
    var reviews by remember(campsite.id) {
        mutableStateOf<List<Review>>(emptyList())
    }

    var showAllReviews by remember(campsite.id) {
        mutableStateOf(false)
    }

    var isLoading by remember(campsite.id) {
        mutableStateOf(true)
    }

    var errorMessage by remember(campsite.id) {
        mutableStateOf<String?>(null)
    }

    // Retrieve campsite reviews from Firebase
    LaunchedEffect(campsite.id) {
        try {
            reviews = reviewRepository
                .getReviewsByCampsite(campsite.id)

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            errorMessage = "Could not load reviews"

        } finally {
            isLoading = false
        }
    }

    // Calculate the average rating
    val averageRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average()
    } else {
        0.0
    }

    val roundedRating = averageRating
        .roundToInt()
        .coerceIn(0, 5)

    // Sort reviews from newest to oldest
    val sortedReviews = reviews
        .sortedByDescending { it.createdAt }

    // Show only the two most recent reviews by default
    val recentReviews = if (showAllReviews) {
        sortedReviews
    } else {
        sortedReviews.take(2)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {

        Text(
            text = "Campsite Details",
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

            // Campsite information
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
                        text = campsite.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Location: ${campsite.location}"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = campsite.description)

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Amenities",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    campsite.amenities.forEach { amenity ->
                        Text(text = "• $amenity")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Maximum occupancy: " +
                                "${campsite.maxOccupancy} people"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lottery status
            Text(
                text = "Reservation Lottery",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isLotteryActive) {
                    "Lottery Active"
                } else {
                    "Lottery Inactive"
                },
                color = if (isLotteryActive) {
                    darkGreen
                } else {
                    Color.Gray
                },
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onJoinWaitingList,
                enabled = isLotteryActive,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = green,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = if (isLotteryActive) {
                        "Join Waiting List"
                    } else {
                        "Lottery Inactive"
                    }
                )
            }

            if (!isLotteryActive) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This lottery is currently unavailable.",
                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // Campsite reviews
            Text(
                text = "Campsite Reviews",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {

                isLoading -> {
                    Text("Loading reviews...")
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                reviews.isEmpty() -> {
                    Text("Not rated yet")
                }

                else -> {

                    // Average rating
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text = "${"%.1f".format(averageRating)}/5",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {

                            Text(
                                text = "★".repeat(roundedRating) +
                                        "☆".repeat(5 - roundedRating),
                                fontSize = 22.sp,
                                color = darkGreen
                            )

                            Text(
                                text = "${reviews.size} reviews",
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Individual reviews
                    recentReviews.forEach { review ->

                        val rating = review.rating.coerceIn(0, 5)

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
                                    text = "★".repeat(rating) +
                                            "☆".repeat(5 - rating),
                                    fontSize = 24.sp,
                                    color = darkGreen
                                )

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Text(
                                    text = review.comment,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (reviews.size > 2) {

                        TextButton(
                            onClick = {
                                showAllReviews = !showAllReviews
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = darkGreen
                            )
                        ) {
                            Text(
                                text = if (showAllReviews) {
                                    "Show Less"
                                } else {
                                    "View All ${reviews.size} Reviews"
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onWriteReview,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = green,
                    contentColor = Color.Black
                )
            ) {
                Text("Write a Review")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Back button stays at the bottom of the screen
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = green,
                contentColor = Color.Black
            )
        ) {
            Text("Back")
        }
    }
}