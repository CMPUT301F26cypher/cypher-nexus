package com.example.campsitereservationsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EntrantBookingListScreen(
    onBack: () -> Unit
){
    //A box is used to allow the back button to be in the bottom left at all times
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Adds spacing from the screen edges
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ){
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Booking",
                fontSize = 40.sp
            )
        }
        //LazyColumn for the list allows it to be scrollable
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            item {

            }
        }
        Button(
            onClick = { onBack() },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .scale(1.2f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF819F5C)
            )
        ) {
            Text(
                text = "Back",
                color = Color.Black
            )
        }
    }

}