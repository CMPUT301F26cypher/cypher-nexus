// Saving and loading the Lottery Details form (registration and booking periods) in Firestore (US 02.04.03, 02.04.04, 02.04.06).

package com.example.campsitereservationsystem

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Saves the Lottery Details form (Frame 24) into Firestore, and loads it back for Update (Frames 31-32).
 *
 * It writes to the same document as [LotteryRepository]: lotteries/{campsiteId}.
 * - Registration period: the lottery's existing "startTimestamp" and "endTimestamp" fields.
 * - Booking period: new "bookingStartTimestamp" and "bookingEndTimestamp" fields.
 *
 * Outstanding issue: the lottery "status", winners and waiting list are not changed here (Pooling System).
 */
class LotteryDetailsRepository {
    private val lotteriesRef = Firebase.firestore.collection("lotteries")

    /**
     * Saves both periods of a campsite's lottery.
     * Only these fields are written. Everything else in the lottery is kept.
     *
     * @param campsiteId The campsite the lottery belongs to.
     * @param details The form. The screen checks it before calling this.
     * @param onResult Called with true when saved, or false if it could not be saved.
     */
    fun saveLotteryDetails(
        campsiteId: String,
        details: LotteryDetails,
        onResult: (Boolean) -> Unit
    ) {
        val registrationStart = details.registration.start()
        val registrationEnd = details.registration.end()
        val bookingStart = details.booking.start()
        val bookingEnd = details.booking.end()

        // Safety net: the screen already checked the form, so this should never happen.
        if (registrationStart == null || registrationEnd == null || bookingStart == null || bookingEnd == null) {
            onResult(false)
            return
        }

        val data = mapOf(
            "campsiteID" to campsiteId,
            "startTimestamp" to toTimestamp(registrationStart),
            "endTimestamp" to toTimestamp(registrationEnd),
            "bookingStartTimestamp" to toTimestamp(bookingStart),
            "bookingEndTimestamp" to toTimestamp(bookingEnd)
        )

        // merge() changes only these fields and keeps everything else in the lottery.
        lotteriesRef.document(campsiteId).set(data, SetOptions.merge())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    /**
     * Loads the saved periods of a campsite's lottery so the form can show them (US 02.04.06).
     *
     * @param campsiteId The campsite the lottery belongs to.
     * @param onLoaded Called with the saved details, or with null if this campsite has no
     * lottery yet (then the form starts empty, as when creating).
     * @param onError Called if Firestore could not be reached.
     */
    fun loadLotteryDetails(
        campsiteId: String,
        onLoaded: (LotteryDetails?) -> Unit,
        onError: () -> Unit
    ) {
        lotteriesRef.document(campsiteId).get()
            .addOnSuccessListener { document ->
                val registrationStart = toDateTime(document.getTimestamp("startTimestamp"))
                val registrationEnd = toDateTime(document.getTimestamp("endTimestamp"))
                val bookingStart = toDateTime(document.getTimestamp("bookingStartTimestamp"))
                val bookingEnd = toDateTime(document.getTimestamp("bookingEndTimestamp"))

                // No saved registration period means there is no lottery to update yet.
                if (registrationStart == null || registrationEnd == null) {
                    onLoaded(null)
                } else {
                    val details = LotteryDetails(
                        registration = LotteryPeriod.from(registrationStart, registrationEnd),
                        booking = LotteryPeriod.from(bookingStart, bookingEnd)
                    )
                    onLoaded(details)
                }
            }
            .addOnFailureListener { onError() }
    }

    // Turns a date and time on the phone into the Timestamp type Firestore stores.
    private fun toTimestamp(dateTime: LocalDateTime): Timestamp {
        val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        return Timestamp(instant.epochSecond, instant.nano)
    }

    // Turns a Firestore Timestamp back into a date and time on the phone. Missing fields give null.
    private fun toDateTime(timestamp: Timestamp?): LocalDateTime? {
        if (timestamp == null) {
            return null
        }
        return LocalDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneId.systemDefault())
    }
}
