package com.example.campsitereservationsystem.duplicates

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class CampsiteRepository {
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")
    private val _campsites = mutableStateListOf<Campsite>()
    val campsites: List<Campsite>
        get() = _campsites
    init {
        campsitesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _campsites.clear()
            snapshot?.documents?.forEach { document ->
                val campsite =
                    document.toObject(Campsite::class.java)
                if (campsite != null) {
                    _campsites.add(campsite.copy(campsiteID = document.id))
                }
            }
        }
    }
    fun saveCampsite(documentId: String, campsite: Campsite) {
        campsitesRef.document(documentId).set(campsite)
    }
    fun updateCampsite(
        documentId: String,
        updatedCampsite: Campsite
    ) {
        campsitesRef.document(documentId).set(updatedCampsite)
    }
    fun deleteCampsite(documentId: String) {
        campsitesRef.document(documentId).delete()
    }
}