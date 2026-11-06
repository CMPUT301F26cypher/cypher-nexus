package com.example.campsitereservationsystem

class CampsiteController (
    private val campsiteRepository: CampsiteRepository,
    private val reservationRepository: ReservationRepository
) {
    /**campsite filters**/
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
        return campsiteRepository.campsites.filter { campsite ->
            if (!campsite.location.contains(
                location.trim(),
                ignoreCase = true
                )
            ) {
                return@filter false
            }
            if (guests != null && campsite.maxGuests < guests) {
                return@filter false
            }
            /**guarantee dates are not null to the system**/
            if (checkIn == null) {
                return@filter true
            }
            return@filter (
                requireNotNull(checkIn) until requireNotNull(checkOut)
            ).all { night ->
                reservationRepository.scheduleFor(campsite.campsiteID)
                    ?.availableDays?.contains(night) == true &&
                reservationRepository.scheduleFor(campsite.campsiteID)
                    ?.bookedDays?.contains(night) == false
            }
        }
    }
}