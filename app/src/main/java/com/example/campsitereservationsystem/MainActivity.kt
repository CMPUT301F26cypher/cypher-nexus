package com.example.campsitereservationsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.campsitereservationsystem.qr.QRCodeScreen
import com.example.campsitereservationsystem.ui.theme.CampsiteReservationSystemTheme
import com.example.campsitereservationsystem.campsite.Campsite
import com.example.campsitereservationsystem.campsite.FakeCampsiteRepository
import com.example.campsitereservationsystem.campsite.OwnerCampsitesScreen
import com.example.campsitereservationsystem.campsite.OwnerCampsiteDetailsScreen
import com.example.campsitereservationsystem.qr.EntrantCampsiteDetailsScreen
import com.example.campsitereservationsystem.qr.QRCodeManager
import com.example.campsitereservationsystem.qr.QRCodeScannerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampsiteReservationSystemTheme {
                //Temporary repository until Firebase is connected
                val repository = remember { FakeCampsiteRepository() }

                val qrCodeManager = remember { QRCodeManager() }

                //Campsites that belong to the owner
                val campsites = remember(repository) {
                    repository.getOwnerCampsites()
                }

                //Currently selected campsite
                var selectedCampsite by remember {
                    mutableStateOf<Campsite?>(null)
                }

                // Current screen
                var currentScreen by remember {
                    mutableStateOf("scanner" )
                }

                when (currentScreen) {

                    "list" -> {
                        OwnerCampsitesScreen(
                            campsites = campsites,
                            onCampsiteClick = { campsite ->
                                selectedCampsite = campsite
                                currentScreen = "details"
                            }
                        )
                    }
                    "details" -> {
                        selectedCampsite?.let { campsite ->
                            OwnerCampsiteDetailsScreen(
                                campsite = campsite,
                                onViewQRCode = {
                                    currentScreen = "qr"
                                },
                                onBack = {
                                    currentScreen = "list"
                                }
                            )
                        }
                    }

                    "qr" -> {
                        selectedCampsite?.let { campsite ->
                            QRCodeScreen(
                                campsiteId = campsite.id,
                                campsiteName = campsite.name,
                                onBack = {
                                    currentScreen = "details"
                                }
                            )
                        }
                    }
                    "scanner" ->{
                        QRCodeScannerScreen(
                            onQRCodeScanned = { content ->
                                val campsiteId = qrCodeManager.decodeQRCode(content)

                                if (campsiteId != null) {

                                    // Find the campsite in our repository
                                    val campsite = repository.getCampsiteById(campsiteId)

                                    if (campsite != null) {
                                        selectedCampsite = campsite
                                        currentScreen = "entrantDetails"
                                    } else {
                                        println("Campsite not found")
                                    }

                                } else {
                                    println("Invalid QR code")
                                }
                            },
                            onBack = {
                                currentScreen = "list"
                            }
                        )
                    }
                    "entrantDetails"->{
                        selectedCampsite?.let { campsite ->
                            EntrantCampsiteDetailsScreen(
                                campsite = campsite,
                                isLotteryActive = true, //Temporal value for the test
                                onJoinWaitingList = {
                                    println("Join Waiting List clicked")
                                },
                                onBack = {
                                    currentScreen = "scanner"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CampsiteReservationSystemTheme {
        Greeting("Android")
    }
}