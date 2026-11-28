package com.example.campsitereservationsystem.campsite

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.campsitereservationsystem.ImagePickerButton
import com.example.campsitereservationsystem.ImageRepository
import com.example.campsitereservationsystem.OwnerCampsitesPath.Campsite

// US 02.01.01 - Register a campsite.
// US 02.01.02 - Provide campsite details.
// US 02.01.03 - Update campsite details.
@Composable
fun AddCampsiteScreen(
    campsite: Campsite? = null,
    onSave: (
        Uri?, String, String, String, String,
        String, String, String, String
    ) -> Unit,
    onCancel: () -> Unit
) {

    BackHandler(onBack = onCancel)

    val green = Color(0xFF819F5C)
    val darkGreen = Color(0xFF587437)

    val isEditing = campsite != null

    val imageRepository = remember {
        ImageRepository()
    }

    var savedImageBytes by remember(campsite) {
        mutableStateOf<ByteArray?>(null)
    }

    var selectedImageUri by remember(campsite) {
        mutableStateOf<Uri?>(null)
    }

    // Retrieve the existing campsite image
    LaunchedEffect(campsite?.id) {
        if (campsite != null) {
            imageRepository.getCampsiteImage(campsite.id) { bytes ->
                savedImageBytes = bytes
            }
        }
    }

    // Existing campsite information
    var name by remember(campsite) {
        mutableStateOf(campsite?.name ?: "")
    }

    var location by remember(campsite) {
        mutableStateOf(campsite?.location ?: "")
    }

    var description by remember(campsite) {
        mutableStateOf(campsite?.description ?: "")
    }

    var amenities by remember(campsite) {
        mutableStateOf(
            campsite?.amenities?.joinToString(", ") ?: ""
        )
    }

    var maxOccupancy by remember(campsite) {
        mutableStateOf(
            campsite?.maxOccupancy?.toString() ?: ""
        )
    }

    var latitude by remember(campsite) {
        mutableStateOf(
            campsite?.latitude?.toString() ?: ""
        )
    }

    var longitude by remember(campsite) {
        mutableStateOf(
            campsite?.longitude?.toString() ?: ""
        )
    }

    var tags by remember(campsite) {
        mutableStateOf(
            campsite?.tags?.joinToString(", ") ?: ""
        )
    }

    // Shared colors for all text fields
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = green,
        unfocusedBorderColor = green,
        focusedLabelColor = darkGreen,
        cursorColor = darkGreen
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .padding(16.dp)
    ) {

        Text(
            text = if (isEditing) {
                "Edit Campsite"
            } else {
                "Register Campsite"
            },
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Campsite image preview
            if (
                selectedImageUri != null ||
                savedImageBytes != null
            ) {

                AsyncImage(
                    model = selectedImageUri ?: savedImageBytes,
                    contentDescription = "Selected campsite image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Image selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Apply green styling to the existing image picker
                // We use the existing image picker and
                // give it the same colors as our screen
                MaterialTheme(
                    colorScheme = MaterialTheme.colorScheme.copy(
                        primary = green,
                        onPrimary = Color.Black
                    )
                ) {

                    ImagePickerButton { uri ->
                        selectedImageUri = uri
                    }
                }

                if (
                    selectedImageUri != null ||
                    savedImageBytes != null
                ) {

                    Button(
                        onClick = {

                            if (selectedImageUri != null) {

                                selectedImageUri = null

                            } else if (campsite != null) {

                                imageRepository.deleteCampsiteImage(
                                    campsite.id
                                )

                                savedImageBytes = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = green,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Delete Image")
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Campsite Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF819F5C),
                    unfocusedBorderColor = Color(0xFF819F5C),
                    focusedLabelColor = Color(0xFF819F5C)
                )
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            OutlinedTextField(
                value = amenities,
                onValueChange = { amenities = it },
                label = {
                    Text("Amenities (comma separated)")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            OutlinedTextField(
                value = maxOccupancy,
                onValueChange = { maxOccupancy = it },
                label = { Text("Maximum Occupancy") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = {
                    Text("Tags (comma separated)")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            OutlinedTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitude (optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitude (optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save campsite
            // We pass all the information through onSave
            // so the saving logic can be handled outside this screen
            Button(
                onClick = {

                    onSave(
                        selectedImageUri,
                        name,
                        location,
                        description,
                        amenities,
                        maxOccupancy,
                        latitude,
                        longitude,
                        tags
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = green,
                    contentColor = Color.Black
                )
            ) {

                Text(
                    if (isEditing) {
                        "Save Changes"
                    } else {
                        "Save Campsite"
                    }
                )
            }

            // Cancel campsite registration or editing
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, green),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}