
package com.example.campsitereservationsystem.campsite

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
    val imageUrl: String? = null,

    // Campsite categories
    val tags: List<String> = emptyList(),
    val rules: String = "",
    val campsiteImages: List<String> = emptyList()
)
