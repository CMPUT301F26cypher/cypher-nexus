
package com.example.campsitereservationsystem

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp

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

//Notification Functionality
import androidx.compose.ui.platform.LocalContext

/**
 * Main activity with temporary navigation for testing.
 * Preserves the team's screens and QR functionality.
 */
class MainActivity : ComponentActivity() {

    /** Opens the existing team screens and keeps the local booking session together. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                // "booking"  -> Entrant booking demo

                // ==========================================
                // REPOSITORIES
                // ==========================================
                val lotteryRepository = remember {
                    LotteryRepository()
                }

                // ==========================================
                // TEAM NAVIGATION
                // ==========================================

                var showTestMenu by remember {
                    mutableStateOf(startScreen == "test")
                }

                var showAvailability by remember {
                    mutableStateOf(startScreen == "schedule")
                }

                var showEntrantBooking by remember {
                    mutableStateOf(startScreen == "booking")
                }

                // Use the reservation-domain model for the owner's existing sample calendar.
                val bookingSite = remember {
                    com.example.campsitereservationsystem.Campsite(
                        campsiteID = "SAMPLE_SPRUCE_RIDGE",
                        ownerID = "sample-owner",
                        location = "Jasper area, Alberta",
                        description = "The sample campsite used by the availability calendar.",
                        rules = "Keep the site clean. Quiet hours are 10 pm to 7 am."
                    )
                }

                // Both sides read the same local repository; no Firebase request is made here.
                val reservationStore = remember {
                    val localStore = ReservationRepository(localOnly = true)
                    localStore.saveAvailabilitySchedule(bookingSite.campsiteID, AvailabilitySchedule())
                    localStore
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

                var user by remember { mutableStateOf<User?>(null) }
                var showAccount by remember { mutableStateOf(startScreen == "account") }
                var showPolicy by remember { mutableStateOf(false) }

                // Reuse the team's device identity when a profile has not been opened yet.
                val deviceGuest = remember { User(id = DeviceId.get(this)) }
                val bookingGuest = user ?: deviceGuest
                val bookingController = remember(bookingGuest.id) {
                    ReservationController(
                        campsite = bookingSite,
                        campsiteName = "Spruce Ridge",
                        cancellationPolicy = "Contact the owner about changes. Cancellation is not available in this prototype.",
                        winnerId = bookingGuest.id,
                        repository = reservationStore,
                        // Test menu fixture only. Replace this with the real lottery result later.
                        isWinner = true
                    )
                }

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
                            AccountTest(
                                user = user,
                                campsites = campsites,
                                onUserChange = { user = it },
                                onExit = { showAccount = false }
                            )
                        }
                        else if (showPolicy) {
                            PolicyScreen(
                                onBack = { showPolicy = false }
                            )
                        }

                        // Entrant booking is separate from the team's QR and profile screens.
                        else if (showEntrantBooking) {
                            EntrantBookingScreen(
                                controller = bookingController,
                                guest = bookingGuest,
                                onBack = { showEntrantBooking = false }
                            )
                        }

                        // made else if
                        else if (showAvailability) {

                            AvailabilityScreen(
                                schedule = reservationStore.localSchedule(bookingSite.campsiteID)
                                    ?: AvailabilitySchedule(),
                                onSave = {
                                    reservationStore.saveAvailabilitySchedule(bookingSite.campsiteID, it)
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
                                lotteryRepository = lotteryRepository,
                                onOpenOwnerCampsites = {
                                    currentQRScreen = "list"
                                },
                                onOpenQRScanner = {
                                    currentQRScreen = "scanner"
                                },
                                onOpenAccount = {
                                    showAccount = true
                                },
                                onOpenEntrantBooking = {
                                    showEntrantBooking = true
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
}

/**
 * Original team test menu.
 * Includes temporary navigation to QR features and the entrant booking demo.
 */
@Composable
fun MainMenuManager(
    onBack: () -> Unit,
    lotteryRepository: LotteryRepository,
    onOpenOwnerCampsites: () -> Unit = {},
    onOpenQRScanner: () -> Unit = {},
    onOpenAccount: () -> Unit = {},  // new
    onOpenEntrantBooking: () -> Unit = {}
) {

    var showLottery by remember {
        mutableStateOf(false)
    }

    var showImageUpload by remember {
        mutableStateOf(false)
    }

    //Notification Test
    val user = remember {
        User(
            id = "Testuser123",
            name = "Test user",
            email = "testDevice123",
            phone = "",
            roles = setOf(Role.ENTRANT),
            profileImage = "",
            notificationsEnabled = true
        )
    }
    val context = LocalContext.current
    val notificationController = remember(user.id){
        NotificationController(user, context)
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

    //fake campsites for testing Lottery firebase
    val fakeCampsite =  Campsite(
                campsiteID = "SAMPLE_SPRUCE_RIDGE",
                ownerID = "sample-owner",
                location = "Jasper area, Alberta",
                description = "The sample campsite used by the availability calendar.",
                rules = "Keep the site clean. Quiet hours are 10 pm to 7 am."
    )

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
            entrants = lotteryTestArray2,
            campsite = fakeCampsite,
            lotteryRepository = lotteryRepository
        )

    } else if (showImageUpload) {

        ImageUploadTestScreen(
            onBack = { showImageUpload = false },
            notificationController = notificationController,
            user = user
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

            // Open only our local winning-opportunity prototype.
            Button(onClick = onOpenEntrantBooking) {
                Text("Entrant booking demo")
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

