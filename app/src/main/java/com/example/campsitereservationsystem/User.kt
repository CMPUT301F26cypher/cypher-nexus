package com.example.campsitereservationsystem

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



