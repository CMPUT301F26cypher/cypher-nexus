package com.example.campsitereservationsystem.campsite

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    val green = Color(0xFF819F5C)
    val cardGray = Color(0xFFE0E0E0)

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = green,
        contentColor = Color.Black
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {

        // Screen title
        Text(
            text = "Owner Campsite Details",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Campsite information
            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardGray,
                        contentColor = Color.Black
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        // Campsite name
                        Text(
                            text = campsite.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        // Campsite location
                        Text(
                            text = campsite.location,
                            fontSize = 16.sp
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        // Campsite description
                        Text(
                            text = campsite.description
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        HorizontalDivider(
                            color = Color.LightGray
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        // Campsite amenities
                        Text(
                            text = "Amenities",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        if (campsite.amenities.isEmpty()) {

                            Text("No amenities listed")

                        } else {

                            campsite.amenities.forEach { amenity ->

                                Text(
                                    text = "• $amenity"
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        HorizontalDivider(
                            color = Color.LightGray
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        // Maximum occupancy
                        Text(
                            text = "Maximum occupancy: " +
                                    "${campsite.maxOccupancy} people"
                        )
                    }
                }
            }

            item {

                HorizontalDivider()
            }

            // Promotional QR Code section
            item {

                Text(
                    text = "Promotional QR Code",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "Share your campsite with prospective " +
                            "guests. They can scan this code to view " +
                            "campsite details."
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                // Open the existing QR Code screen
                Button(
                    onClick = onViewQRCode,
                    modifier = Modifier.fillMaxWidth(),
                    colors = buttonColors,
                    shape = RoundedCornerShape(8.dp)
                ) {

                    Text("View QR Code")
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Use it on posters, flyers, or social media.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Back button
        Button(
            onClick = onBack,
            colors = buttonColors,
            shape = RoundedCornerShape(8.dp)
        ) {

            Text("Back")
        }
    }
}