
/*
 * EntrantCampsiteDetailsScreen.kt
 *
 * This file is part of the View in our MVC architecture.
 * Here we show the campsite information, its reviews and
 * whether the entrant can join the lottery or not.
 *
 * Outstanding issues:
 * - We still need to test the lottery status and the Join
 *   Waiting List button with the actual waitlist functionality.
 */

package com.example.campsitereservationsystem.EntrantPath

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsitereservationsystem.Lottery
import com.example.campsitereservationsystem.LotteryRepository
import com.example.campsitereservationsystem.OwnerCampsitesPath.Campsite
import com.example.campsitereservationsystem.User
import com.example.campsitereservationsystem.review.Review
import com.example.campsitereservationsystem.review.ReviewRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
//Image Display libraries
import coil3.compose.AsyncImage
import com.example.campsitereservationsystem.ImageRepository
import androidx.compose.ui.layout.ContentScale

/**
 * This screen lets the entrant see the campsite details,
 * join the waiting list if the lottery is active and write a review.
 *
 * @param campsite The campsite we want to display.
 * @param isLotteryActive Whether the lottery is active or not.
 * @param reviewRepository The repository we use to get the reviews.
 * @param onJoinWaitingList Callback used when the entrant wants to join.
 * @param onWriteReview Callback used to open the review screen.
 * @param onBack Callback used to go back to the previous screen.
 */
@Composable
fun EntrantCampsiteDetailsScreen(
    user: User,
    campsite: Campsite,
    reviewRepository: ReviewRepository,
    onJoinWaitingList: () -> Unit,
    onWriteReview: () -> Unit,
    onBack: () -> Unit
) {

    BackHandler(onBack = onBack)

    val green = Color(0xFF819F5C)
    val darkGreen = Color(0xFF587437)
    val cardGray = Color(0xFFE0E0E0)

    val lotteryRepository = remember { LotteryRepository() }

    var lottery by remember(campsite.id) {
        mutableStateOf<Lottery?>(null)
    }

    var isLotteryLoading by remember(campsite.id) {
        mutableStateOf(true)
    }

    LaunchedEffect(campsite.id) {
        lotteryRepository.getLotteryByCampsiteID(campsite.id) { result ->
            lottery = result
            //isLotteryLoading = false
        }
    }

    //check lottery status here to and set isLotteryActive accordingly
    var isLotteryActive = false
    var isLotteryClosed = false
    var isLotteryFull = false
    var isLotteryCompleted = false

    lottery?.let {
        if (it.status == "OPEN") {
            isLotteryActive = true
        } else if (it.status == "CLOSED") {
            isLotteryClosed = true
        } else if (it.status == "FULL") {
            isLotteryFull = true
        }
        //last one not needed as the else{} handles it
        /*
         else if (it.status == "COMPLETED") {
            isLotteryCompleted = true
        }

             */
    }

    val scope = rememberCoroutineScope()

    // Here we store the reviews we get from Firebase
    var reviews by remember(campsite.id) {
        mutableStateOf<List<Review>>(emptyList())
    }

    // We use this to know if the user wants to see all the reviews
    var showAllReviews by remember(campsite.id) {
        mutableStateOf(false)
    }

    var isLoading by remember(campsite.id) {
        mutableStateOf(true)
    }

    var errorMessage by remember(campsite.id) {
        mutableStateOf<String?>(null)
    }

    // Hold image ByteArray and reference ImageRepository
    val imageRepo = remember { ImageRepository() }
    var imageBytes by remember (campsite.id){mutableStateOf<ByteArray?>(null)}

    // We get the reviews using the campsite ID
    LaunchedEffect(campsite.id) {
    imageRepo.getCampsiteImage(campsite.id) { bytes ->
        imageBytes = bytes
    }

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

    // We put the newest reviews first
    val sortedReviews = reviews
        .sortedByDescending { it.createdAt }

    // For now, we show two reviews unless the user wants to see all of them
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
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // We make this part scrollable so the user can see all the information
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {

            // Here we show the main information of the campsite
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

                    val bytes = imageBytes
                    if(bytes != null){
                        AsyncImage(
                            model = bytes,
                            contentDescription = "Campsite Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                            )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

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

                    // We go through the list and show each amenity
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

            // Here we show if the lottery is active or not
            Text(
                text = "Reservation Lottery",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()

            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isLotteryActive) {
                    "Lottery Active"
                } else if (isLotteryClosed) {
                    "Lottery Registration has not started yet"
                } else if (isLotteryFull) {
                    "Lottery is full"
                } else {
                    "There is no active lottery"
                },
                color = if (isLotteryActive) {
                    darkGreen
                } else {
                    Color.Gray
                },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // The entrant can only press this button if the lottery is active
            Button(
                onClick = {
                    lottery?.let { currentLottery ->
                        joinLottery(
                            newEntrant = user,
                            campsite = campsite,
                            lottery = currentLottery,
                            isLotteryActive = isLotteryActive,
                            lotteryRepository = lotteryRepository,
                            scope = scope
                        )
                    }
                },
                enabled = isLotteryActive && lottery != null,
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

            /*
            // If the lottery is inactive, we let the entrant know
            if (!isLotteryActive) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This lottery is currently unavailable.",
                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    )
                )
            }

             */

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // Here we show the reviews of the campsite
            Text(
                text = "Campsite Reviews",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // We display different messages depending on
            // whether the reviews have been loaded or not
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

                    // We show the average as a number and as stars
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

                    // Here we create a card for each review we want to show
                    recentReviews.forEach { review ->

                        // We make sure the rating stays between 0 and 5
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

                    // If there are more than two reviews,
                    // we give the user the option to see all of them
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

            // This button takes the entrant to the screen
            // where they can write a review
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

        // We keep the Back button at the bottom
        // so the user does not have to scroll to go back
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



fun joinLottery(
    newEntrant: User,
    campsite: Campsite,
    lottery: Lottery,
    isLotteryActive: Boolean,
    lotteryRepository: LotteryRepository,
    scope: CoroutineScope
) {
    if (!isLotteryActive) return

    lotteryRepository.getWaitingList(
        campsiteID = campsite.id,
        onResult = { waitingList ->

            // Check if the entrant has already joined
            if (waitingList.any { it.id == newEntrant.id }) {
                println("User is already on the waiting list")
                return@getWaitingList
            }

            // Add the entrant to the waiting list
            val updatedWaitingList = waitingList + newEntrant

            scope.launch {
                try {
                    // First, update the waiting list in Firebase
                    lotteryRepository.updateList(
                        lottery,
                        updatedWaitingList,
                        "waitingList"
                    )

                    println("Waiting list successfully updated")

                    // Check capacity using the updated waiting list
                    if (
                        lottery.status == "OPEN" &&
                        lottery.capacity > 0 &&
                        updatedWaitingList.size >= lottery.capacity
                    ) {
                        // Update the status in Firebase
                        lotteryRepository.updateStatus(
                            lottery,
                            "FULL"
                        )

                        println("Lottery status successfully changed to FULL")
                    } else {
                        println("Lottery has not reached capacity")
                    }

                    println("Successfully joined waiting list")

                } catch (e: Exception) {
                    println("Error joining lottery: ${e.message}")
                }
            }
        },
        onError = {
            println("Could not retrieve waiting list")
        }
    )
}
