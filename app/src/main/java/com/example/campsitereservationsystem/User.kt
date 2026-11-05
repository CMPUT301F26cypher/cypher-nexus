package com.example.campsitereservationsystem

data class User(
    val id: String = "", // Device ID
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    val roles: Set<Role> = setOf(Role.ENTRANT),
    var notificationsEnabled: Boolean = true,
    var profileImage: String = ""
)



