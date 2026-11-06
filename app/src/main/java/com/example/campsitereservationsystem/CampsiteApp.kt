package com.example.campsitereservationsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.campsitereservationsystem.campsite.CampsiteRepository
import com.example.campsitereservationsystem.campsite.Campsite as OwnerCampsite

private const val WELCOME = "welcome"
private const val POLICY = "policy"
private const val TEST_MENU = "test_menu"
private const val AVAILABILITY = "availability"
private const val BOOKING = "booking"
private const val LOTTERY = "lottery"
private const val IMAGE_TEST = "image_test"
private const val ACCOUNT = "account"

/** Connecting the team's existing screens with NavHost pattern.(In lab-7 format) */
@Composable
fun CampsiteApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val deviceId = remember(context) { DeviceId.get(context) }
    val campsiteRepository = remember { CampsiteRepository() }
    val ownerSites = remember { mutableStateListOf<OwnerCampsite>() }
    var signedInCamper by remember { mutableStateOf<User?>(null) }

    // Reusing sample campsite for the availability and booking demos.
    val sampleCamp = remember {
        Campsite(
            campsiteID = "SAMPLE_SPRUCE_RIDGE",
            ownerID = "sample-owner",
            location = "Jasper area, Alberta",
            description = "The sample campsite used by the availability calendar.",
            rules = "Keep the site clean. Quiet hours are 10 pm to 7 am."
        )
    }
    // Sharing the same sample dates across both screens as the user moves around.
    val demoNights = remember {
        ReservationRepository(localOnly = true).apply {
            saveAvailabilitySchedule(sampleCamp.campsiteID, AvailabilitySchedule())
        }
    }
    val deviceCamper = remember(deviceId) { User(id = deviceId) }
    val currentUser = signedInCamper ?: deviceCamper //used for lotteryScreen testing
    val bookingCamper by rememberUpdatedState(signedInCamper ?: deviceCamper)
    val bookingController = remember(bookingCamper.id) {
        ReservationController(
            campsite = sampleCamp,
            campsiteName = "Spruce Ridge",
            cancellationPolicy = "Contact the owner about changes. Cancellation is not available in this prototype.",
            winnerId = bookingCamper.id,
            repository = demoNights,
            isWinner = true
        )
    }
    val currentBookingController by rememberUpdatedState(bookingController)
    val lotteryRepository = remember { LotteryRepository() }

    // Keeping the same button destinations that the team wired up in MainActivity.
    NavHost(navController = navController, startDestination = WELCOME) {
        composable(WELCOME) { page ->
            // Opening Welcome screen and its original four actions.
            WelcomeScreen(
                onFindCampsite = { navController.openPage(page, SCAN_CAMPSITE_ROUTE) },
                onManageCampsites = { navController.openPage(page, AVAILABILITY) },
                onPolicyClick = { navController.openPage(page, POLICY) },
                onOpenTestMenu = { navController.openPage(page, TEST_MENU) }
            )
        }
        composable(POLICY) { page ->
            // Showing efarr's existing policy screen (0af9fa9).
            PolicyScreen(onBack = { navController.closePage(page) })
        }
        composable(TEST_MENU) { page ->
            TestMenu(
                onAccount = { navController.openPage(page, ACCOUNT) },
                onLottery = { navController.openPage(page, LOTTERY) },
                onImages = { navController.openPage(page, IMAGE_TEST) },
                onOwnerCampsites = { navController.openPage(page, OWNER_CAMPSITES_ROUTE) },
                onScan = { navController.openPage(page, SCAN_CAMPSITE_ROUTE) },
                onBooking = { navController.openPage(page, BOOKING) },
                onBack = { navController.closePage(page) }
            )
        }

        // Registering jmrojas17's owner campsite and QR screens (e8087bc).
        campsiteRoutes(navController, campsiteRepository, deviceId, ownerSites)

        composable(AVAILABILITY) { page ->
            // Opening Arpit's existing calendar (9b13916).
            AvailabilityScreen(
                schedule = demoNights.localSchedule(sampleCamp.campsiteID) ?: AvailabilitySchedule(),
                onSave = { schedule ->
                    demoNights.saveAvailabilitySchedule(sampleCamp.campsiteID, schedule)
                },
                onBack = { navController.closePage(page) }
            )
        }
        composable(BOOKING) { page ->
            // Opening Arpit's entrant booking demo with the shared dates (9df1603, 9f24e48).
            EntrantBookingScreen(
                controller = currentBookingController,
                guest = bookingCamper,
                onBack = { navController.closePage(page) }
            )
        }
        composable(LOTTERY) { page ->
            // Passing the original test entrants to aboogabingbong's lottery screen (83f3b25).
            LotteryScreen(
                onBack = { navController.closePage(page) },
                campsite = sampleCamp,
                lotteryRepository = lotteryRepository,
                currentUser
            )
        }
        composable(IMAGE_TEST) { page ->
            ImageTestRoute(onBack = { navController.closePage(page) })
        }
        composable(ACCOUNT) { page ->
            // Keeping efarr's existing account and profile flow inside AccountTest (0af9fa9).
            AccountTest(
                user = signedInCamper,
                campsites = ownerSites,
                onUserChange = { signedInCamper = it },
                onExit = { navController.closePage(page) }
            )
        }
    }
}

/** Showing the same buttons as the team's original test menu. */
@Composable
private fun TestMenu(
    onAccount: () -> Unit,
    onLottery: () -> Unit,
    onImages: () -> Unit,
    onOwnerCampsites: () -> Unit,
    onScan: () -> Unit,
    onBooking: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onAccount) { Text("Account") }
        Button(onClick = onLottery) { Text("Lottery WIP") }
        Button(onClick = onImages) { Text("Image Upload Test Screen") }
        Button(onClick = onOwnerCampsites) { Text("My Campsites") }
        Button(onClick = onScan) { Text("Scan QR Code") }
        Button(onClick = onBooking) { Text("Entrant booking demo") }
        Button(onClick = onBack) { Text("Back to welcome") }
    }
}

/** Reusing dcreyes1's image upload and notifications test (a2114fc, 5cb3df6). */
@Composable
private fun ImageTestRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    val demoCamper = remember {
        User(id = "Testuser123", name = "Test user", email = "testDevice123")
    }
    val notifications = remember(context, demoCamper.id) {
        NotificationController(demoCamper, context)
    }

    // Stopping the notification listener when the user leaves this test screen.
    DisposableEffect(notifications) {
        onDispose { notifications.stopNotifications() }
    }
    ImageUploadTestScreen(
        onBack = onBack,
        notificationController = notifications,
        user = demoCamper
    )
}
