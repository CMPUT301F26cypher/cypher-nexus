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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsitereservationsystem.campsite.Campsite

@Composable
fun EntrantCampsiteDetailsScreen(
    campsite: Campsite,
    isLotteryActive: Boolean,
    onJoinWaitingList: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

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
    }
}