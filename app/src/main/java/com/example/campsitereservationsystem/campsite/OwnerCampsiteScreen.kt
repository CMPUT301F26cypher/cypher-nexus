
package com.example.campsitereservationsystem.campsite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OwnerCampsitesScreen(
    campsites: List<Campsite>,
    onCampsiteClick: (Campsite) -> Unit,
    onAddCampsite: () -> Unit,
    onEditCampsite: (Campsite) -> Unit,
    onDeleteCampsite: (Campsite) -> Unit
) {
    var campsiteToDelete by remember {
        mutableStateOf<Campsite?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Campsites",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddCampsite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Add Campsite")
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(campsites, key = { it.id }) { campsite ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
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

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = campsite.location)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onEditCampsite(campsite)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Edit")
                            }

                            OutlinedButton(
                                onClick = {
                                    campsiteToDelete = campsite
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog before deleting
    campsiteToDelete?.let { campsite ->
        AlertDialog(
            onDismissRequest = {
                campsiteToDelete = null
            },
            title = {
                Text("Delete Campsite?")
            },
            text = {
                Text(
                    "Are you sure you want to delete " +
                            "${campsite.name}? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCampsite(campsite)
                        campsiteToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        campsiteToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
