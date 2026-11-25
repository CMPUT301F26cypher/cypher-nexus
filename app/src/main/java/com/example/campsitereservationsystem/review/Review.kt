package com.example.campsitereservationsystem.review

data class Review(
    val id: String = "",
    val campsiteId: String = "",
    val entrantId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Long = 0L
)