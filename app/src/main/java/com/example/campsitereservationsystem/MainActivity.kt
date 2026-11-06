// Starting the app, applying its theme, and handing screen changes to CampsiteApp.

package com.example.campsitereservationsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.campsitereservationsystem.ui.theme.CampsiteReservationSystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Activity setup
        // Letting Android finish activity setup before installing the Compose content.
        super.onCreate(savedInstanceState)

        // Drawing behind the system bars and letting each screen handle its padding.
        enableEdgeToEdge()

        // App screens
        setContent {
            // Keeping the same app theme around Welcome and the screens it opens.
            CampsiteReservationSystemTheme {
                // Starting on Welcome through the NavHost in CampsiteApp.kt.
                // Keeping the owner and QR routes together in CampsiteRoutes.kt.
                CampsiteApp()
            }
        }
    }
}
