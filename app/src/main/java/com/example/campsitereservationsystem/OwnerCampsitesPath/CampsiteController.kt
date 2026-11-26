
/*
 * CampsiteController.kt
 *
 * This file is part of the Controller in our MVC architecture.
 * Here we handle the logic for registering, updating, deleting
 * and filtering campsites.
 *
 * We also check that the information entered by the owner
 * is valid before passing it to the repository.
 *
 * Outstanding issues:
 * - We still need to test the date filter with the actual
 *   campsite availability and reservation data.
 * - For now, the date filter only works with days from 1 to 31
 *   and does not support different months or years.
 */

package com.example.campsitereservationsystem.OwnerCampsitesPath

import com.example.campsitereservationsystem.ReservationRepository

/**
 * This class handles the logic between the campsite screens
 * and the repositories.
 *
 * @property repository The repository we use to access,
 * save, update and delete campsites.
 * @property reservationRepository The repository we use
 * to check the available and booked days of a campsite.
 */
class CampsiteController(
    private val repository: CampsiteRepository,
    private val reservationRepository: ReservationRepository
) {

    /**
     * Registers a new campsite after checking that
     * the information entered by the owner is valid.
     *
     * @param campsite The campsite we want to register.
     * @return The ID of the new campsite.
     * @throws IllegalArgumentException If the campsite
     * information is not valid.
     */
    suspend fun registerCampsite(campsite: Campsite): String {
        validateCampsite(campsite)
        return repository.save(campsite)
    }

    /**
     * Updates the information of a campsite that already exists.
     * Before updating it, we check that its information
     * is valid and that it has an ID.
     *
     * @param campsite The campsite with the updated information.
     * @throws IllegalArgumentException If the information
     * is not valid or the campsite ID is empty.
     */
    suspend fun updateCampsite(campsite: Campsite) {
        validateCampsite(campsite)

        require(campsite.id.isNotBlank()) {
            "Campsite ID cannot be empty"
        }

        repository.update(campsite)
    }

    /**
     * Deletes a campsite using its ID.
     *
     * @param campsiteId The ID of the campsite we want to delete.
     * @throws IllegalArgumentException If the campsite ID is empty.
     */
    suspend fun deleteCampsite(campsiteId: String) {
        require(campsiteId.isNotBlank()) {
            "Campsite ID cannot be empty"
        }

        repository.delete(campsiteId)
    }

    /**
     * This function filters the campsites depending on
     * what the user is looking for.
     *
     * We check the location, the number of guests that need
     * to fit in the campsite and the days the user wants to stay.
     * If the user does not enter a specific filter, we do not
     * use that filter when looking for campsites.
     *
     * @param location The location the user is looking for.
     * An empty string means any location.
     * @param guests The number of guests that need to fit
     * in the campsite. Null means any capacity.
     * @param checkIn The day the user wants to arrive.
     * @param checkOut The day the user wants to leave.
     * Both dates must be null if the user is not filtering by dates.
     * @return A list of campsites that match the selected filters.
     * @throws IllegalArgumentException If the number of guests
     * is not positive or the selected dates are not valid.
     */
    fun filterCampsites(
        location: String,
        guests: Int?,
        checkIn: Int?,
        checkOut: Int?
    ): List<Campsite> {

        // First we check that the filters entered by the user are valid
        require(guests == null || guests > 0)

        require(
            (checkIn == null && checkOut == null) ||
                    (checkIn != null && checkOut != null &&
                            checkIn in 1..31 &&
                            checkOut in 1..31 &&
                            checkOut > checkIn)
        )

        return repository.campsites.filter { campsite ->

            // We remove the campsites that do not match
            // the location the user is looking for
            if (!campsite.location.contains(
                    location.trim(),
                    ignoreCase = true
                )
            ) {
                return@filter false
            }

            // The campsite needs to have enough space
            // for the number of guests entered by the user
            if (guests != null && campsite.maxOccupancy < guests) {
                return@filter false
            }

            // If the user did not enter any dates,
            // we do not need to check the availability
            if (checkIn == null) {
                return@filter true
            }

            // Here we check that every night of the stay
            // is available and has not already been booked.
            // We do not include the check-out day.
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

    /**
     * Checks that the campsite has all the required information
     * before we register or update it.
     *
     * We use this function in both cases so we do not have
     * to write the same validation logic twice.
     *
     * @param campsite The campsite we want to validate.
     * @throws IllegalArgumentException If any of the required
     * information is missing or not valid.
     */
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
