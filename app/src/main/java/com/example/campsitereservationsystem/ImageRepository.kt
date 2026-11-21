package com.example.campsitereservationsystem

import android.content.Context
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.Blob
import android.util.Log
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

    private val imageMaxSize = 300_000 //Can change to anything under 900,000 (900KB)


    //Save profile image to firebase
    fun saveProfileImage(userID: String, imageBytes:ByteArray) {
        if (imageBytes.size > imageMaxSize)
            return

        usersRef.document(userID).update(
            "profileImage",
            Blob.fromBytes(imageBytes))
    }
    //Delete profile image from firebase
    fun deleteProfileImage(userID: String) {
        usersRef.document(userID).update("profileImage", null)
    }
    //save campsite image to firebase
    fun saveCampsiteImage(campsiteID: String, imageBytes: ByteArray) {

        if (imageBytes.size > imageMaxSize) {
            return
        }

        campsRef.document(campsiteID)
            .update("imageData", Blob.fromBytes(imageBytes))
    }
    //Delete campsite image from firebase
    fun deleteCampsiteImage(campsiteID: String){
        campsRef.document(campsiteID).update("imageData", null)
    }
    //Get profile image reference from firebase
    fun getProfileImage(userID: String, onSuccess: (ByteArray?) -> Unit){
        usersRef.document(userID).get()
            .addOnSuccessListener { document ->
                onSuccess(document.getBlob("profileImage")?.toBytes())
            }
    }

    //Get campsite image reference from firebase
    fun getCampsiteImage(campsiteID: String, onSuccess: (ByteArray?) -> Unit){
        campsRef.document(campsiteID).get()
            .addOnSuccessListener { document ->
                onSuccess(document.getBlob("imageData")?.toBytes())
            }
    }

    fun convertUriToBytes(context: Context, uri: Uri): ByteArray? {
        val inputStream = context.contentResolver.openInputStream(uri)

        val imageBytes = inputStream?.use {
            it.readBytes()
        }

        return imageBytes
    }

}