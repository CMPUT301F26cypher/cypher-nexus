// Showing the app’s entry points for scanning, browsing, availability, policy, and the test menu.

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
// Receiving the four button actions from the app graph.
fun WelcomeScreen(
    // Accepting the entrant and owner actions as callbacks.
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

    // Filling the page with the Welcome background and respecting the scaffold padding.
    Scaffold(containerColor = background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Applying the system padding before the page’s own margins.
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placing the campsite illustration above the title and fitting it into a fixed-height area.
            Image(
                painter = painterResource(R.drawable.campsite_welcome),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                // Keeping the entire illustration in view by fitting it within the available width and height.
                contentScale = ContentScale.Fit
            )

            // Showing the app name as a centred heading.
            Text(
                text = "Campsite Reservations",
                modifier = Modifier.padding(bottom = 32.dp),
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = green,
                textAlign = TextAlign.Center
            )

            Text(
                // Introducing the choices with a short heading before asking the visitor how to continue.
                text = "Welcome",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                // Leaving room below the prompt to separate the introduction from the two actions.
                text = "How would you like to continue?",
                modifier = Modifier.padding(bottom = 28.dp),
                fontSize = 16.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )

            // Presenting the entrant action as the filled primary button.
            Button(
                // Connecting the filled button directly to the entrant action supplied by the app graph.
                onClick = onFindCampsite,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = green,
                    // Using a light label and icon on the dark green button for contrast.
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.welcome_search),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                // Separating the icon from the text without reducing the button’s overall tap area.
                Spacer(modifier = Modifier.width(12.dp))
                Text("Find a campsite", fontSize = 18.sp)
            }
            Text(
                // Placing a short explanation below the entrant action.
                text = "Search campsites and enter lotteries.",
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                fontSize = 14.sp,
                color = smallTextColor,
                textAlign = TextAlign.Center
            )

            // Presenting the owner action as an outlined button with the same size as the entrant action.
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
                    // Using the tent icon to distinguish the owner choice.
                    painter = painterResource(R.drawable.welcome_tent),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Manage my campsites", fontSize = 18.sp)
            }
            Text(
                text = "Register campsites and manage bookings.",
                // Leaving more space after the main choices so the policy and test menu links read as a separate group.
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp),
                fontSize = 14.sp,
                color = smallTextColor,
                textAlign = TextAlign.Center
            )

            // Opening the app policy from Welcome.
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
