package com.example.campsitereservationsystem.AdminPages

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
fun AdminHomeScreen(
    onImages: () -> Unit,
    onNotifications: () -> Unit,
    onProfiles:() -> Unit,
    onCampsites: () -> Unit,
    onMessages: () -> Unit
){

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        //BROWSE CAMPSITES
        AdminMenuButtons("Image Gallery"){
            onImages()
        }
        Spacer(modifier = Modifier.height(25.dp))

        //JOINED LOTTERIES
        AdminMenuButtons("Review Notifications"){
            onNotifications()
        }

        Spacer(modifier = Modifier.height(25.dp))

        AdminMenuButtons("Review Profiles"){
            onProfiles()
        }

        Spacer(modifier = Modifier.height(25.dp))

        //BOOKING
        AdminMenuButtons("Review Campsites"){
            onCampsites()
        }

        Spacer(modifier = Modifier.height(25.dp))

        //RESERVATIONS
        AdminMenuButtons("Review Messages"){
            onMessages()
        }

        Spacer(modifier = Modifier.height(25.dp))


    }
}


/*
Button creation for Admin Menu screen and its specific formatting
 */
@Composable
fun AdminMenuButtons(
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