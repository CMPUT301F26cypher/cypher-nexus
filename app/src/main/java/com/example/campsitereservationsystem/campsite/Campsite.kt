package com.example.campsitereservationsystem.campsite

data class Campsite(
    val id: String,
    val name: String,
    val location: String,
    val description: String,
    val amenities: List<String>,
    val maxOccupancy: Int
)
