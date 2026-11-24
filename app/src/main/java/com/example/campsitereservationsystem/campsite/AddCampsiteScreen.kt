
package com.example.campsitereservationsystem.campsite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.campsitereservationsystem.ImagePickerButton
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.campsitereservationsystem.ImageRepository

@Composable
fun AddCampsiteScreen(
    campsite: Campsite? = null,
    onSave: (
        Uri?, String, String, String, String,
        String, String, String, String
    ) -> Unit,
    onCancel: () -> Unit
) {
    val isEditing = campsite != null

    val imageRepository = remember { ImageRepository() }
    var savedImageBytes by remember(campsite){
        mutableStateOf<ByteArray?>(null)
    }
    var selectedImageUri by remember(campsite){
        mutableStateOf<Uri?>(null)
    }

    LaunchedEffect(campsite?.id) {
        if(campsite != null){
            imageRepository.getCampsiteImage(campsite.id){ bytes ->
                savedImageBytes = bytes
            }
        }
    }

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
        mutableStateOf(campsite?.amenities?.joinToString(", ") ?: "")
    }

    var maxOccupancy by remember(campsite) {
        mutableStateOf(campsite?.maxOccupancy?.toString() ?: "")
    }

    var latitude by remember(campsite) {
        mutableStateOf(campsite?.latitude?.toString() ?: "")
    }

    var longitude by remember(campsite) {
        mutableStateOf(campsite?.longitude?.toString() ?: "")
    }

    var tags by remember(campsite) {
        mutableStateOf(campsite?.tags?.joinToString(", ") ?: "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isEditing) "Edit Campsite" else "Register Campsite",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 40.sp
        )

        if (selectedImageUri != null || savedImageBytes!= null){
            AsyncImage(
                model = selectedImageUri ?: savedImageBytes,
                contentDescription = "Selected campsite image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ImagePickerButton { uri ->
                selectedImageUri = uri
            }

            if (selectedImageUri != null || savedImageBytes != null) {
                Button(
                    onClick = {
                        if (selectedImageUri != null) {
                            selectedImageUri = null
                        } else if (campsite != null) {
                            imageRepository.deleteCampsiteImage(campsite.id)
                            savedImageBytes = null
                        }
                    }
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
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amenities,
            onValueChange = { amenities = it },
            label = { Text("Amenities (comma separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = maxOccupancy,
            onValueChange = { maxOccupancy = it },
            label = { Text("Maximum Occupancy") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Tags (comma separated)") },
            modifier = Modifier.fillMaxWidth()
        )

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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isEditing) "Save Changes" else "Save Campsite"
            )
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

/*

fun AddCampsiteTextFields(){
    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Campsite Name") },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF819F5C)
        )

    )
}
*/