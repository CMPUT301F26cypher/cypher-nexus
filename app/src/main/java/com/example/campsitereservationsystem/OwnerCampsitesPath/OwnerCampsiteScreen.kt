
/*
 * OwnerCampsitesScreen.kt
 *
 * This file is part of the View in our MVC architecture.
 * Here we display all the campsites that belong to the owner.
 * The owner can register a new campsite, edit or delete an
 * existing one, and open its details.
 *
 * We also ask the owner for confirmation before deleting
 * a campsite so they do not delete one by accident.
 *
 * Outstanding issues:
 * - For now, the Booking button is ready, but we still
 *   need to connect it to the booking functionality.
 *   The button is disabled until we pass the callback.
 */

package com.example.campsitereservationsystem.OwnerCampsitesPath

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

/**
 * This screen allows the owner to see and manage
 * all their registered campsites in one place.
 *
 * @param campsites The list of campsites owned by the current owner.
 * @param onCampsiteClick Callback used to open the campsite details.
 * @param onAddCampsite Callback used to register a new campsite.
 * @param onEditCampsite Callback used to edit the selected campsite.
 * @param onDeleteCampsite Callback used to delete the selected campsite.
 * @param onBack Callback used to go back to the previous screen.
 * @param onBookingCampsite Callback used to open the booking
 * functionality. If it is null, the Booking button is disabled.
 */
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

    // Here we keep track of which campsite the owner wants to delete.
    // If it is null, we do not display the confirmation dialog.
    var campsiteToDelete by remember {
        mutableStateOf<Campsite?>(null)
    }

    // If the confirmation dialog is open, we close it first.
    // Otherwise, we go back to the previous screen.
    BackHandler {
        if (campsiteToDelete != null) {
            campsiteToDelete = null
        } else {
            onBack()
        }
    }

    // We use the same colors for all the main buttons
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

        // This button takes the owner to the registration screen
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

        // We use a LazyColumn so the owner can scroll
        // through all their registered campsites.
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // If the owner has no campsites, we display a message
            if (campsites.isEmpty()) {
                item {
                    Text(
                        text = "You have no registered campsites."
                    )
                }
            }

            // Here we create a card for each campsite.
            // We use the campsite ID to identify each item in the list.
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

                        // The owner can click on the campsite
                        // information to open its details.
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

                        // Here we display the different actions
                        // the owner can perform on each campsite.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            // We pass the selected campsite
                            // to the callback so the owner can edit it.
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

                            // We do not delete the campsite immediately.
                            // First, we ask the owner for confirmation.
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

                            // For now, this button only works if
                            // the booking callback is provided.
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

        // We keep the Back button outside the LazyColumn
        // so the owner does not have to scroll to go back.
        Button(
            onClick = onBack,
            colors = buttonColors,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Back")
        }
    }

    // We only display this dialog when the owner
    // has selected a campsite to delete.
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

            // We include the name so the owner knows
            // exactly which campsite they are about to delete.
            text = {
                Text(
                    "Are you sure you want to delete " +
                            "${campsite.name}? " +
                            "This action cannot be undone."
                )
            },

            // If the owner confirms, we call onDeleteCampsite
            // and close the confirmation dialog.
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

            // If the owner cancels, we close the dialog
            // without requesting the deletion of the campsite.
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
