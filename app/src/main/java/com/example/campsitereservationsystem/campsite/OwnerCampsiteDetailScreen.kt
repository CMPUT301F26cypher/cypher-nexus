package com.example.campsitereservationsystem.campsite

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OwnerCampsiteDetailsScreen(
    campsite: Campsite,
    onViewQRCode: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        //Arrow as the back button
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
                text = "Owner Campsite Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        //Campsite name
        Text(
            text = campsite.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        //Location
        Text(text = campsite.location)

        Spacer(modifier = Modifier.height(16.dp))

        //Description
        Text(text = campsite.description)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Amenities",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        //Show each amenity
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

        Text(
            text = "Promotional QR Code",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Share your campsite with prospective guests. They can scan this code to view campsite details."
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onViewQRCode,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View QR Code")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Use it on posters, flyers, or social media.",
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}