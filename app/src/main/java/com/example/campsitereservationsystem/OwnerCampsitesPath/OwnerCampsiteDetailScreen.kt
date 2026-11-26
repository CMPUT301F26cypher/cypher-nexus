
/*
 * OwnerCampsiteDetailsScreen.kt
 *
 * This file is part of the View in our MVC architecture.
 * Here we allow the owner to see the information of their
 * campsite, such as the name, location, description,
 * amenities and maximum occupancy.
 *
 * We also have a button that takes the owner to the QR Code
 * screen so they can promote their campsite to other users.
 *
 * Outstanding issues:
 * - For now, we do not display the campsite image,
 *   rules and policies or tags on this screen.
 */

package com.example.campsitereservationsystem.OwnerCampsitesPath

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

/**
 * This screen allows the owner to see the details of
 * a campsite and access its promotional QR Code.
 *
 * @param campsite The campsite we want to display.
 * @param onViewQRCode Callback used to open the QR Code screen.
 * @param onBack Callback used to go back to the previous screen.
 */
@Composable
fun OwnerCampsiteDetailsScreen(
    campsite: Campsite,
    onViewQRCode: () -> Unit,
    onBack: () -> Unit
) {

    BackHandler(onBack = onBack)

    val green = Color(0xFF819F5C)
    val cardGray = Color(0xFFE0E0E0)

    // We use the same colors for all the buttons on this screen
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

        // We use a LazyColumn so the owner can scroll
        // through the information of the campsite
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Here we display the main information of the campsite
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

                        // If the campsite does not have any amenities,
                        // we display a message instead of an empty list
                        if (campsite.amenities.isEmpty()) {

                            Text("No amenities listed")

                        } else {

                            // We go through the list and display each amenity
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

                        // Here we show how many people can fit in the campsite
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

            // Here we explain to the owner what the promotional
            // QR Code is used for and how other users can use it
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

                // We use the callback to open the QR Code screen
                // instead of handling the QR Code logic here
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

        // We keep the Back button outside the LazyColumn
        // so the owner can always go back without scrolling
        Button(
            onClick = onBack,
            colors = buttonColors,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Back")
        }
    }
}
