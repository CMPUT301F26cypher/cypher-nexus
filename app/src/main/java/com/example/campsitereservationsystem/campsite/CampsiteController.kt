
package com.example.campsitereservationsystem.campsite

import com.example.campsitereservationsystem.ReservationRepository

class CampsiteController(
    private val repository: CampsiteRepository,
    private val reservationRepository: ReservationRepository
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

    fun filterCampsites(
        location: String,
        guests: Int?,
        checkIn: Int?,
        checkOut: Int?
    ): List<Campsite> {
        require(guests == null || guests > 0)
        require(
            (checkIn == null && checkOut == null) || (checkIn != null && checkOut != null &&
                    checkIn in 1..31 && checkOut in 1..31 && checkOut > checkIn)
        )
        return repository.campsites.filter { campsite ->
            if (!campsite.location.contains(
                    location.trim(),
                    ignoreCase = true
                )
            ) {
                return@filter false
            }
            if (guests != null && campsite.maxOccupancy < guests) {
                return@filter false
            }
            /**guarantee dates are not null to the system**/
            if (checkIn == null) {
                return@filter true
            }
            return@filter (
                    requireNotNull(checkIn) until requireNotNull(checkOut)
                    ).all { night ->
                    reservationRepository.scheduleFor(campsite.id)
                        ?.availableDays?.contains(night) == true &&
                    reservationRepository.scheduleFor(campsite.id)
                        ?.bookedDays?.contains(night) == false
                }
            }
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
