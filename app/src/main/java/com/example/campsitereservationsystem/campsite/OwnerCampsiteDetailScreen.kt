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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

//IMAGE UPLOAD IMPORTS
import com.example.campsitereservationsystem.ImageRepository
import com.example.campsitereservationsystem.ImagePickerButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import coil3.compose.AsyncImage

@Composable
fun OwnerCampsiteDetailsScreen(
    campsite: Campsite,
    onViewQRCode: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    //initializes imageRepository and stores campsite image uri
    //val imageRepository = remember{ ImageRepository() }
    val imageUri by remember(campsite.id){
        mutableStateOf(campsite.imageUrl ?: "")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
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
        }
            //Display Image if imageUri available
        item {
            if (imageUri.isNotEmpty()) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Campsite Image",
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

            //Campsite name
        item {
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
        }
            //Show each amenity
            items(campsite.amenities) { amenity ->
                Text(text = "• $amenity")
            }
        item {
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
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}