
package com.example.campsitereservationsystem

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
 * Main activity with temporary navigation for testing.
 * Preserves the team's screens and QR functionality.
 */
class MainActivity : ComponentActivity() {

    private var user by mutableStateOf<User?>(null)
    private var userLoaded by mutableStateOf(false)

    private val userRepo =  UserRepository(onUserChanged = { updated: User? -> user = updated })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        userRepo.findByDeviceId(
            deviceId = DeviceId.get(this),
            onResult = { found: User? ->
                user = found
                userLoaded = true
            },
            onError = { userLoaded = true }
        )

        setContent {
            CampsiteReservationSystemTheme {

                // ==========================================
                // TEMPORARY START SCREEN
                // CHANGE ONLY THIS VALUE TO TEST SCREENS
                // ==========================================

                val startScreen = "schedule"

                // Available values:
                // "list"     -> My Campsites
                // "scanner"  -> QR Scanner
                // "schedule" -> Availability Calendar
                // "test"     -> Lottery and Image Upload
                // "welcome"  -> Welcome Screen

                // ==========================================
                // TEAM NAVIGATION
                // ==========================================

                var showTestMenu by remember {
                    mutableStateOf(startScreen == "test")
                }

                var showAvailability by remember {
                    mutableStateOf(startScreen == "schedule")
                }

                var savedSchedule by remember {
                    mutableStateOf(AvailabilitySchedule())
                }

                // ==========================================
                // CAMPSITE AND QR DATA
                // ==========================================

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

                // Null means use the team's navigation
                var currentQRScreen by remember {
                    mutableStateOf<String?>(
                        when (startScreen) {
                            "list" -> "list"
                            "scanner" -> "scanner"
                            else -> null
                        }
                    )
                }

                var showAccount by remember { mutableStateOf(startScreen == "account") }
                var showPolicy by remember { mutableStateOf(false) }


                // ==========================================
                // SCREEN NAVIGATION
                // ==========================================

                when (currentQRScreen) {

                    // OWNER CAMPSITES LIST
                    "list" -> {
                        OwnerCampsitesScreen(
                            campsites = campsites,
                            onCampsiteClick = { campsite ->
                                selectedCampsite = campsite
                                currentQRScreen = "details"
                            }
                        )
                    }

                    // OWNER CAMPSITE DETAILS
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

                    // QR CODE GENERATION
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

                    // QR CODE SCANNER
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

                    // ENTRANT CAMPSITE DETAILS
                    "entrantDetails" -> {
                        selectedCampsite?.let { campsite ->
                            EntrantCampsiteDetailsScreen(
                                campsite = campsite,

                                // Temporary test value
                                isLotteryActive = true,

                                onJoinWaitingList = {
                                    println("Join Waiting List clicked")
                                },

                                onBack = {
                                    currentQRScreen = "scanner"
                                }
                            )
                        }
                    }

                    // ==========================================
                    // ORIGINAL TEAM SCREENS
                    // ==========================================

                    else -> {

                        // To create an account and view app policy & guidelines
                        if (showAccount) {
                            if (!userLoaded) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            else {
                                AccountTest(
                                    user = user,
                                    campsites = campsites,
                                    repo = userRepo,
                                    onUserChange = { user = it },
                                    onExit = { showAccount = false }
                                )
                            }
                        }
                        else if (showPolicy) {
                            PolicyScreen(
                                onBack = { showPolicy = false }
                            )
                        }

                        // made else if
                        else if (showAvailability) {

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
                                },
                                onOpenAccount = {
                                    showAccount = true
                                }
                            )

                        } else {

                            WelcomeScreen(
                                onFindCampsite = {
                                    currentQRScreen = "scanner"
                                },

                                onManageCampsites = {
                                    showAvailability = true
                                },

                                // Finsihed showPolicy, so added it here
                                onPolicyClick = {
                                    showPolicy = true
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

    override fun onDestroy() {
        super.onDestroy()
        userRepo.stopListening()
    }
}

/**
 * Original team test menu.
 * Includes temporary navigation to QR features.
 */
@Composable
fun MainMenuManager(
    onBack: () -> Unit,
    onOpenOwnerCampsites: () -> Unit = {},
    onOpenQRScanner: () -> Unit = {},
    onOpenAccount: () -> Unit = {}  // new
) {

    var showLottery by remember {
        mutableStateOf(false)
    }

    var showImageUpload by remember {
        mutableStateOf(false)
    }

    // Original test arrays
    val lotteryTestArray1 = remember {
        mutableStateListOf(
            "John Man", "Jane Woman", "Petey Person",
            "Bob", "Brad", "Billy", "Bartolomeu",
            "Barry", "Terry", "Tom", "Timmy",
            "Jimmy", "Jack", "Robert"
        )
    }

    val lotteryTestArray2 = remember {
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

            // Account & Profile functionality
            Button(
                onClick = onOpenAccount
            ) {
                Text("Account")
            }

            // Team lottery functionality
            Button(
                onClick = {
                    showLottery = true
                }
            ) {
                Text("Lottery WIP")
            }

            // Team image upload functionality
            Button(
                onClick = {
                    showImageUpload = true
                }
            ) {
                Text("Image Upload Test Screen")
            }

            // Owner campsite functionality
            Button(
                onClick = onOpenOwnerCampsites
            ) {
                Text("My Campsites")
            }

            // QR scanner functionality
            Button(
                onClick = onOpenQRScanner
            ) {
                Text("Scan QR Code")
            }

            // Back to welcome
            Button(
                onClick = onBack
            ) {
                Text("Back to welcome")
            }
        }
    }
}

