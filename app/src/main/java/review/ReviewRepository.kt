package com.example.campsitereservationsystem.review

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReviewRepository(
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val col = db.collection("reviews")

    // Save a review to Firebase
    suspend fun addReview(review: Review) {
        require(review.campsiteId.isNotBlank()) {
            "Campsite ID cannot be empty"
        }
        require(review.entrantId.isNotBlank()) {
            "Entrant ID cannot be empty"
        }
        require(review.rating in 1..5) {
            "Rating must be between 1 and 5"
        }
        require(review.comment.isNotBlank()) {
            "Review comment cannot be empty"
        }

        val doc = col.document()

        val newReview = review.copy(
            id = doc.id,
            comment = review.comment.trim(),
            createdAt = System.currentTimeMillis()
        )

        doc.set(newReview).await()
    }

    // Get all reviews for a campsite
    //suspend fun means that
    suspend fun getReviewsByCampsite(
        campsiteId: String
    ): List<Review> {

        val snapshot = col
            .whereEqualTo("campsiteId", campsiteId)
            .get()
            .await()
        //mapNotNull transforms documents form Firebase into objects of review if not it discard them.
        return snapshot.documents.mapNotNull { doc ->
            //Review::class.java is in charge saying "Firebase, transform this document to a Review object."
            doc.toObject(Review::class.java)
                ?.copy(id = doc.id)
        }
    }

    // Find an entrant's existing review for a campsite
    suspend fun getReviewByEntrant(
        campsiteId: String,
        entrantId: String
    ): Review? {

        val snapshot = col
            .whereEqualTo("campsiteId", campsiteId)
            .whereEqualTo("entrantId", entrantId)
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()
            ?.let { doc ->
                doc.toObject(Review::class.java)
                    ?.copy(id = doc.id)
            }
    }
}