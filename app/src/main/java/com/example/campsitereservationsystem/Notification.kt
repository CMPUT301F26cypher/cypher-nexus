package com.example.campsitereservationsystem

data class Notification(
    val notifID: String,
    val message: String,
    val receiverID: String,
    val timestamp: Long
)
