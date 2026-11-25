package com.example.campsitereservationsystem.campsite

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OwnerCampsitesScreen(
    campsites: List<Campsite>,
    onCampsiteClick: (Campsite) -> Unit,
    onAddCampsite: () -> Unit,
    onEditCampsite: (Campsite) -> Unit,
    onDeleteCampsite: (Campsite) -> Unit,
    onBack: () -> Unit,
    onBookingCampsite: ((Campsite) -> Unit)? = null
) {

    val green = Color(0xFF819F5C)
    val darkGreen = Color(0xFF587437)
    val cardGray = Color(0xFFE0E0E0)

    var campsiteToDelete by remember {
        mutableStateOf<Campsite?>(null)
    }

    BackHandler {
        if (campsiteToDelete != null) {
            campsiteToDelete = null
        } else {
            onBack()
        }
    }

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
            text = "My Campsites",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Register a new campsite
        Button(
            onClick = onAddCampsite,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            ),
            colors = buttonColors
        ) {
            Text("Register Campsite")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // List of campsites owned by the current owner
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (campsites.isEmpty()) {
                item {
                    Text(
                        text = "You have no registered campsites."
                    )
                }
            }

            items(
                items = campsites,
                key = { it.id }
            ) { campsite ->

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

                        // Open campsite details
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCampsiteClick(campsite)
                                }
                        ) {

                            Text(
                                text = campsite.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = campsite.location,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            // Edit campsite
                            Button(
                                onClick = {
                                    onEditCampsite(campsite)
                                },
                                modifier = Modifier.weight(1f),
                                colors = buttonColors,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 4.dp,
                                    vertical = 8.dp
                                )
                            ) {
                                Text(
                                    text = "Edit",
                                    fontSize = 13.sp
                                )
                            }

                            // Request campsite deletion
                            Button(
                                onClick = {
                                    campsiteToDelete = campsite
                                },
                                modifier = Modifier.weight(1f),
                                colors = buttonColors,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 4.dp,
                                    vertical = 8.dp
                                )
                            ) {
                                Text(
                                    text = "Delete",
                                    fontSize = 13.sp
                                )
                            }

                            // Open campsite booking
                            Button(
                                onClick = {
                                    onBookingCampsite?.invoke(campsite)
                                },
                                enabled = onBookingCampsite != null,
                                modifier = Modifier.weight(1f),
                                colors = buttonColors,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 4.dp,
                                    vertical = 8.dp
                                )
                            ) {
                                Text(
                                    text = "Booking",
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Back button stays at the bottom
        Button(
            onClick = onBack,
            colors = buttonColors,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Back")
        }
    }

    // Confirmation dialog before deleting a campsite
    campsiteToDelete?.let { campsite ->

        AlertDialog(
            onDismissRequest = {
                campsiteToDelete = null
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,

            title = {
                Text("Delete Campsite?")
            },

            text = {
                Text(
                    "Are you sure you want to delete " +
                            "${campsite.name}? " +
                            "This action cannot be undone."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        onDeleteCampsite(campsite)
                        campsiteToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFB3261E)
                    )
                ) {
                    Text("Delete")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        campsiteToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = darkGreen
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}