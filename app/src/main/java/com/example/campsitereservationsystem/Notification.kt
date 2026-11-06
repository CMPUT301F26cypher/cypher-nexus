package com.example.campsitereservationsystem

data class Notification(
    val notifID: String = "",
    val senderID: String = "",
    val message: String = "",
    val receiverID: String = "",
    val timestamp: Long = 0L
)
