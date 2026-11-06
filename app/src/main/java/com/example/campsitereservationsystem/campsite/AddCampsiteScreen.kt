
package com.example.campsitereservationsystem.campsite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddCampsiteScreen(
    campsite: Campsite? = null,
    onSave: (
        String, String, String, String,
        String, String, String, String
    ) -> Unit,
    onCancel: () -> Unit
) {
    val isEditing = campsite != null

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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isEditing) "Edit Campsite" else "Register Campsite",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Campsite Name") },
            modifier = Modifier.fillMaxWidth()
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
