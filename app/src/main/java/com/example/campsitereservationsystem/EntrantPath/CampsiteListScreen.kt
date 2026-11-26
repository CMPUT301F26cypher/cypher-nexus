
/*
 * CampsiteListScreen.kt
 *
 * This file is part of the View in our MVC architecture.
 * Here we allow the user to search for campsites depending
 * on the location, number of guests and available dates.
 *
 * We check that the information entered by the user is valid
 * before passing it to the CampsiteController, which handles
 * the actual filtering of the campsites.
 *
 * Outstanding issues:
 * - For now, the dates are limited to May 2027.
 *   We still need to support different months and years.
 * - This screen only displays the matching campsites.
 *   It does not allow the user to open their details directly.
 */

package com.example.campsitereservationsystem.EntrantPath

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campsitereservationsystem.OwnerCampsitesPath.CampsiteController

/**
 * This screen allows the user to find campsites depending
 * on where they want to stay, how many people are going
 * and which dates they are looking for.
 *
 * @param controller The controller we use to filter the campsites.
 * @param onBack Callback used to go back to the previous screen.
 */
@Composable
fun CampsiteListScreen(
    controller: CampsiteController,
    onBack: () -> Unit
) {

    // Here we store the information entered by the user.
    // The fields start empty because the filters are optional.
    var location by remember { mutableStateOf("") }
    var guestText by remember { mutableStateOf("") }
    var checkInText by remember { mutableStateOf("") }
    var checkOutText by remember { mutableStateOf("") }

    // We convert the number of guests and dates to integers.
    // If the text cannot be converted, we get null instead of an error.
    val guests = guestText.trim().toIntOrNull()
    val checkIn = checkInText.trim().toIntOrNull()
    val checkOut = checkOutText.trim().toIntOrNull()

    // We check if the user has entered at least one date
    val hasDates = checkInText.isNotBlank() || checkOutText.isNotBlank()

    // Before filtering, we make sure the information
    // entered by the user is valid.
    val error = when {

        // The number of guests must be greater than zero
        guestText.isNotBlank() && (guests == null || guests < 1) ->
            "Enter number of guests"

        // If the user wants to filter by dates,
        // both check-in and check-out dates are required.
        hasDates && (checkIn == null || checkOut == null) ->
            "Enter check in and check out dates"

        // Both dates must be between 1 and 31,
        // and the check-out must be after the check-in.
        checkIn != null && checkOut != null &&
                (checkIn !in 1..31 || checkOut !in 1..31 ||
                        checkOut <= checkIn) ->
            "Dates must be within 1-31 and check in must be before check out"

        // If everything is valid, there is no error
        else -> ""
    }

    // We only call the controller if there are no errors.
    // Otherwise, we return an empty list.
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

    // We use a LazyColumn so the user can scroll
    // through the filters and the matching campsites.
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Go back to the previous screen
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

        // For now, we are only working with dates from May 2027
        item {
            Text("Dates: May 2027")
        }

        // Here the user can enter the location they are looking for
        item {
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // The campsite needs to have enough space
        // for the number of guests entered by the user.
        item {
            OutlinedTextField(
                value = guestText,
                onValueChange = { guestText = it },
                label = { Text("Number of guests") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Here the user enters the day they want to arrive
        item {
            OutlinedTextField(
                value = checkInText,
                onValueChange = { checkInText = it },
                label = { Text("Check in date") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Here the user enters the day they want to leave
        item {
            OutlinedTextField(
                value = checkOutText,
                onValueChange = { checkOutText = it },
                label = { Text("Check out date") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // If something is wrong with the filters,
        // we display a message so the user can correct it.
        if (error.isNotEmpty()) {
            item {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        } else {

            // If no campsites match the filters,
            // we let the user know instead of showing an empty screen.
            if (result.isEmpty()) {
                item {
                    Text("No matching campsites")
                }
            }

            // Here we go through the matching campsites
            // and display the information of each one.
            // We use the campsite ID to identify each item.
            items(result, key = { it.id }) { campsite ->
                Column {
                    Text(campsite.location)
                    Text(campsite.description)
                    Text(text = "Maximum guests: ${campsite.maxOccupancy}")
                }
            }
        }
    }
}
