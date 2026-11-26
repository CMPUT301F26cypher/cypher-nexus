
/*
 * CampsiteRepository.kt
 *
 * This file is part of the data layer of our Model in MVC.
 * Here we handle the connection between our application
 * and the campsites collection in Firebase Firestore.
 *
 * We use this repository to save, retrieve, update and
 * delete campsites. We also use it to keep the list of
 * campsites updated when something changes in Firebase.
 *
 * Outstanding issues:
 * - For now, errors from the snapshot listener are not
 *   displayed to the user. We still need to handle these
 *   errors so the user knows if the campsites could not be loaded.
 */

package com.example.campsitereservationsystem.OwnerCampsitesPath

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.ListenerRegistration

/**
 * This class handles all the operations related to
 * campsites in Firebase Firestore.
 *
 * We use this class so the controller does not have
 * to communicate directly with Firebase.
 *
 * @param db The Firestore database we use to store
 * and retrieve the campsites.
 */
class CampsiteRepository(
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Here we access the collection where all the campsites are stored
    private val col = db.collection("campsites")

    // We use a state list so the UI can update when the campsites change
    private val storedCampsites = mutableStateListOf<Campsite>()

    /**
     * The current list of campsites loaded by the snapshot listener.
     * We expose it as a List so other classes can read it without
     * directly modifying the stored list through this property.
     */
    val campsites: List<Campsite> get() = storedCampsites

    /**
     * This function listens for changes in the campsites collection.
     *
     * Every time something changes in Firebase, we update our
     * local list so the user can see the updated campsites
     * without having to reload the screen manually.
     *
     * @return The listener registration that we use to stop
     * listening when we no longer need it.
     */
    fun watchCampsites(): ListenerRegistration {

        return col.addSnapshotListener { snapshot, error ->

            // For now, we stop processing this update if there is an error
            if (error != null) {
                return@addSnapshotListener
            }

            // We clear the previous list before adding the updated campsites
            storedCampsites.clear()

            snapshot?.documents?.forEach { document ->

                val campsite = document.toObject(Campsite::class.java)

                if (campsite != null) {

                    // We use the Firebase document ID as the campsite ID
                    storedCampsites.add(
                        campsite.copy(id = document.id)
                    )
                }
            }
        }
    }

    /**
     * Saves a new campsite in Firebase.
     *
     * Here we create a new document and use its ID
     * as the ID of the campsite we are registering.
     *
     * @param campsite The campsite we want to save.
     * @return The ID of the campsite saved in Firebase.
     */
    suspend fun save(campsite: Campsite): String {

        val doc = col.document()
        val newCampsite = campsite.copy(id = doc.id)

        doc.set(newCampsite).await()

        return doc.id
    }

    /**
     * This function allows us to find a specific campsite
     * using its ID.
     *
     * We use this when we need to retrieve the information
     * of a particular campsite, for example when the user
     * scans its QR code or opens its details.
     *
     * @param id The ID of the campsite we are looking for.
     * @return The campsite if it is found, or null if the
     * document does not exist or cannot be converted to a campsite.
     */
    suspend fun getById(id: String): Campsite? {

        val doc = col.document(id).get().await()

        if (!doc.exists()) return null

        return doc.toObject(Campsite::class.java)?.copy(id = doc.id)
    }

    /**
     * Gets all the campsites registered by a specific owner.
     *
     * We use the owner ID so we only retrieve the campsites
     * that belong to that owner.
     *
     * @param ownerId The ID of the owner.
     * @return A list of campsites registered by that owner.
     * If no campsites are found, the list will be empty.
     */
    suspend fun getByOwner(ownerId: String): List<Campsite> {

        val snapshot = col
            .whereEqualTo("ownerId", ownerId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->

            doc.toObject(Campsite::class.java)?.copy(id = doc.id)
        }
    }

    /**
     * Gets all the campsites stored in Firebase.
     *
     * We use this function for the admin browsing functionality
     * so the admin can see all the registered campsites.
     *
     * Related user story: US 03.04.01.
     *
     * @return A list of all the campsites retrieved from Firebase.
     */
    suspend fun getAll(): List<Campsite> {

        val snapshot = col.get().await()

        return snapshot.documents.mapNotNull { doc ->

            doc.toObject(Campsite::class.java)?.copy(id = doc.id)
        }
    }

    /**
     * Updates the information of a campsite in Firebase.
     *
     * We use the campsite ID to find the document where
     * we want to save the updated information.
     *
     * @param campsite The campsite with the updated information.
     * @throws IllegalArgumentException If the campsite ID is empty.
     */
    suspend fun update(campsite: Campsite) {

        require(campsite.id.isNotBlank()) {
            "Campsite ID cannot be empty"
        }

        col.document(campsite.id)
            .set(campsite)
            .await()
    }

    /**
     * Deletes a campsite document from Firebase using its ID.
     *
     * Before deleting it, we make sure that the campsite ID
     * is not empty.
     *
     * @param campsiteId The ID of the campsite we want to delete.
     * @throws IllegalArgumentException If the campsite ID is empty.
     */
    suspend fun delete(campsiteId: String) {

        require(campsiteId.isNotBlank()) {
            "Campsite ID cannot be empty"
        }

        col.document(campsiteId)
            .delete()
            .await()
    }
}
