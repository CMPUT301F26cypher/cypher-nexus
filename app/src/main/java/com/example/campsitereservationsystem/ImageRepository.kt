package com.example.campsitereservationsystem

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
/**
 * API's/LIBRARIES NEEDED FOR GENERAL UPLOAD IMAGE FUNCTIONS:
 *  -Android Studio Picker
 *  -Android
 *  -Firebase Storage (have to use to store images and videos )
 *  -Coil (Image-loading/displaying library)
 *
 * NOTES:
 * Going to put all the firebase-image functions into this class
 * -Upload image to firebase
 * -Delete photo from firebase
 * -Gather photo reference from firebase
 *
 * class ImageRepository:
 *         Stores image reference inside firebase
 *         Gets image reference from firebase
 *         Deletes campsite adn profile images from firebase
 */


class ImageRepository {
    private val db = Firebase.firestore

    private val campsRef = db.collection("campsites")
    private val usersRef = db.collection("users")

    //Save profile image to firebase
    fun saveProfileImage(userID: String, imageUri: String) {
        usersRef.document(userID).update("profileImage", imageUri)
    }
    //Delete profile image from firebase
    fun deleteProfileImage(userID: String) {
        usersRef.document(userID).update("profileImage", "")
    }
    //save campsite image to firebase
    fun saveCampsiteImage(campsiteID: String, imageUri: String){
        campsRef.document(campsiteID).update("imageUrl", imageUri)
    }
    //Delete campsite image from firebase
    fun deleteCampsiteImage(campsiteID: String){
        campsRef.document(campsiteID).update("imageUrl", "")
    }
    //Get profile image reference from firebase
    fun getProfileImage(userID: String, onSuccess: (String?) -> Unit){
        usersRef.document(userID).get()
            .addOnSuccessListener { document ->
                onSuccess(document.getString("profileImage"))
            }
    }

    //Get campsite image reference from firebase
    fun getCampsiteImage(campsiteID: String, onSuccess: (String?) -> Unit){
        campsRef.document(campsiteID).get()
            .addOnSuccessListener { document ->
                onSuccess(document.getString("imageUrl"))
            }
    }

}