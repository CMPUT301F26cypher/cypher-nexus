// Holding one start/end period typed on the Lottery Details form (used for registration and booking, US 02.04.03, 02.04.04, 02.04.06).

package com.example.campsitereservationsystem

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import com.google.firebase.Timestamp

// "M/d/uuuu" accepts 01/20/2027, 1/20/2027 and 02/3/2027 (the formats used in the storyboard).
// STRICT stops impossible dates such as 02/30/2027 from being quietly changed to 02/28/2027.
private val DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/uuuu").withResolverStyle(ResolverStyle.STRICT)

// "H:mm" accepts 24 hour times such as 9:05, 12:00 and 23:59.
private val TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm").withResolverStyle(ResolverStyle.STRICT)

// Used to show saved values in the form, for example "01/20/2027" and "09:05" (US 02.04.06).
private val SHOW_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy")
private val SHOW_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

/**
 * One period on the Lottery Details form (Frame 24), exactly as the owner typed it.
 * The same class is used for the registration period (US 02.04.03)
 * and the booking period (US 02.04.04).
 *
 * @property startDate Start date as MM/DD/YYYY, for example "01/20/2027".
 * @property startTime Start time on a 24 hour clock, for example "12:00".
 * @property endDate End date as MM/DD/YYYY, for example "01/31/2027".
 * @property endTime End time on a 24 hour clock, for example "23:59".
 */
data class LotteryPeriod(
    val startDate: String = "",
    val startTime: String = "",
    val endDate: String = "",
    val endTime: String = ""
) {

    /** @return When the period starts, or null if the start date or time is not valid. */
    fun start(): LocalDateTime? {
        return toDateTime(startDate, startTime)
    }

    /** @return When the period ends, or null if the end date or time is not valid. */
    fun end(): LocalDateTime? {
        return toDateTime(endDate, endTime)
    }

    /** Returns the Firebase Timestamp for the start of this period. */
    fun startTimestamp(): Timestamp? {
        return toTimestamp(startDate, startTime)
    }

    /** Returns the Firebase Timestamp for the end of this period. */
    fun endTimestamp(): Timestamp? {
        return toTimestamp(endDate, endTime)
    }

    /**
     * Checks this period on its own: every field filled in, real dates and times,
     * and the end after the start.
     *
     * @param name "Registration" or "Booking". Used to build the error message.
     * @return An empty string if the period is fine, or a message explaining what to fix.
     */
    fun problem(name: String): String {
        if (startDate.isBlank() || startTime.isBlank() || endDate.isBlank() || endTime.isBlank()) {
            return "Please fill in every ${name.lowercase()} date and time."
        }

        val start = start()
        val end = end()

        if (start == null) {
            return "$name start must be a date like 01/20/2027 and a time like 12:00."
        }
        if (end == null) {
            return "$name end must be a date like 01/31/2027 and a time like 23:59."
        }
        if (!end.isAfter(start)) {
            return "$name end must be after ${name.lowercase()} start."
        }
        return ""
    }

    companion object {
        /**
         * Builds a period from saved dates and times, ready to show in the form (US 02.04.06).
         * Example: 20 Jan 2027 12:00 becomes startDate "01/20/2027" and startTime "12:00".
         *
         * @param start When the period starts, or null if nothing is saved.
         * @param end When the period ends, or null if nothing is saved.
         * @return The filled-in period, or an empty period if either value is missing.
         */
        fun from(start: LocalDateTime?, end: LocalDateTime?): LotteryPeriod {
            if (start == null || end == null) {
                return LotteryPeriod()
            }
            return LotteryPeriod(
                startDate = start.format(SHOW_DATE_FORMAT),
                startTime = start.format(SHOW_TIME_FORMAT),
                endDate = end.format(SHOW_DATE_FORMAT),
                endTime = end.format(SHOW_TIME_FORMAT)
            )
        }
    }
}

// Turns typed text such as "01/20/2027" and "12:00" into one date and time.
// Returns null if either part is not a real date or time.
private fun toDateTime(dateText: String, timeText: String): LocalDateTime? {
    return try {
        val date = LocalDate.parse(dateText.trim(), DATE_FORMAT)
        val time = LocalTime.parse(timeText.trim(), TIME_FORMAT)
        LocalDateTime.of(date, time)
    } catch (error: DateTimeParseException) {
        null
    }
}

// Turns typed text such as "01/20/2027" and "12:00"
// directly into a Firebase Timestamp.
// Returns null if either part is not a valid date or time.
private fun toTimestamp(dateText: String, timeText: String): Timestamp? {
    return try {
        val dateTime = toDateTime(dateText, timeText)

        if (dateTime != null) {
            Timestamp(
                java.util.Date.from(
                    dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()
                )
            )
        } else {
            null
        }
    } catch (error: Exception) {
        null
    }
}