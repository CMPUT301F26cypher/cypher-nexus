package com.example.campsitereservationsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.campsitereservationsystem.ui.theme.CampsiteReservationSystemTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // ==========================================
        // ACTIVITY SETUP
        // ==========================================
        super.onCreate(savedInstanceState)

        // Drawing behind the system bars and letting each screen handle its padding.
        enableEdgeToEdge()

        // ==========================================
        // APP SCREENS
        // ==========================================
        setContent {
            CampsiteReservationSystemTheme {
                // Starting on Welcome through the NavHost in CampsiteApp.kt.
                // Keeping the owner and QR routes together in CampsiteRoutes.kt.
                CampsiteApp()
            }
        }
    }
}