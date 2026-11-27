
/*
 * EntrantCampsiteListScreen.kt
 *
 * This file is part of the View in our MVC architecture.
 * Here we allow the entrant to search for campsites by name,
 * location, number of guests and available dates.
 *
 * We also show recently viewed campsites, their ratings
 * and give the entrant the option to join multiple waiting lists.
 *
 * Outstanding issues:
 * - We still need to test the waiting list registration with Firebase.
 * - For now, the dates are limited to May 2027.
 */

// US 01.01.04 - View campsite summary information.
// US 01.01.01 - Search for campsites.

package com.example.campsitereservationsystem.EntrantPath

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsitereservationsystem.OwnerCampsitesPath.Campsite
import com.example.campsitereservationsystem.OwnerCampsitesPath.CampsiteController
import com.example.campsitereservationsystem.OwnerCampsitesPath.CampsiteRepository
import com.example.campsitereservationsystem.review.Review
import com.example.campsitereservationsystem.review.ReviewRepository
import kotlinx.coroutines.CancellationException
import kotlin.math.roundToInt

private val CampsiteGreen = Color(0xFF819F5C)

/**
 * This screen allows the entrant to browse campsites,
 * search for specific ones and open their details.
 *
 * @param onBack Callback used to go back to the previous screen.
 * @param onCampsiteSelected Callback used to open a campsite using its ID.
 * @param recentlyViewedIds IDs of the campsites the entrant recently viewed.
 * @param reviewRepository The repository we use to get the reviews.
 * @param repository The repository we use to get the campsites.
 * @param controller The controller we use to filter the campsites.
 * @param onJoinAllWaitlists Callback used to join multiple waiting lists.
 * @param isJoiningAll Tells us if the entrant is joining waiting lists.
 */
@Composable
fun EntrantCampsiteListScreen(
    onBack: () -> Unit,
    onCampsiteSelected: (String) -> Unit,
    recentlyViewedIds: List<String>,
    reviewRepository: ReviewRepository,
    repository: CampsiteRepository,
    controller: CampsiteController,
    onJoinAllWaitlists: ((List<String>) -> Unit)? = null,
    isJoiningAll: Boolean = false
) {

    // Here we store what the entrant enters in the search bar and filters
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    var showFilters by rememberSaveable {
        mutableStateOf(false)
    }

    var location by rememberSaveable {
        mutableStateOf("")
    }

    var guestText by rememberSaveable {
        mutableStateOf("")
    }

    var checkInText by rememberSaveable {
        mutableStateOf("")
    }

    var checkOutText by rememberSaveable {
        mutableStateOf("")
    }

    // Here we store the IDs of the campsites selected
    // when the entrant wants to join multiple waiting lists
    var pendingWaitlistIds by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var showJoinConfirmation by remember {
        mutableStateOf(false)
    }

    var showUnavailableDialog by remember {
        mutableStateOf(false)
    }

    BackHandler(onBack = onBack)

    // We listen for changes in Firebase and stop listening
    // when the user leaves this screen
    DisposableEffect(repository) {
        val listener = repository.watchCampsites()

        onDispose {
            listener.remove()
        }
    }

    // We convert the guests and dates to integers
    val guests = guestText.trim().toIntOrNull()
    val checkIn = checkInText.trim().toIntOrNull()
    val checkOut = checkOutText.trim().toIntOrNull()

    val hasDates =
        checkInText.isNotBlank() ||
                checkOutText.isNotBlank()

    // Before filtering, we make sure the information is valid
    val error = when {

        guestText.isNotBlank() &&
                (guests == null || guests < 1) ->
            "Enter a valid number of guests"

        hasDates &&
                (checkIn == null || checkOut == null) ->
            "Enter check in and check out dates"

        checkIn != null && checkOut != null &&
                (checkIn !in 1..31 ||
                        checkOut !in 1..31 ||
                        checkOut <= checkIn) ->
            "Enter valid dates between 1 and 31"

        else -> ""
    }

    // We use the controller to filter by location, guests and dates.
    // After that, we also filter by the name entered in the search bar.
    val filteredCampsites = if (error.isEmpty()) {

        controller.filterCampsites(
            location = location,
            guests = guests,
            checkIn = checkIn,
            checkOut = checkOut
        ).filter { campsite ->

            campsite.name.contains(
                searchQuery.trim(),
                ignoreCase = true
            )
        }

    } else {
        emptyList()
    }

    // Here we check if the entrant is using any of the filters
    val hasActiveFilters =
        location.isNotBlank() ||
                guestText.isNotBlank() ||
                hasDates

    val isSearching =
        searchQuery.isNotBlank() || hasActiveFilters

    // We get the IDs of the matching campsites
    // and make sure we do not have any empty or repeated IDs
    val matchingCampsiteIds = filteredCampsites
        .map { it.id }
        .filter { it.isNotBlank() }
        .distinct()

    // We only enable the button if there are matching campsites,
    // there are no errors and we are not already joining waiting lists
    val canJoinMatchingWaitlists =
        error.isEmpty() &&
                matchingCampsiteIds.isNotEmpty() &&
                isSearching &&
                !isJoiningAll &&
                onJoinAllWaitlists != null

    // We use the recently viewed IDs to find the campsites
    // that the entrant has already opened
    val recentlyViewedCampsites =
        recentlyViewedIds.mapNotNull { id ->

            repository.campsites.find {
                it.id == id
            }
        }

    // We use the same colors for the fields and buttons
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CampsiteGreen,
        unfocusedBorderColor = CampsiteGreen,
        focusedLabelColor = CampsiteGreen,
        cursorColor = CampsiteGreen
    )

    val greenButtonColors = ButtonDefaults.buttonColors(
        containerColor = CampsiteGreen,
        contentColor = Color.Black
    )

    // We ask the entrant for confirmation before requesting
    // to join all the matching waiting lists
    if (showJoinConfirmation) {

        AlertDialog(
            onDismissRequest = {
                showJoinConfirmation = false
                pendingWaitlistIds = emptyList()
            },

            title = {
                Text("Join Matching Waitlists?")
            },

            text = {
                Text(
                    "You are requesting to join the eligible " +
                            "waiting lists for " +
                            "${pendingWaitlistIds.size} " +
                            "matching campsites.\n\n" +
                            "Only eligible waiting lists can be joined.\n\n" +
                            "Do you want to continue?"
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        // We keep a copy of the IDs before clearing the list
                        val campsiteIds =
                            pendingWaitlistIds.toList()

                        showJoinConfirmation = false
                        pendingWaitlistIds = emptyList()

                        // We pass the matching IDs to CampsiteRoutes
                        // so the entrant can join the waiting lists
                        if (onJoinAllWaitlists != null) {

                            onJoinAllWaitlists.invoke(campsiteIds)

                        } else {

                            showUnavailableDialog = true
                        }
                    },
                    enabled = !isJoiningAll,
                    colors = greenButtonColors
                ) {
                    Text("Confirm")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showJoinConfirmation = false
                        pendingWaitlistIds = emptyList()
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = Color(0xFF587437)
                    )
                }
            }
        )
    }

    // We keep this message in case the callback is not available
    if (showUnavailableDialog) {

        AlertDialog(
            onDismissRequest = {
                showUnavailableDialog = false
            },

            title = {
                Text("Waitlist Registration Unavailable")
            },

            text = {
                Text(
                    "Waitlist registration is not available yet. " +
                            "No waiting lists have been joined."
                )
            },

            confirmButton = {

                Button(
                    onClick = {
                        showUnavailableDialog = false
                    },
                    colors = greenButtonColors
                ) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .padding(16.dp)
    ) {

        Text(
            text = "Browse Campsites",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Here we have the search bar and the button to open the filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                label = {
                    Text("Search campsite name")
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = fieldColors
            )

            // We use + to show the filters and - to hide them
            FilledIconButton(
                onClick = {
                    showFilters = !showFilters
                },
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = CampsiteGreen,
                    contentColor = Color.Black
                )
            ) {

                Text(
                    text = if (showFilters) "−" else "+",
                    fontSize = 28.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // We use a LazyColumn so the entrant can scroll through the campsites
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // We only show the extra filters when the entrant opens them
            if (showFilters) {

                item {

                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        // Location filter
                        OutlinedTextField(
                            value = location,
                            onValueChange = {
                                location = it
                            },
                            label = {
                                Text("Location")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = fieldColors
                        )

                        // Number of guests filter
                        OutlinedTextField(
                            value = guestText,
                            onValueChange = {
                                guestText = it
                            },
                            label = {
                                Text("Number of Guests")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            colors = fieldColors
                        )

                        // For now, we only work with dates from May 2027
                        Text(
                            text = "Open Dates — May 2027",
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            OutlinedTextField(
                                value = checkInText,
                                onValueChange = {
                                    checkInText = it
                                },
                                label = {
                                    Text("Check In")
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = fieldColors
                            )

                            OutlinedTextField(
                                value = checkOutText,
                                onValueChange = {
                                    checkOutText = it
                                },
                                label = {
                                    Text("Check Out")
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = fieldColors
                            )
                        }

                        // We clear the filters without clearing the search bar
                        Button(
                            onClick = {
                                location = ""
                                guestText = ""
                                checkInText = ""
                                checkOutText = ""
                            },
                            colors = greenButtonColors,
                            modifier = Modifier.align(
                                Alignment.End
                            )
                        ) {
                            Text("Clear Filters")
                        }
                    }
                }
            }

            // If the information is not valid, we show the error
            if (error.isNotEmpty()) {

                item {

                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // WOW Factor
            // Here we give the entrant the option to join
            // multiple matching waiting lists at once
            if (isSearching) {

                item {

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        // We show how many campsites match the search
                        Text(
                            text = "${matchingCampsiteIds.size} " +
                                    "matching campsites",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // We save the matching IDs and ask for confirmation
                        Button(
                            onClick = {

                                pendingWaitlistIds =
                                    matchingCampsiteIds.toList()

                                showJoinConfirmation = true
                            },
                            enabled = canJoinMatchingWaitlists,
                            modifier = Modifier.fillMaxWidth(),
                            colors = greenButtonColors
                        ) {

                            // We show the loading indicator while
                            // the entrant is joining the waiting lists
                            if (isJoiningAll) {

                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = CampsiteGreen
                                )

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

                                Text("Joining Waitlists...")

                            } else {

                                Text(
                                    "Join All Matching Waitlists " +
                                            "(${matchingCampsiteIds.size})"
                                )
                            }
                        }
                    }
                }
            }

            // We only show recently viewed campsites
            // when the entrant is not searching
            if (
                !isSearching &&
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

            // We change the title depending on whether
            // the entrant is searching or just browsing
            item {

                Text(
                    text = if (isSearching) {
                        "Search Results"
                    } else {
                        "All Campsites"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Here we create a card for each matching campsite
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

            // If there are no matching campsites, we let the entrant know
            if (
                filteredCampsites.isEmpty() &&
                error.isEmpty()
            ) {

                item {
                    Text("No matching campsites")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // We keep the Back button at the bottom of the screen
        Button(
            onClick = onBack,
            colors = greenButtonColors
        ) {
            Text("Back")
        }
    }
}

/**
 * Here we create the card that we use to display each campsite.
 * We show its name, location, maximum occupancy and rating.
 *
 * @param campsite The campsite we want to display.
 * @param reviewRepository The repository we use to get its reviews.
 * @param onClick Callback used to open the campsite details.
 */
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

            // We use this function to show the rating of the campsite
            CampsiteRating(
                campsiteId = campsite.id,
                reviewRepository = reviewRepository
            )

            // This button takes the entrant to the campsite details
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

/**
 * This function gets the reviews of a campsite and
 * shows its average rating and number of reviews.
 *
 * @param campsiteId The ID of the campsite we want the reviews from.
 * @param reviewRepository The repository we use to get the reviews.
 */
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

    // We get the reviews from Firebase using the campsite ID
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

    // We calculate the average using all the reviews
    val averageRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average()
    } else {
        0.0
    }

    // We round the rating to know how many stars to fill
    val roundedRating = averageRating
        .roundToInt()
        .coerceIn(0, 5)

    // Here we show the stars, the average and the number of reviews
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

                else ->
                    "${"%.1f".format(averageRating)}/5 (${reviews.size})"
            },
            fontSize = 12.sp
        )
    }
}
