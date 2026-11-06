// Keeping the open and booked nights for the May 2027 sample calendar.

package com.example.campsitereservationsystem

/**
 * Sample overnight availability for May 2027.
 * Dates are kept in memory; other months and database storage are not connected.
 *
 * @property availableDays Nights offered by the owner, excluding booked nights.
 * @property bookedDays Occupied nights that availability edits must preserve.
 */
data class AvailabilitySchedule(
    val availableDays: Set<Int> = setOf(
        7, 8, 9, 10, 11, 12, 13,
        16, 17, 18, 19, 20,
        23, 24, 25, 26, 27, 30, 31
    ),
    val bookedDays: Set<Int> = setOf(14, 15, 21, 22, 28, 29)
) {
    init {
        // Checking that every open night belongs to the sample month and is free of existing bookings.
        for (day in availableDays) {
            require(day in 1..31) { "The date must be in May." }
            require(day !in bookedDays) { "A booked night cannot be available." }
        }

        for (day in bookedDays) {
            require(day in 1..31) { "The date must be in May." }
        }
    }

    /**
     * Updates the selected nights while keeping existing bookings.
     *
     * @param days Day numbers in May 2027 to update.
     * @param makeAvailable True to open the nights, false to close them.
     * @return A copy of the schedule with the requested changes.
     * @throws IllegalArgumentException If a day is outside the range 1 to 31.
     */
    fun withAvailability(days: List<Int>, makeAvailable: Boolean): AvailabilitySchedule {
        // Editing a separate set so the original schedule can still be used until the owner saves the draft.
        val changedDays = availableDays.toMutableSet()

        for (day in days) {
            require(day in 1..31) { "Pick a date from 1 to 31 May." }

            if (day in bookedDays) {
                continue
            }

            if (makeAvailable) {
                changedDays.add(day)
            } else {
                changedDays.remove(day)
            }
        }

        return copy(availableDays = changedDays.toSet())
    }
}
