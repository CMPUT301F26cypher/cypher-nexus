
package com.example.campsitereservationsystem.OwnerCampsitesPath

import com.google.firebase.firestore.Blob

/***
 * Represents a campsite registered by an owner.
 * This data class is part of the Model layer in MVC.
 * It stores campsite information used by the owner and entrant screens
 * and by the Firebase repository.
 */
data class Campsite(
    //Normal details for each campsite
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val amenities: List<String> = emptyList(),
    val maxOccupancy: Int = 0,

    // Owner who registered this campsite
    val ownerId: String = "",

    // Geographic coordinates for the map
    val latitude: Double? = null,
    val longitude: Double? = null,

    // Optional campsite image
    val imageData: Blob? = null,

    // Campsite categories
    val tags: List<String> = emptyList(),
    val rules: String = "",
)
