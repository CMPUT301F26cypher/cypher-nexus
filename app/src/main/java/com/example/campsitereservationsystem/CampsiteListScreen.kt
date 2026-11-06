package com.example.campsitereservationsystem

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun CampsiteListScreen(
    controller: CampsiteController,
    onBack: () -> Unit
) {
    var location by remember { mutableStateOf("") }
    var guestText by remember { mutableStateOf("") }
    var checkInText by remember { mutableStateOf("") }
    var checkOutText by remember { mutableStateOf("") }
    val guests = guestText.trim().toIntOrNull()
    val checkIn = checkInText.trim().toIntOrNull()
    val checkOut = checkOutText.trim().toIntOrNull()
    val hasDates = checkInText.isNotBlank() || checkOutText.isNotBlank()
    val error = when {
        guestText.isNotBlank() && (guests == null || guests < 1) ->
            "Enter number of guests"
        hasDates && (checkIn == null || checkOut == null) ->
            "Enter check in and check out dates"
        checkIn != null && checkOut != null && (checkIn !in 1..31 || checkOut !in 1..31 ||
                checkOut <= checkIn) ->
                    "Dates must be within 1-31 and check in must be before check out"
        else -> ""
    }
    val result = if (error.isEmpty()) {
        controller.filterCampsites(
            location = location,
            guests = guests,
            checkIn = checkIn,
            checkOut = checkOut
        )
    } else {
        emptyList()
    }
    BackHandler(onBack = onBack)
    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
        item {
            Text(
                "Campsites",
            )
        }
        item {
            Text("Dates: May 2027")
        }
        item {
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = guestText,
                onValueChange = { guestText = it },
                label = { Text("Number of guests") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = checkInText,
                onValueChange = { checkInText = it },
                label = { Text("Check in date") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = checkOutText,
                onValueChange = { checkOutText = it },
                label = { Text("Check out date") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (error.isNotEmpty()) {
            item {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        } else {
            if (result.isEmpty()) {
                item {
                    Text("No matching campsites")
                }
            }
            items(result, key = { it.campsiteID }) { campsite ->
                Column {
                    Text(campsite.location)
                    Text(campsite.description)
                    Text(text = "Maximum guests: ${campsite.maxGuests}")
                }
            }
        }
    }
}