package com.example.campsitereservationsystem.duplicates

data class Campsite(
    val campsiteID: String = "",
    val ownerID: String = "",
    val location: String = "",
    val description: String = "",
    val rules: String = "",
    val campsiteImages: List<String> = emptyList(),
    val maxGuests: Int = 0
)