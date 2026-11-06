
package com.example.campsitereservationsystem.campsite

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CampsiteRepository(
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val col = db.collection("campsites")

    // Register a new campsite in Firebase
    suspend fun save(campsite: Campsite): String {
        val doc = col.document()
        val newCampsite = campsite.copy(id = doc.id)

        doc.set(newCampsite).await()

        return doc.id
    }

    // Get a campsite by its ID
    suspend fun getById(id: String): Campsite? {
        val doc = col.document(id).get().await()

        if (!doc.exists()) return null

        return doc.toObject(Campsite::class.java)
    }

    // Get all campsites registered by an owner
    suspend fun getByOwner(ownerId: String): List<Campsite> {
        val snapshot = col
            .whereEqualTo("ownerId", ownerId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Campsite::class.java)
        }
    }

    // Update an existing campsite
    suspend fun update(campsite: Campsite) {
        require(campsite.id.isNotBlank()) {
            "Campsite ID cannot be empty"
        }

        col.document(campsite.id)
            .set(campsite)
            .await()
    }

    // Delete an existing campsite from Firebase
    suspend fun delete(campsiteId: String) {
        require(campsiteId.isNotBlank()) {
            "Campsite ID cannot be empty"
        }

        col.document(campsiteId)
            .delete()
            .await()
    }
}
