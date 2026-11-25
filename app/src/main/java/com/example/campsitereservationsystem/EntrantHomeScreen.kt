package com.example.campsitereservationsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*

 */
@Composable
fun EntrantHomeScreen(
    onBrowseCampsites: () -> Unit,
    onJoinedLotteries: () -> Unit,
    onBooking: () -> Unit,
    onEntrantReservations: () -> Unit,
    onQRScan: () -> Unit,
    onEntrantProfile: () -> Unit
){

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        //BROWSE CAMPSITES
        EntrantHomeScreenButtons("Browse Campsites") {
            onBrowseCampsites()
        }
        Spacer(modifier = Modifier.height(25.dp))

        //JOINED LOTTERIES
        EntrantHomeScreenButtons("Joined Lotteries") {
            onJoinedLotteries()
        }

        Spacer(modifier = Modifier.height(25.dp))

        //BOOKING
        EntrantHomeScreenButtons("Booking") {
            onBooking()
        }

        Spacer(modifier = Modifier.height(25.dp))

        //RESERVATIONS
        EntrantHomeScreenButtons("Reservations") {
            onEntrantReservations()
        }

        Spacer(modifier = Modifier.height(25.dp))

        EntrantHomeScreenButtons("QR Scan") {
            onQRScan()
        }

        Spacer(modifier = Modifier.height(25.dp))

        EntrantHomeScreenButtons("Profile") {
            onEntrantProfile()
        }
    }
}


/*
Creates a button with a specific formating that is used for the Owner Home Screen.
When clicked the button sets a passed boolean to true when clicked.
 */
@Composable
fun EntrantHomeScreenButtons(
    buttonText: String,
    onClick: () -> Unit
) {
    //MY CAMPSITES
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF819F5C)
        ),
        modifier = Modifier.size(
            width = 300.dp,
            height = 75.dp,
        ),
        shape = RoundedCornerShape(5.dp)
    ) {
        Text(
            text = buttonText,
            color = Color.Black,
            fontSize = 25.sp
        )
    }
}