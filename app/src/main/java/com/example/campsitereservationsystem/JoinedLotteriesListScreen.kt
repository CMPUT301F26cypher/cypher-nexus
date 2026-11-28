
/*
 * JoinedLotteriesListScreen.kt
 *
 * This file is part of the View in our MVC architecture.
 * Here we show all the lotteries the entrant has joined,
 * including the ones joined using our WOW Factor.
 *
 * Outstanding issues:
 * - We still need to test the screen with actual registrations.
 * - For now, we refresh the list when the entrant opens the screen.
 */

package com.example.campsitereservationsystem

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException

private val JoinedLotteryGreen = Color(0xFF819F5C)

/**
 * Here we show the lotteries the current entrant has joined.
 *
 * @param user The current registered user.
 * @param lotteryRepository The repository we use to get the lotteries.
 * @param onViewCampsite Callback used to open the campsite details.
 * @param onBack Callback used to go back to the previous screen.
 */
@Composable
fun JoinedLotteriesListScreen(
    user: User?,
    lotteryRepository: LotteryRepository,
    onViewCampsite: (String) -> Unit,
    onBack: () -> Unit
) {

    BackHandler(onBack = onBack)

    // Here we store the lotteries the entrant has joined
    var joinedLotteries by remember(user?.id) {
        mutableStateOf<List<Lottery>>(emptyList())
    }

    var isLoading by remember(user?.id) {
        mutableStateOf(true)
    }

    var errorMessage by remember(user?.id) {
        mutableStateOf("")
    }

    var refreshCount by remember {
        mutableStateOf(0)
    }

    // We get the lotteries using the ID of the current user
    LaunchedEffect(user?.id, lotteryRepository, refreshCount) {

        isLoading = true
        errorMessage = ""
        joinedLotteries = emptyList()

        try {

            if (user == null || user.id.isBlank()) {

                errorMessage =
                    "You need to sign in to view your lotteries."

            } else {

                joinedLotteries =
                    lotteryRepository.getJoinedLotteries(user.id)
            }

        } catch (cancelled: CancellationException) {

            throw cancelled

        } catch (error: Exception) {

            errorMessage = "Unable to load joined lotteries."

        } finally {

            isLoading = false
        }
    }

    val greenButtonColors = ButtonDefaults.buttonColors(
        containerColor = JoinedLotteryGreen,
        contentColor = Color.Black
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {

        Text(
            text = "Joined Lotteries",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Here we show the information depending on the current state
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            when {

                // We show a loading indicator while getting the lotteries
                isLoading -> {

                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = JoinedLotteryGreen
                    )
                }

                // If something goes wrong, we let the entrant try again
                errorMessage.isNotEmpty() -> {

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = errorMessage,
                            textAlign = TextAlign.Center
                        )

                        if (user != null && user.id.isNotBlank()) {

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    refreshCount++
                                },
                                colors = greenButtonColors
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }

                // We let the entrant know if they have not joined any lotteries
                joinedLotteries.isEmpty() -> {

                    Text(
                        text = "You have not joined any lotteries yet.",
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                }

                // Here we display all the lotteries the entrant has joined
                else -> {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        item {

                            Text(
                                text = "${joinedLotteries.size} joined lotteries",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        items(joinedLotteries) { lottery ->

                            JoinedLotteryCard(
                                lottery = lottery,
                                userID = user?.id.orEmpty(),
                                onViewCampsite = onViewCampsite
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // We keep the Back button at the bottom left
        Button(
            onClick = onBack,
            colors = greenButtonColors
        ) {
            Text("Back")
        }
    }
}

/**
 * Here we show the information of each joined lottery.
 *
 * @param lottery The lottery the entrant has joined.
 * @param userID The ID of the current entrant.
 * @param onViewCampsite Callback used to open the campsite details.
 */
@Composable
private fun JoinedLotteryCard(
    lottery: Lottery,
    userID: String,
    onViewCampsite: (String) -> Unit
) {

    val campsite = lottery.campsite

    // We check if the entrant was selected as a winner
    val isWinner = lottery.winners.any {
        it.id == userID
    }

    val entrantStatus = when {

        lottery.status == "COMPLETED" && isWinner ->
            "Selected as a winner"

        lottery.status == "COMPLETED" ->
            "Not selected"

        lottery.status == "DRAWING" ->
            "Lottery in progress"

        else ->
            "Joined Waiting List"
    }

    Card(
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
                text = campsite?.name ?: "Campsite",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Location: ${campsite?.location ?: "Unavailable"}"
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Lottery Status: ${lottery.status}"
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Number of Winners: ${lottery.numWinners}"
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Entrants: ${lottery.waitingList.size}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = entrantStatus,
                color = Color(0xFF587437),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // We use the existing campsite details screen
            Button(
                onClick = {
                    onViewCampsite(lottery.campsiteID)
                },
                enabled = lottery.campsiteID.isNotBlank(),
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JoinedLotteryGreen,
                    contentColor = Color.Black
                )
            ) {
                Text("View Campsite")
            }
        }
    }
}
