package com.example.campsitereservationsystem.qr

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsitereservationsystem.campsite.Campsite
import com.example.campsitereservationsystem.review.Review
import com.example.campsitereservationsystem.review.ReviewRepository
import kotlin.coroutines.cancellation.CancellationException

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
    //in reviews we store the reviews from the firebase
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
    //Launched initiates a coroutine
    LaunchedEffect(campsite.id) {
        try {
            //getReviewsByCampsite is the function that consults the firebase
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
    //avarageRating calculates the avg of the rating
    val averageRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average()
    } else {
        0.0
    }
    //We only show up to 2 reviews
    val sortedReviews = reviews
        .sortedByDescending { it.createdAt }

    val recentReviews = if (showAllReviews) {
        sortedReviews
    } else {
        sortedReviews.take(2)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back"
                )
            }

            Text(
                text = "Campsite Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Campsite information
        Text(
            text = campsite.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = campsite.location)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = campsite.description)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Amenities",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        campsite.amenities.forEach { amenity ->
            Text(text = "• $amenity")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Maximum occupancy: ${campsite.maxOccupancy} people"
        )

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        //Lottery status
        Text(
            text = "Reservation Lottery",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isLotteryActive) {
                "Lottery Active"
            } else {
                "Lottery Inactive"
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onJoinWaitingList,
            enabled = isLotteryActive,
            modifier = Modifier.fillMaxWidth()
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

        Text(
            text = "Campsite Reviews",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

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
                Text(
                    text = "Average rating: ${"%.1f".format(averageRating)}/5",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text("${reviews.size} reviews")
                Spacer(modifier = Modifier.height(16.dp))

                recentReviews.forEach { review ->

                    Text(
                        text = "★".repeat(review.rating) +
                                "☆".repeat(5 - review.rating),
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = review.comment,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (reviews.size > 2) {
                    TextButton(
                        onClick = {
                            showAllReviews = !showAllReviews
                        },
                        modifier = Modifier.fillMaxWidth()
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
        Button(
            onClick = onWriteReview,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Write a Review")
        }
    }
}