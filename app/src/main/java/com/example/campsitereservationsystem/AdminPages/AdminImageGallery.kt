package com.example.campsitereservationsystem.AdminPages

import android.util.Log
import com.example.campsitereservationsystem.ImageRepository
import com.example.campsitereservationsystem.UserRepository
import com.example.campsitereservationsystem.User
import com.example.campsitereservationsystem.OwnerCampsitesPath.Campsite
import com.example.campsitereservationsystem.OwnerCampsitesPath.CampsiteRepository
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import coil3.compose.AsyncImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

@Composable
fun AdminImageGallery(
    onBack: () -> Unit
) {
    val imageRepo = remember { ImageRepository() }
    val userRepo = remember { UserRepository() }
    val campsiteRepo = remember { CampsiteRepository() }

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var campsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }

    // Get all campsites and users
    LaunchedEffect(Unit) {
        userRepo.getAllUsers { loadedUsers ->
            users = loadedUsers
        }

        try {
            campsites = campsiteRepo.getAll()
        } catch (e: Exception) {
            Log.e("AdminImageGallery", "Failed to retrieve campsites", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Image Gallery", fontSize = 30.sp)

        Spacer(modifier = Modifier.height(25.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            // Campsite Images
            item {
                Text("Campsite Images", fontSize = 22.sp)
            }

            items(campsites, key = { "camp_${it.id}" }) { campsite ->

                var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
                var loading by remember { mutableStateOf(true) }

                // Retrieve campsite image
                LaunchedEffect(campsite.id) {
                    imageRepo.getCampsiteImage(campsite.id) { bytes ->
                        imageBytes = bytes
                        loading = false
                    }
                }

                if (loading) {
                    Spacer(modifier = Modifier.height(100.dp))
                }

                // Displays Row (name, campsite image, and delete option) if not null
                val bytes = imageBytes
                if(bytes != null){
                    AdminImageRow(
                        name = campsite.name,
                        imageBytes = bytes,
                        onRemove = {
                            imageRepo.deleteCampsiteImage(campsite.id)
                            imageBytes = null

                        }
                    )
                }
            }

            // Separate profiles images and campsite images
            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(15.dp))
                Text("Profile Images", fontSize = 22.sp)
            }

            items(users, key = { "user_${it.id}" }) { user ->

                var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
                var loading by remember { mutableStateOf(true) }

                // Retrieve profile image
                LaunchedEffect(user.id) {
                    imageRepo.getProfileImage(user.id) { bytes ->
                        imageBytes = bytes
                        loading = false
                    }
                }

                if (loading) {
                    Spacer(modifier = Modifier.height(100.dp))
                }

                // Displays Row (name, profile image, and delete option) if not null
                val bytes = imageBytes
                if (bytes != null) {
                    AdminImageRow(
                        name = user.name,
                        imageBytes = bytes,
                        onRemove = {
                            imageRepo.deleteProfileImage(user.id)
                            imageBytes = null

                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        AdminMenuButtons("Back", onBack)

    }


}

@Composable
private fun AdminImageRow(
    name: String,
    imageBytes: ByteArray,
    onRemove: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = imageBytes,
            contentDescription = name,
            modifier = Modifier.size(100.dp),
            contentScale = ContentScale.Crop
        )

        Text(
            text = name,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )
        Button(
            onClick = { showConfirm = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF819F5C)
            )
        ) {
            Text("Remove", color = Color.Black)
        }
    }

    //Confirmation pop-up
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Remove Image") },
            text = { Text("Are you sure you want to remove this image?") },
            confirmButton = {
                TextButton(onClick = {
                    onRemove()
                    showConfirm = false
                }) { Text("Remove", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

