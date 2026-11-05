package com.example.campsitereservationsystem

import android.content.Context
import android.provider.Settings
import com.example.campsitereservationsystem.Role.ENTRANT
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


object DeviceId {
    fun get(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}
// Info about user
data class User (
    val id: String = "", // device ID
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val roles: Set<Role> = setOf(Role.ENTRANT), // bc default role is entrant
    val notificationsEnabled: Boolean = true
)

class UserRepository(
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val col = db.collection("users")

    // US 01.09.01
    // As an entrant, I want to be identified by my device, so that I do not have to use a username and password.
    suspend fun getById(id: String): User? {
        val doc = col.document(id).get().await()
        if (!doc.exists()) return null
        val roleNames = doc.get("roles") as? List<*> ?: emptyList<Any>()
        val roles =
            roleNames.mapNotNull { runCatching { Role.valueOf(it.toString()) }.getOrNull() }.toSet()
        return User(
            id = doc.id,
            name = doc.getString("name") ?: "",
            email = doc.getString("email") ?: "",
            phone = doc.getString("phone") ?: "",
            roles = roles.ifEmpty { setOf(Role.ENTRANT) },
            notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true
        )
    }

    // US 01.07.03
    // As an entrant, I want to update my personal information, such as my name, email, and phone number, on my profile.
    suspend fun save(user: User) {
        col.document(user.id).set(
            mapOf(
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "roles" to user.roles.map { it.name },
                "notificationsEnabled" to user.notificationsEnabled
            )
        ).await()
    }

    // US 01.08.01
    // As an entrant, I want to delete my profile.
    suspend fun delete(id: String) {
        col.document(id).delete().await()
    }


}