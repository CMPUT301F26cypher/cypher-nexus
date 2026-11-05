package com.example.campsitereservationsystem.campsite

class FakeCampsiteRepository {

    private val campsites = listOf(
        Campsite(
            id = "CAMP001",
            name = "Banff",
            location = "Banff,Alberta",
            description = "A peaceful place",
            amenities = listOf(
                "Water", "Restrooms", "Fire pit", "Picnic table"
            ),
            maxOccupancy = 6
        ),
        Campsite(
            id = "CAMP002",
            name = "Mountain",
            location = "Calgary,Alberta",
            description = "Incredible place",
            amenities = listOf(
                "Water", "Restrooms","Picnic table"
            ),
            maxOccupancy = 4
        )
    )
    fun getOwnerCampsites(): List<Campsite> {
        return campsites
    }

    fun getCampsiteById(id: String): Campsite? {
        return campsites.find { it.id == id }
    }

}
