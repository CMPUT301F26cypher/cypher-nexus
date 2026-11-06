
package com.example.campsitereservationsystem.campsite

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.ListenerRegistration

class CampsiteRepository(
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val col = db.collection("campsites")
    private val storedCampsites = mutableStateListOf<Campsite>()

    val campsites: List<Campsite> get() = storedCampsites

    //load campsites for screen search
    fun watchCampsites(): ListenerRegistration {
        return col.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            storedCampsites.clear()
            snapshot?.documents?.forEach { document ->
                val campsite = document.toObject(Campsite::class.java)
                if (campsite != null) {
                    storedCampsites.add(
                        campsite.copy(id = document.id)
                    )
                }
            }
        }
    }

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

        return doc.toObject(Campsite::class.java)?.copy(id = doc.id)
    }

    // Get all campsites registered by an owner
    suspend fun getByOwner(ownerId: String): List<Campsite> {
        val snapshot = col
            .whereEqualTo("ownerId", ownerId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Campsite::class.java)?.copy(id = doc.id)
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
