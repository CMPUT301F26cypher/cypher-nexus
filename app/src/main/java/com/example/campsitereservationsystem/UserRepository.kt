package com.example.campsitereservationsystem

import android.content.Context
import android.provider.Settings
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.example.campsitereservationsystem.campsite.Campsite
/* Just commented out data class as we have another separate data class

data class User(
    val id: String = "", // Device ID
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    val roles: Set<Role> = setOf(Role.ENTRANT),
    val notificationsEnabled: Boolean = true,
    val profileImage: String = "",
    val deviceId: String = ""
)
*/


// Gets the device's ID
// US 01.09.01
//  // As an entrant, I want to be identified by my device, so that I do not have to use a username and password.
object DeviceId {
    fun get(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}

/*
    UserRepository:
    Keeps track of user's data.
    Can log them in without username or password.
    It remembers the user's Device ID.
 */

class UserRepository(
    private val onUserChanged: (User?) -> Unit = {}
) {
    private val db = Firebase.firestore
    private val usersRef = db.collection("users")

    private var listener: ListenerRegistration? = null

    private fun toUser(doc: DocumentSnapshot): User {
        val roleNames = doc.get("roles") as? List<*> ?: emptyList<Any>()
        val roles = roleNames
            .mapNotNull { runCatching { Role.valueOf(it.toString()) }.getOrNull() }.toSet()
        return User(
            id = doc.id,
            name = doc.getString("name") ?: "",
            email = doc.getString("email") ?: "",
            phone = doc.getString("phone") ?: "",
            roles = roles.ifEmpty { setOf(Role.ENTRANT) },
            notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true,
            profileImage = doc.getString("profileImage") ?: "",
            deviceId = doc.getString("deviceId") ?: ""
        )
    }

    private fun toMap(user: User) = mapOf(
        "name" to user.name,
        "email" to user.email,
        "phone" to user.phone,
        "roles" to user.roles.map { it.name },
        "notificationsEnabled" to user.notificationsEnabled,
        "profileImage" to user.profileImage,
        "deviceId" to user.deviceId
    )


    // Functions for firebase
    // startListening watches the user's document
    fun startListening(userId: String) {
        listener?.remove()
        listener = usersRef.document(userId).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                return@addSnapshotListener
            }
            if (snapshot.exists()) onUserChanged(toUser(snapshot))
            else onUserChanged(null)
        }
    }

    // Stops getting firestore updates
    fun stopListening() {
        listener?.remove()
        listener = null
    }

    // Checks if the device ID is remembered
    fun findByDeviceId(
        deviceId: String,
        onResult: (User?) -> Unit,
        onError: () -> Unit = {}
    ) {
        if (deviceId.isBlank()) {
            onResult(null)
            return
        }
        // made some changes so the user can log in by their device ID
        usersRef.whereEqualTo("deviceId", deviceId).limit(1).get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.documents.firstOrNull()?.let { toUser(it) })
                val user = snapshot.documents.firstOrNull()?.let { toUser(it) }
            }
            .addOnFailureListener { onError() }
    }

    // Logging in and Registering
    fun register(
        user: User,
        password: String,
        onSuccess: (User) -> Unit,
        onEmailTaken: () -> Unit,
        onError: () -> Unit = {}
    ) {
        val email = user.email.trim().lowercase()
        usersRef.whereEqualTo("email", email).limit(1).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    onEmailTaken()
                    return@addOnSuccessListener
                }
                val doc = usersRef.document()
                val newUser = user.copy(
                    id = doc.id,
                    email = email
                )
                doc.set(toMap(newUser))
                    .addOnSuccessListener {
                        startListening(newUser.id)
                        onSuccess(newUser)
                    }
                    .addOnFailureListener { onError() }
            }
            .addOnFailureListener { onError() }

    }

    fun login(
        email: String,
        password: String,
        deviceId: String,
        onResult: (User?) -> Unit,
        onError: () -> Unit = {}
    ) {
        usersRef.whereEqualTo("email", email.trim().lowercase()).limit(1).get()
            .addOnSuccessListener { snapshot ->
                val found = snapshot.documents.firstOrNull()?.let { toUser(it) }
                if (found == null) {
                    onResult(null)
                    return@addOnSuccessListener
                }

                // Remember the device
                val remembered = found.copy(deviceId = deviceId)
                usersRef.document(found.id).set(toMap(remembered))
                    .addOnSuccessListener {
                        startListening(remembered.id)
                        onResult(remembered)
                    }
                    .addOnFailureListener { onError() }
            }
            .addOnFailureListener { onError() }
    }

    // Stop listening, but still remeber the device ID for next time
    fun logout(user: User) {
        stopListening()
    }

    fun delete(user: User) {
        stopListening()
        usersRef.document(user.id).delete()
    }

    fun updateUser(user: User) {
        usersRef.document(user.id).set(toMap(user))
    }
}
