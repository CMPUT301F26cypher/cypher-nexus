package com.example.campsitereservationsystem

data class Entrant(
    val name: String,
    val entrantID: String,
    val deviceID: String,
    val role: Role = Role.ENTRANT, //
    val profileImage: String,
    val notifPreference: Boolean
)



