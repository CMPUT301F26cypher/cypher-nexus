
/*
 * AddCampsiteScreen.kt
 *
 * This file is part of the View in our MVC architecture.
 * Here we allow the owner to register a new campsite or edit
 * one that already exists. The owner can also add or delete
 * an image and change the information of the campsite.
 *
 * Outstanding issues:
 * - We still need to add a field for rules and policies.
 * - For now, deleting an existing image removes it without
 *   waiting for the owner to save the changes. We need to
 *   make sure that cancelling does not delete the saved image.
 */

package com.example.campsitereservationsystem.OwnerCampsitesPath

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

/**
 * This screen allows the owner to create or edit a campsite.
 * We use the same screen for both cases so we don't have
 * to create two different screens with the same fields.
 *
 * @param campsite The campsite we want to edit. If it is null,
 * we are creating a new campsite.
 * @param onSave Callback used to send the selected image and
 * all the information entered by the owner when saving.
 * @param onCancel Callback used to go back without saving
 * the form changes.
 */
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

    // Check if we are editing or creating a campsite
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

    // If the campsite already exists, we get its saved image
    LaunchedEffect(campsite?.id) {
        if (campsite != null) {
            imageRepository.getCampsiteImage(campsite.id) { bytes ->
                savedImageBytes = bytes
            }
        }
    }

    // Here we get the existing information of the campsite.
    // If we are creating a new one, the fields start empty.
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

    // We use the same colors for all the text fields
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

        // The title depends on what the owner is doing
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

            // Here we display the selected image or the
            // image that was already saved for the campsite
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

            // The owner can select or delete an image
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

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

                            // If the image was just selected,
                            // we only remove it from the form.
                            if (selectedImageUri != null) {

                                selectedImageUri = null

                            } else if (campsite != null) {

                                // If the image was already saved,
                                // we delete it using the repository.
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

            // Here the owner can enter or change
            // the information of the campsite
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Campsite Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
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

            // The owner does not have to enter the coordinates
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

            // The owner can go back without saving the form
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
