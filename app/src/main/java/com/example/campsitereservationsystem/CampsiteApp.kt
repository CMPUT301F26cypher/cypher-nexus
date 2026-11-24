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
import com.example.campsitereservationsystem.campsite.Campsite
import com.example.campsitereservationsystem.campsite.CampsiteController
import kotlin.collections.emptyList
import com.example.campsitereservationsystem.campsite.Campsite as OwnerCampsite
import com.example.campsitereservationsystem.review.ReviewRepository

private const val WELCOME = "welcome"
private const val POLICY = "policy"
private const val TEST_MENU = "test_menu"
private const val AVAILABILITY = "availability"
private const val BOOKING = "booking"
private const val LOTTERY = "lottery"
private const val IMAGE_TEST = "image_test"
private const val ACCOUNT = "account"
private const val OWNER_WAITLIST = "owner_waitlist"
private const val BROWSE_CAMPSITES = "browse_campsites"

//********HOME SCREENS WHERE USER GOES AFTER LOGIN*********
private const val ENTRANT_SCREEN = "entrant_screen"

private const val OWNER_SCREEN = "owner_screen"

private const val ADMIN_SCREEN = "admin_screen"

//******************ENTRANT HOME SCREEN********************
private const val ENTRANT_CAMPSITES = "entrant_campsites"

private const val ENTRANT_LOTTERIES = "entrant_lotteries"

private const val ENTRANT_BOOKING = "entrant_booking"

private const val ENTRANT_RESERVATIONS = "entrant_reservations"

private const val ENTRANT_QRSCAN= "entrant_qrscan"

private const val ENTRANT_PROFILE= "entrant_profile"

//********************OWNER HOME SCREEN********************

private const val OWNER_CAMPSITES = "owner_campsites"

private const val OWNER_LOTTERIES = "owner_lotteries"

private const val OWNER_RESERVATIONS = "owner_reservations"

private const val OWNER_NOTIFICATIONS = "owner_notifications"


/** Connecting the team's existing screens with NavHost pattern.(In lab-7 format) */
@Composable
fun CampsiteApp() {
    // Keeping one controller for the app’s back stack.
    val navController = rememberNavController()
    val context = LocalContext.current
    // Using the existing device ID for the owner list and the demo guest.
    val deviceId = remember(context) { DeviceId.get(context) }
    // Passing this repository to the campsite routes.

    var signedInCamper by remember { mutableStateOf<User?>(null) }


    // REPOSITORIES
    val campsiteRepository = remember { CampsiteRepository() }
    val recentlyViewedIds = remember {
        mutableStateListOf<String>()
    }
    val reservationRepository = remember { ReservationRepository() }
    var userRepository = remember {
        UserRepository(onUserChanged = { updated: User? -> signedInCamper = updated })
    }
    val reviewRepository = remember { ReviewRepository() }
    val imageRepository = remember { ImageRepository() }    // NEW: used for adding profile image US 01.08.02


    // Cleaning up the user listener when CampsiteApp leaves composition.
    DisposableEffect(userRepository) {
        onDispose {
            userRepository.stopListening()
        }
    }


    val ownerSites = remember { mutableStateListOf<OwnerCampsite>() }
    // Keeping the account state above the routes so a change from the account screen can be read by the booking demo.
    val campsiteController = remember(campsiteRepository) { CampsiteController(repository = campsiteRepository, reservationRepository = ReservationRepository()) }
    //val ownerSites = remember { mutableStateListOf<Campsite>() }

    // Reusing sample campsite for the availability and booking demos.
    val sampleCamp = remember {
        Campsite(
            id = "SAMPLE_SPRUCE_RIDGE",
            name = "Spruce Ridge",
            ownerId = "sample-owner",
            location = "Jasper area, Alberta",
            description = "The sample campsite used by the availability calendar.",
            maxOccupancy = 4,
            rules = "Keep the site clean. Quiet hours are 10 pm to 7 am."
        )
    }
    // Sharing the same sample dates across both screens as the user moves around.
    val demoNights = remember {
        ReservationRepository(localOnly = true).apply {
            saveAvailabilitySchedule(sampleCamp.id, AvailabilitySchedule())
        }
    }
    val deviceCamper = remember(deviceId) { User(id = deviceId) }
    // Using the account profile when one exists and the device profile otherwise.
    val currentUser = signedInCamper ?: deviceCamper //used for lotteryScreen testing
    val bookingCamper by rememberUpdatedState(signedInCamper ?: deviceCamper)
    // Keeping the same controller while the guest stays the same.
    val bookingController = remember(bookingCamper.id) {
        ReservationController(
            campsite = sampleCamp,
            campsiteName = "Spruce Ridge",
            cancellationPolicy = "Contact the owner about changes. Cancellation is not available in this prototype.",
            winnerId = bookingCamper.id,
            repository = demoNights,
            // Supplying the sample winning opportunity for the demo.
            isWinner = true
        )
    }
    // Making the current controller available to the registered route after a change of guest.
    val currentBookingController by rememberUpdatedState(bookingController)
    val lotteryRepository = remember { LotteryRepository() }

    // Keeping the same button destinations that the team wired up in MainActivity.
    // Starting at Welcome and registering the other screens on the same stack.
    NavHost(navController = navController, startDestination = WELCOME) {
        composable(WELCOME) { page ->
            // Opening Welcome screen and its original four actions.
            // Keeping the original Welcome actions.
            WelcomeScreen(
                onFindCampsite = { navController.openPage(page, SCAN_CAMPSITE_ROUTE) },
                onManageCampsites = { navController.openPage(page, AVAILABILITY) },
                onPolicyClick = { navController.openPage(page, POLICY) },
                onOpenTestMenu = { navController.openPage(page, TEST_MENU) },
                onBrowseCampsites = { navController.openPage(page, BROWSE_CAMPSITES) }
            )
        }
        //Entrants can browse and filter camps
        composable(BROWSE_CAMPSITES) { page ->
            DisposableEffect(campsiteRepository) {
                val listener = campsiteRepository.watchCampsites()

                onDispose {
                    listener.remove()
                }
            }

            CampsiteListScreen(
                controller = campsiteController,
                onBack = {
                    navController.closePage(page)
                }
            )
        }

        composable(POLICY) { page ->
            // Showing efarr's existing policy screen (0af9fa9).
            PolicyScreen(onBack = { navController.closePage(page) })
        }
        composable(TEST_MENU) { page ->
            // Connecting the old test menu buttons to routes.
            TestMenu(
                onAccount = { navController.openPage(page, ACCOUNT) },
                onLottery = { navController.openPage(page, LOTTERY) },
                onImages = { navController.openPage(page, IMAGE_TEST) },
                onOwnerCampsites = { navController.openPage(page, OWNER_CAMPSITES_ROUTE) },
                onScan = { navController.openPage(page, SCAN_CAMPSITE_ROUTE) },
                onBooking = { navController.openPage(page, BOOKING) },
                // TEMPORARY (US 02.04.03, 02.04.04): opens Lottery Details until My Lotteries exists.
                onLotteryDetails = { navController.openPage(page, TEMP_PICK_FOR_LOTTERY_ROUTE) },
                onBack = { navController.closePage(page) }
            )
        }

        // Registering jmrojas17's owner campsite and QR screens (e8087bc).

        campsiteRoutes(
            navController = navController,
            campsiteRepository = campsiteRepository,
            campsiteController = campsiteController,
            reservationRepository,
            ownerId = deviceId,
            ownerSites = ownerSites,
            recentlyViewedIds = recentlyViewedIds,
            reviewRepository = reviewRepository,
            currentUser = currentUser
        )

        //*****************************ENTRANT_SCREEN AND SUBSEQUENT PAGES************************
        composable(ENTRANT_SCREEN) { page ->
            EntrantHomeScreen(
                onBrowseCampsites = { navController.openPage(page, ENTRANT_CAMPSITES) },
                onJoinedLotteries = { navController.openPage(page, ENTRANT_LOTTERIES) },
                onBooking = { navController.openPage(page, ENTRANT_BOOKING) },
                onEntrantReservations = { navController.openPage(page, ENTRANT_RESERVATIONS) },
                onQRScan = { navController.openPage(page, ENTRANT_QRSCAN) },
                onEntrantProfile = { navController.openPage(page, ENTRANT_PROFILE) }
            )
        }

        //Browse Campsite page
        composable(ENTRANT_CAMPSITES) { page ->
            EntrantCampsiteListScreen(
                onBack = {
                    navController.closePage(page)
                }
            )
        }

        //Joined Lotteries page
        composable(ENTRANT_LOTTERIES) { page ->
            JoinedLotteriesListScreen(
                onBack = {
                    navController.closePage(page)
                }
            )
        }

        //Booking page
        composable(ENTRANT_BOOKING) { page ->
            EntrantBookingListScreen(
                user = signedInCamper,
                onBack = {
                    navController.closePage(page)
                }
            )
        }

        //Entrant Reservations list page
        composable(ENTRANT_RESERVATIONS) { page ->
            EntrantReservationsListScreen(
                onBack = {
                    navController.closePage(page)
                }
            )
        }

        //QR scan page
        composable(ENTRANT_QRSCAN) { page ->
            EntrantQRScanListScreen(
                onBack = {
                    navController.closePage(page)
                }
            )
        }

        //Entrant Profile page
        composable(ENTRANT_PROFILE) { page ->
            EntrantProfileScreen(
                onBack = {
                    navController.closePage(page)
                }
            )
        }

        //*****************************OWNER_SCREEN AND SUBSEQUENT PAGES************************
        composable(OWNER_SCREEN) { page ->
            OwnerHomeScreen(
                onMyCampsites = { navController.openPage(page, OWNER_CAMPSITES) },
                onMyLotteries = { navController.openPage(page, OWNER_LOTTERIES) },
                onOwnerReservations = { navController.openPage(page, OWNER_RESERVATIONS) },
                onOwnerNotifications = { navController.openPage(page, OWNER_NOTIFICATIONS) }
            )
        }

        //Campsite Screen
        composable(OWNER_CAMPSITES) { page ->
            OwnerCampsiteListScreen(
                onBack = {
                    navController.closePage(page)
                }
            )
        }
        //Lotteries Screen
        composable(OWNER_LOTTERIES) { page ->
            OwnerLotteriesListScreen(
                onBack = {
                    navController.closePage(page)
                }
            )
        }
        //Reservations Screen
        composable(OWNER_RESERVATIONS) { page ->
            OwnerReservationsListScreen(
                onBack = {
                    navController.closePage(page)
                }
            )
        }
        //Notifications Screen
        composable(OWNER_NOTIFICATIONS) { page ->
            OwnerNotificationsListScreen(
                user = signedInCamper,
                onBack = {
                    navController.closePage(page)
                }
            )
        }

        //*****************************ADMIN_SCREEN AND SUBSEQUENT PAGES************************
        composable("admin_screen") {
            AdminHomeScreen()
        }

        composable(AVAILABILITY) { page ->
            // Opening Arpit's existing calendar (9b13916).
            AvailabilityScreen(
                schedule = demoNights.localSchedule(sampleCamp.id) ?: AvailabilitySchedule(),
                onSave = { schedule ->
                    demoNights.saveAvailabilitySchedule(sampleCamp.id, schedule)
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
                repo = userRepository,
                onUserChange = { signedInCamper = it },
                onExit = { navController.closePage(page) },

                onEntrantHome = {
                    navController.navigate(ENTRANT_SCREEN) {
                        popUpTo(ACCOUNT) { inclusive = true }
                    }
                },

                onOwnerHome = {
                    navController.navigate(OWNER_SCREEN) {
                        popUpTo(ACCOUNT) { inclusive = true }
                    }
                },

                onAdminHome = {
                    navController.navigate(ADMIN_SCREEN) {
                        popUpTo(ACCOUNT) { inclusive = true }
                    }
                }
            )
        }
        composable(OWNER_WAITLIST) { page ->
            val owner = signedInCamper
            if (owner != null && Role.OWNER in owner.roles) {
                // campsites can be owned by deivce ID or owner ID
                val myCampsites = ownerSites.filter { it.ownerId == owner.id || it.ownerId == owner.deviceId }
                OwnerWaitingListScreen(
                    campsites = myCampsites,
                    loadEntrants = {c, done ->
                        lotteryRepository.getWaitingList(
                            c.id,
                            done,
                            { done(emptyList())}
                        )
                    },
                    onBack = { navController.closePage(page) }
                )
            }
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
    onLotteryDetails: () -> Unit,
    onBack: () -> Unit
) {
    // Keeping the test menu in its original order with one callback per button.
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onAccount) { Text("Account") }
        Button(onClick = onLottery) { Text("Lottery WIP") }
        Button(onClick = onImages) { Text("Image Upload Test Screen") }
        // Keeping the real owner list separate from the sample availability button on Welcome.
        Button(onClick = onOwnerCampsites) { Text("My Campsites") }
        Button(onClick = onScan) { Text("Scan QR Code") }
        Button(onClick = onBooking) { Text("Entrant booking demo") }
        Button(onClick = onLotteryDetails) { Text("Lottery Details (TEMP)") }
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
