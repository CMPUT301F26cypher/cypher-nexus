package com.example.campsitereservationsystem

data class Lottery (
    val open: Boolean = false, //true mean entrant can join the lottery
    val capacity: Int,
    val startDate: String,
    val endDate: String,
    val campsite: Campsite
)
