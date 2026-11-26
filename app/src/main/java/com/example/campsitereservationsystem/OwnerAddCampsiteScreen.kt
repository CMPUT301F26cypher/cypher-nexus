package com.example.campsitereservationsystem

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.campsitereservationsystem.OwnerCampsitesPath.Campsite
import com.example.campsitereservationsystem.OwnerCampsitesPath.CampsiteController
import com.example.campsitereservationsystem.campsite.AddCampsiteScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch


/*

This function was based off of Juan's CampsiteFormPage
Modified AddCampsiteScreen.kt accordingly to fit the requried changes based on the ui
 */

@Composable
fun OwnerAddCampsiteScreen(
    campsiteController: CampsiteController,
    ownerId: String,
    ownerSites: MutableList<Campsite>,
    originalSite: Campsite? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageRepository = remember { ImageRepository()}
    // Tracking a save while the existing form stays visible.
    var savingSite by remember { mutableStateOf(false) }

    AddCampsiteScreen(
        campsite = originalSite,
        // Returning from the form without submitting its values when the owner cancels.
        onCancel = onBack,
        onSave = save@{ imageUri, name, location, description, amenities, occupancy, latitude, longitude, tags ->
            if (savingSite) return@save
            // Checking the owner of an existing record before editing.
            if (originalSite != null && originalSite.ownerId != ownerId) {
                context.showCampsiteMessage("You cannot edit this campsite")
                return@save
            }

            // Keeping the original ID, owner, and image when editing existing details.
            // Turning form input into the campsite model while retaining fields already on the selected campsite.
            val blankOrExisting = originalSite ?: Campsite(ownerId = ownerId)
            val updatedSite = blankOrExisting.copy(
                name = name.trim(),
                location = location.trim(),
                description = description.trim(),
                // Converting comma-separated form entries into lists.
                amenities = amenities.commaSeparatedItems(),
                maxOccupancy = occupancy.toIntOrNull() ?: 0,
                latitude = latitude.toDoubleOrNull(),
                longitude = longitude.toDoubleOrNull(),
                tags = tags.commaSeparatedItems()
            )

            // Marking the form busy before launching the request.
            savingSite = true
            scope.launch {
                try {
                    // Registering a new record when the form opened empty.
                    if (originalSite == null) {
                        val newId = campsiteController.registerCampsite(updatedSite)
                        //IMAGE
                        if (imageUri != null) {
                            val imageBytes = imageRepository.convertUriToBytes(context, imageUri)

                            if (imageBytes != null) {
                                imageRepository.saveCampsiteImage(newId, imageBytes)
                            }
                        }
                        ownerSites.add(updatedSite.copy(id = newId))
                        context.showCampsiteMessage("Campsite saved successfully!")
                    } else {
                        // Saving an edit under the existing campsite ID.
                        campsiteController.updateCampsite(updatedSite)
                        //IMAGE
                        if (imageUri != null) {
                            val imageBytes = imageRepository.convertUriToBytes(context, imageUri)

                            if (imageBytes != null) {
                                imageRepository.saveCampsiteImage(updatedSite.id, imageBytes)
                            }
                        }
                        val row = ownerSites.indexOfFirst { it.id == updatedSite.id }
                        if (row >= 0) ownerSites[row] = updatedSite
                        context.showCampsiteMessage("Campsite updated successfully!")
                    }
                    // Closing the form after a successful save.
                    onBack()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    // Keeping the form open after an error and reporting whether registration or editing was attempted.
                    val fallback = if (originalSite == null) "Failed to save campsite" else "Failed to update campsite"
                    context.showCampsiteMessage(error.message ?: fallback, long = true)
                } finally {
                    savingSite = false
                }
            }
        }
    )
}

//copied from Campsite Routes
private fun Context.showCampsiteMessage(message: String, long: Boolean = false) {
    val duration = if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, message, duration).show()
}

//copied form CampisteRoutes
private fun String.commaSeparatedItems(): List<String> {
    // Trimming list entries and dropping blanks caused by extra commas.
    return split(",").map { it.trim() }.filter { it.isNotEmpty() }
}