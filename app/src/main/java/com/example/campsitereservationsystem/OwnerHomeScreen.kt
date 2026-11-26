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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import com.example.campsitereservationsystem.reusableButtons.InboxButton

/*

 */
@Composable
fun OwnerHomeScreen(
    onMyCampsites: () -> Unit,
    onMyLotteries: () -> Unit,
    onOwnerReservations: () -> Unit,
    onOwnerNotifications: () -> Unit,
    onInbox: () -> Unit
) {
    //OWNER SCREEN VARIABLE
    var showCampsites by remember { mutableStateOf(false) }
    var showLotteries by remember { mutableStateOf(false) }
    var showReservations by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            //MY CAMPSITES
            OwnerHomeScreenButtons("My Campsites") {
                onMyCampsites()
            }
            Spacer(modifier = Modifier.height(25.dp))

            //MY LOTTERIES
            OwnerHomeScreenButtons("My Lotteries") {
                onMyLotteries()
            }

            Spacer(modifier = Modifier.height(25.dp))

            //RESERVATIONS
            OwnerHomeScreenButtons("Reservations") {
                onOwnerReservations()
            }

            Spacer(modifier = Modifier.height(25.dp))

            //NOTIFICATIONS
            OwnerHomeScreenButtons("Notifications") {
                onOwnerNotifications()
            }
        }
        InboxButton(
            onClick = onInbox,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)

        )
    }
}


/*
Creates a button with a specific formating that is used for the Owner Home Screen.
When clicked the button sets a passed boolean to true when clicked.
 */
@Composable
fun OwnerHomeScreenButtons(
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