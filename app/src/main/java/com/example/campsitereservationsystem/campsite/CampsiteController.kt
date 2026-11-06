
package com.example.campsitereservationsystem.campsite

class CampsiteController(
    private val repository: CampsiteRepository = CampsiteRepository()
) {

    // Register a new campsite
    suspend fun registerCampsite(campsite: Campsite): String {
        validateCampsite(campsite)
        return repository.save(campsite)
    }

    // Update an existing campsite
    suspend fun updateCampsite(campsite: Campsite) {
        validateCampsite(campsite)

        require(campsite.id.isNotBlank()) {
            "Campsite ID cannot be empty"
        }

        repository.update(campsite)
    }

    // Delete an existing campsite
    suspend fun deleteCampsite(campsiteId: String) {
        require(campsiteId.isNotBlank()) {
            "Campsite ID cannot be empty"
        }

        repository.delete(campsiteId)
    }

    // Validate campsite information
    private fun validateCampsite(campsite: Campsite) {
        require(campsite.name.isNotBlank()) {
            "Campsite name cannot be empty"
        }

        require(campsite.location.isNotBlank()) {
            "Campsite location cannot be empty"
        }

        require(campsite.maxOccupancy > 0) {
            "Maximum occupancy must be greater than zero"
        }

        require(campsite.ownerId.isNotBlank()) {
            "Campsite owner is required"
        }
    }
}
