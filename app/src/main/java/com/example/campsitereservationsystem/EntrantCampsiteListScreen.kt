package com.example.campsitereservationsystem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsitereservationsystem.campsite.Campsite
import com.example.campsitereservationsystem.campsite.CampsiteRepository
import com.example.campsitereservationsystem.review.Review
import com.example.campsitereservationsystem.review.ReviewRepository
import kotlinx.coroutines.CancellationException
import kotlin.math.roundToInt

private val CampsiteGreen = Color(0xFF819F5C)

@Composable
fun EntrantCampsiteListScreen(
    onBack: () -> Unit,
    onCampsiteSelected: (String) -> Unit,
    recentlyViewedIds: List<String>,
    reviewRepository: ReviewRepository
) {
    val repository = remember {
        CampsiteRepository()
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    DisposableEffect(repository) {
        val listener = repository.watchCampsites()

        onDispose {
            listener.remove()
        }
    }

    val filteredCampsites = repository.campsites.filter { campsite ->
        campsite.name.contains(
            searchQuery.trim(),
            ignoreCase = true
        )
    }

    val recentlyViewedCampsites = recentlyViewedIds.mapNotNull { id ->
        repository.campsites.find { it.id == id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Browse Campsites",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search campsite name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CampsiteGreen,
                unfocusedBorderColor = CampsiteGreen,
                focusedLabelColor = CampsiteGreen,
                cursorColor = CampsiteGreen
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (
                searchQuery.isBlank() &&
                recentlyViewedCampsites.isNotEmpty()
            ) {
                item {
                    Text(
                        text = "Recently Viewed",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(
                    recentlyViewedCampsites,
                    key = { "recent_${it.id}" }
                ) { campsite ->

                    CampsiteCard(
                        campsite = campsite,
                        reviewRepository = reviewRepository,
                        onClick = {
                            onCampsiteSelected(campsite.id)
                        }
                    )
                }
            }

            item {
                Text(
                    text = if (searchQuery.isBlank()) {
                        "All Campsites"
                    } else {
                        "Search Results"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(
                filteredCampsites,
                key = { "all_${it.id}" }
            ) { campsite ->

                CampsiteCard(
                    campsite = campsite,
                    reviewRepository = reviewRepository,
                    onClick = {
                        onCampsiteSelected(campsite.id)
                    }
                )
            }

            if (filteredCampsites.isEmpty()) {
                item {
                    Text("No campsites found")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = CampsiteGreen,
                contentColor = Color.Black
            )
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun CampsiteCard(
    campsite: Campsite,
    reviewRepository: ReviewRepository,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE0E0E0),
            contentColor = Color.Black
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = campsite.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Location: ${campsite.location}")

            Text(
                "Max occupancy: ${campsite.maxOccupancy}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            CampsiteRating(
                campsiteId = campsite.id,
                reviewRepository = reviewRepository
            )

            Button(
                onClick = onClick,
                modifier = Modifier.align(
                    Alignment.End
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CampsiteGreen,
                    contentColor = Color.Black
                )
            ) {
                Text("Details")
            }
        }
    }
}

@Composable
private fun CampsiteRating(
    campsiteId: String,
    reviewRepository: ReviewRepository
) {
    var reviews by remember(campsiteId) {
        mutableStateOf<List<Review>>(emptyList())
    }

    var isLoading by remember(campsiteId) {
        mutableStateOf(true)
    }

    var hasError by remember(campsiteId) {
        mutableStateOf(false)
    }

    LaunchedEffect(campsiteId) {
        try {
            reviews = reviewRepository
                .getReviewsByCampsite(campsiteId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            hasError = true
        } finally {
            isLoading = false
        }
    }

    val averageRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average()
    } else {
        0.0
    }

    val roundedRating = averageRating
        .roundToInt()
        .coerceIn(0, 5)

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "★".repeat(roundedRating) +
                    "☆".repeat(5 - roundedRating),
            color = Color(0xFF587437),
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = when {
                isLoading -> "Loading..."
                hasError -> "Rating unavailable"
                reviews.isEmpty() -> "Not rated yet"
                else -> "${"%.1f".format(averageRating)}/5 (${reviews.size})"
            },
            fontSize = 12.sp
        )
    }
}