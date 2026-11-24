package com.example.campsitereservationsystem.qr

import androidx.compose.runtime.LaunchedEffect
import com.example.campsitereservationsystem.review.Review
import com.example.campsitereservationsystem.review.ReviewRepository
import kotlinx.coroutines.CancellationException
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.DisposableEffect
import com.example.campsitereservationsystem.campsite.CampsiteRepository
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.ui.text.font.FontWeight


@Composable
fun QRCodeScannerScreen(
    onQRCodeScanned: (String) ->Unit,
    onBack:() ->Unit,
    onCampsiteSelected: (String) -> Unit,
    recentlyViewedIds: List<String>,
    reviewRepository: ReviewRepository
) {
    val context = LocalContext.current
    val scanner = remember(context) {
        GmsBarcodeScanning.getClient(context)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }
    //the filter to search for the campsites
    val repository = remember { CampsiteRepository() }

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
        searchQuery.isNotBlank() &&
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Button(onClick = onBack) {
            Text("← Back")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Find a Campsite"
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search campsites by name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isBlank() && recentlyViewedCampsites.isNotEmpty()) {

            Text(
                text = "Recently Viewed",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            recentlyViewedCampsites.forEach { campsite ->
                Card(
                    onClick = {
                        onCampsiteSelected(campsite.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = campsite.name,
                            fontWeight = FontWeight.Bold
                        )

                        Text(text = "Location: ${campsite.location}")

                        Text(
                            text = "Maximum occupancy: ${campsite.maxOccupancy}"
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredCampsites, key = { it.id }) { campsite ->
                Card(
                    onClick = {
                        onCampsiteSelected(campsite.id)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = campsite.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Location: ${campsite.location}"
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Maximum occupancy: ${campsite.maxOccupancy} people"
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        CampsiteRating(
                            campsiteId = campsite.id,
                            reviewRepository = reviewRepository
                        )
                    }
                }

            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Scan QR Code")

        Button(
            onClick = {
                errorMessage = null
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        val content = barcode.rawValue

                        if (content != null) {
                            onQRCodeScanned(content)
                        } else {
                            errorMessage = "QR code is empty"
                        }
                    }
                    .addOnFailureListener {
                        errorMessage = "Unable to scan QR code"
                    }
            }
        ) {
            Text("Open QR Scanner")
        }

        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message)
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

    val roundedRating = averageRating.roundToInt()
        .coerceIn(0, 5)

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "★".repeat(roundedRating) +
                    "☆".repeat(5 - roundedRating),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = when {
                isLoading -> "Loading..."
                hasError -> "Rating unavailable"
                reviews.isEmpty() -> "Not rated yet"
                else -> "${"%.1f".format(averageRating)}/5 (${reviews.size})"
            },
            style = MaterialTheme.typography.bodySmall
        )
    }
}