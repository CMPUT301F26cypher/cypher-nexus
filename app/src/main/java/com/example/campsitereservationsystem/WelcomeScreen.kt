package com.example.campsitereservationsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The opening page for choosing the entrant or owner area. */
@Composable
fun WelcomeScreen(
    onFindCampsite: () -> Unit,
    onManageCampsites: () -> Unit,
    onPolicyClick: () -> Unit,
    onOpenTestMenu: () -> Unit,
    onBrowseCampsites: () -> Unit
) {
    val green = Color(0xFF214F3D)
    val background = Color(0xFFFAF9F3)
    val textColor = Color(0xFF1C2330)
    val smallTextColor = Color(0xFF59616B)

    Scaffold(containerColor = background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.campsite_welcome),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Campsite Reservations",
                modifier = Modifier.padding(bottom = 32.dp),
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = green,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Welcome",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "How would you like to continue?",
                modifier = Modifier.padding(bottom = 28.dp),
                fontSize = 16.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onFindCampsite,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = green,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.welcome_search),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Find a campsite", fontSize = 18.sp)
            }
            Text(
                text = "Search campsites and enter lotteries.",
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                fontSize = 14.sp,
                color = smallTextColor,
                textAlign = TextAlign.Center
            )

            OutlinedButton(
                onClick = onBrowseCampsites,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, green),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = green)
            ) {
                Text("Browse and filter campsites", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onManageCampsites,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, green),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = green)
            ) {
                Icon(
                    painter = painterResource(R.drawable.welcome_tent),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Manage my campsites", fontSize = 18.sp)
            }
            Text(
                text = "Register campsites and manage bookings.",
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp),
                fontSize = 14.sp,
                color = smallTextColor,
                textAlign = TextAlign.Center
            )

            TextButton(onClick = onPolicyClick) {
                Text(
                    text = "App policy & guidelines",
                    color = green,
                    textDecoration = TextDecoration.Underline
                )
            }

            TextButton(onClick = onOpenTestMenu) {
                Text("Open test menu", color = green)
            }
        }
    }
}
