
package com.example.campsitereservationsystem

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import com.example.campsitereservationsystem.ui.theme.CampsiteReservationSystemTheme

// Campsite functionality
import com.example.campsitereservationsystem.campsite.Campsite
import com.example.campsitereservationsystem.campsite.FakeCampsiteRepository
import com.example.campsitereservationsystem.campsite.OwnerCampsitesScreen
import com.example.campsitereservationsystem.campsite.OwnerCampsiteDetailsScreen

// QR functionality
import com.example.campsitereservationsystem.qr.QRCodeManager
import com.example.campsitereservationsystem.qr.QRCodeScreen
import com.example.campsitereservationsystem.qr.QRCodeScannerScreen
import com.example.campsitereservationsystem.qr.EntrantCampsiteDetailsScreen

/**
 * Opens the welcome page and keeps the existing
 * feature test menu reachable.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CampsiteReservationSystemTheme {

                // Existing team navigation
                var showTestMenu by remember {
                    mutableStateOf(false)
                }

                var showAvailability by remember {
                    mutableStateOf(false)
                }

                // Preserve the team's availability schedule
                var savedSchedule by remember {
                    mutableStateOf(AvailabilitySchedule())
                }

                // Temporary campsite repository
                val repository = remember {
                    FakeCampsiteRepository()
                }

                val qrCodeManager = remember {
                    QRCodeManager()
                }

                val campsites = remember(repository) {
                    repository.getOwnerCampsites()
                }

                var selectedCampsite by remember {
                    mutableStateOf<Campsite?>(null)
                }

                // Null means we are using the team's navigation
                var currentQRScreen by remember {
                    mutableStateOf<String?>(null)
                }

                // QR screens take priority while they are open
                when (currentQRScreen) {

                    "list" -> {
                        OwnerCampsitesScreen(
                            campsites = campsites,
                            onCampsiteClick = { campsite ->
                                selectedCampsite = campsite
                                currentQRScreen = "details"
                            }
                        )
                    }

                    "details" -> {
                        selectedCampsite?.let { campsite ->
                            OwnerCampsiteDetailsScreen(
                                campsite = campsite,
                                onViewQRCode = {
                                    currentQRScreen = "qr"
                                },
                                onBack = {
                                    currentQRScreen = "list"
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
                                    currentQRScreen = "details"
                                }
                            )
                        }
                    }

                    "scanner" -> {
                        QRCodeScannerScreen(
                            onQRCodeScanned = { content ->

                                val campsiteId =
                                    qrCodeManager.decodeQRCode(content)

                                if (campsiteId != null) {

                                    val campsite =
                                        repository.getCampsiteById(campsiteId)

                                    if (campsite != null) {
                                        selectedCampsite = campsite
                                        currentQRScreen = "entrantDetails"
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Campsite not found",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                } else {
                                    Toast.makeText(
                                        this,
                                        "Invalid QR code",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onBack = {
                                currentQRScreen = null
                            }
                        )
                    }

                    "entrantDetails" -> {
                        selectedCampsite?.let { campsite ->
                            EntrantCampsiteDetailsScreen(
                                campsite = campsite,

                                // Temporary test value
                                isLotteryActive = true,

                                // Waiting list integration pending
                                onJoinWaitingList = {
                                    println(
                                        "Join Waiting List clicked"
                                    )
                                },

                                onBack = {
                                    currentQRScreen = "scanner"
                                }
                            )
                        }
                    }

                    else -> {

                        // Original navigation from main
                        if (showAvailability) {

                            AvailabilityScreen(
                                schedule = savedSchedule,
                                onSave = {
                                    savedSchedule = it
                                },
                                onBack = {
                                    showAvailability = false
                                }
                            )

                        } else if (showTestMenu) {

                            MainMenuManager(
                                onBack = {
                                    showTestMenu = false
                                },
                                onOpenOwnerCampsites = {
                                    currentQRScreen = "list"
                                },
                                onOpenQRScanner = {
                                    currentQRScreen = "scanner"
                                }
                            )

                        } else {

                            WelcomeScreen(
                                onFindCampsite = {
                                    // Temporary: open QR scanner
                                    currentQRScreen = "scanner"
                                },
                                onManageCampsites = {
                                    showAvailability = true
                                },
                                onPolicyClick = {
                                    Toast.makeText(
                                        this,
                                        "The policy page is not connected yet.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onOpenTestMenu = {
                                    showTestMenu = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/*
 * The main menu, if need be later we modify
 * how exactly this works to account for different user types.
 */
@Composable
fun MainMenuManager(
    onBack: () -> Unit,
    onOpenOwnerCampsites: () -> Unit = {},
    onOpenQRScanner: () -> Unit = {}
) {

    var showLottery by remember {
        mutableStateOf(false)
    }

    var showImageUpload by remember {
        mutableStateOf(false)
    }

    // Original test arrays from main
    var lotteryTestArray1 = remember {
        mutableStateListOf(
            "John Man", "Jane Woman", "Petey Person",
            "Bob", "Brad", "Billy", "Bartolomeu",
            "Barry", "Terry", "Tom", "Timmy",
            "Jimmy", "Jack", "Robert"
        )
    }

    var lotteryTestArray2 = remember {
        mutableStateListOf(
            "John Man", "Jane Woman", "Petey Person"
        )
    }

    BackHandler {
        if (showLottery) {
            showLottery = false
        } else if (showImageUpload) {
            showImageUpload = false
        } else {
            onBack()
        }
    }

    // Original team functionality
    if (showLottery) {

        LotteryScreen(
            onBack = {
                showLottery = false
            },
            lotteryTestArray2
        )

    } else if (showImageUpload) {

        ImageUploadTestScreen(
            onBack = {
                showImageUpload = false
            }
        )

    } else {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Original lottery test button
            Button(
                onClick = {
                    showLottery = true
                }
            ) {
                Text("Lottery WIP")
            }

            // Original image upload test button
            Button(
                onClick = {
                    showImageUpload = true
                }
            ) {
                Text("Image Upload Test Screen")
            }

            // New QR functionality
            Button(
                onClick = onOpenOwnerCampsites
            ) {
                Text("My Campsites")
            }

            Button(
                onClick = onOpenQRScanner
            ) {
                Text("Scan QR Code")
            }

            // Original back button
            Button(
                onClick = onBack
            ) {
                Text("Back to welcome")
            }
        }
    }
}
