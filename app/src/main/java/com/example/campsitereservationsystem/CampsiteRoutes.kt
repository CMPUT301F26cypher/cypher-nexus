// Opening the owner and QR screens. The entrant’s waiting-list action is still a placeholder.

package com.example.campsitereservationsystem

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.campsitereservationsystem.campsite.AddCampsiteScreen
import com.example.campsitereservationsystem.campsite.Campsite
import com.example.campsitereservationsystem.campsite.CampsiteController
import com.example.campsitereservationsystem.campsite.CampsiteRepository
import com.example.campsitereservationsystem.campsite.OwnerCampsiteDetailsScreen
import com.example.campsitereservationsystem.campsite.OwnerCampsitesScreen
import com.example.campsitereservationsystem.qr.EntrantCampsiteDetailsScreen
import com.example.campsitereservationsystem.qr.QRCodeManager
import com.example.campsitereservationsystem.qr.QRCodeScannerScreen
import com.example.campsitereservationsystem.qr.QRCodeScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

internal const val OWNER_CAMPSITES_ROUTE = "my_campsites"
internal const val SCAN_CAMPSITE_ROUTE = "scan_campsite"

private const val ADD_CAMPSITE_ROUTE = "add_campsite"
private const val EDIT_CAMPSITE_ROUTE = "edit_campsite"
private const val OWNER_DETAILS_ROUTE = "owner_details"
private const val SHOW_QR_ROUTE = "show_qr"
private const val ENTRANT_DETAILS_ROUTE = "entrant_details"
private const val CAMPSITE_ID = "campsiteId"

// Declaring the campsite ID as a route argument so a restored page can identify which campsite it is meant to show.
private val campsiteArguments = listOf(navArgument(CAMPSITE_ID) { type = NavType.StringType })

/**
 * Registering the existing campsite and QR screens on the same stack as Welcome.
 * Keeping the owner and QR flows implemented by jmrojas17 in e8087bc.
 */
internal fun NavGraphBuilder.campsiteRoutes(
    navController: NavHostController,
    campsiteRepository: CampsiteRepository,
    campsiteController: com.example.campsitereservationsystem.campsite.CampsiteController,
    reservationRepository: ReservationRepository,
    ownerId: String,
    ownerSites: MutableList<Campsite>
) {
    // Using the same repository for the controller and route lookups.
    val campsiteController = CampsiteController(campsiteRepository, reservationRepository)
    // Holding the last checked scan alongside its ID.
    //var lastScan: Pair<String, OwnerCampsite>? = null

    var lastScan: Pair<String, Campsite>? = null

    // Reusing a campsite already loaded for the owner list.
    fun knownOwnerSite(page: NavBackStackEntry): Campsite? {
        val campsiteId = page.arguments?.getString(CAMPSITE_ID)
        // Looking for the owner’s record in the list already loaded.
        return ownerSites.firstOrNull { it.id == campsiteId }
    }

    composable(OWNER_CAMPSITES_ROUTE) { page ->
        // Observing whether the owner list is at the top of the stack.
        val currentPage by navController.currentBackStackEntryAsState()
        OwnerListPage(
            campsiteRepository = campsiteRepository,
            campsiteController = campsiteController,
            ownerId = ownerId,
            ownerSites = ownerSites,
            showingList = currentPage == page,
            // Opening a blank form for Add and passing an ID for existing campsite actions.
            onAdd = { navController.openPage(page, ADD_CAMPSITE_ROUTE) },
            onView = { site -> navController.openPage(page, siteRoute(OWNER_DETAILS_ROUTE, site.id)) },
            onEdit = { site -> navController.openPage(page, siteRoute(EDIT_CAMPSITE_ROUTE, site.id)) }
        )
    }

    // Opening the existing form without a campsite so it starts with empty fields.
    composable(ADD_CAMPSITE_ROUTE) { page ->
        // Sharing the list with the form so a saved campsite can be reflected immediately when the owner returns.
        CampsiteFormPage(campsiteController, ownerId, ownerSites, onBack = { navController.closePage(page) })
    }

    // Opening the same form with the requested campsite.
    composable("$EDIT_CAMPSITE_ROUTE/{$CAMPSITE_ID}", arguments = campsiteArguments) { page ->
        val goBack = { navController.closePage(page) }
        CampsitePage(page, campsiteRepository, knownOwnerSite(page), goBack) { site ->
            CampsiteFormPage(campsiteController, ownerId, ownerSites, site, goBack)
        }
    }

    composable("$OWNER_DETAILS_ROUTE/{$CAMPSITE_ID}", arguments = campsiteArguments) { page ->
        val goBack = { navController.closePage(page) }
        CampsitePage(page, campsiteRepository, knownOwnerSite(page), goBack) { site ->
            OwnerCampsiteDetailsScreen(
                campsite = site,
                // Starting the QR route from this specific details entry so Back can show the same campsite again.
                onViewQRCode = { navController.openPage(page, siteRoute(SHOW_QR_ROUTE, site.id)) },
                onBack = goBack
            )
        }
    }

    // Using the existing QR screen for the campsite on this route.
    composable("$SHOW_QR_ROUTE/{$CAMPSITE_ID}", arguments = campsiteArguments) { page ->
        val goBack = { navController.closePage(page) }
        CampsitePage(page, campsiteRepository, knownOwnerSite(page), goBack) { site ->
            QRCodeScreen(campsiteId = site.id, campsiteName = site.name, onBack = goBack)
        }
    }

    // Using one scanner destination for Welcome and the test menu.
    composable(SCAN_CAMPSITE_ROUTE) { page ->
        val context = LocalContext.current
        // Keeping the lookup in the scanner’s scope so it can stop when the screen is closed.
        val scope = rememberCoroutineScope()
        val qrReader = remember { QRCodeManager() }
        val goBack = { navController.closePage(page) }
        var checkingCode by remember { mutableStateOf(false) }

        QRCodeScannerScreen(
            // Handling scan results in the route while the scanner screen continues to own the camera interaction.
            onQRCodeScanned = scanned@{ code ->
                // Ignoring a result that arrives after leaving the scanner or while another lookup is running.
                if (navController.currentBackStackEntry != page || checkingCode) return@scanned
                // Reading the campsite ID with the team’s QR decoder.
                val campsiteId = qrReader.decodeQRCode(code)
                if (campsiteId == null) {
                    context.showCampsiteMessage("Invalid QR code")
                    return@scanned
                }

                // Checking that the scanned campsite exists before leaving the scanner.
                // Marking the lookup as active before another scan can start.
                checkingCode = true
                scope.launch {
                    try {
                        // Looking up the scanned campsite before moving forward.
                        val site = campsiteRepository.getById(campsiteId)
                        if (navController.currentBackStackEntry != page) return@launch
                        if (site == null) {
                            context.showCampsiteMessage("Campsite not found")
                            return@launch
                        }

                        // Carrying the checked record into the details screen.
                        // Passing the encoded ID through the route as well so a restored page can reload the campsite.
                        lastScan = campsiteId to site
                        navController.openPage(page, siteRoute(ENTRANT_DETAILS_ROUTE, campsiteId))
                    } catch (cancelled: CancellationException) {
                        // Letting cancellation leave this coroutine instead of showing it as a lookup error.
                        throw cancelled
                    } catch (error: Exception) {
                        if (navController.currentBackStackEntry == page) {
                            context.showCampsiteMessage(error.message ?: "Error loading campsite", long = true)
                        }
                    } finally {
                        // Clearing the lookup flag so the next scan can be handled.
                        checkingCode = false
                    }
                }
            },
            onBack = goBack
        )
    }

    composable("$ENTRANT_DETAILS_ROUTE/{$CAMPSITE_ID}", arguments = campsiteArguments) { page ->
        val goBack = { navController.closePage(page) }
        val campsiteId = page.arguments?.getString(CAMPSITE_ID)
        // Reusing the scan result only when its key matches the entrant details route.
        val knownSite = lastScan?.takeIf { it.first == campsiteId }?.second
        CampsitePage(page, campsiteRepository, knownSite, goBack) { site ->
            EntrantCampsiteDetailsScreen(
                campsite = site,
                // Keeping the existing demo behavior until the waiting list is connected.
                isLotteryActive = true,
                // Keeping the existing placeholder for the waiting list button.
                onJoinWaitingList = { println("Join Waiting List clicked") },
                onBack = goBack
            )
        }
    }
}

/** Reloading the owner list when returning from the campsite form. */
@Composable
private fun OwnerListPage(
    campsiteRepository: CampsiteRepository,
    campsiteController: CampsiteController,
    ownerId: String,
    ownerSites: MutableList<Campsite>,
    showingList: Boolean,
    onAdd: () -> Unit,
    onView: (Campsite) -> Unit,
    onEdit: (Campsite) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Tracking the owner’s delete request until it finishes so a second request cannot start in the same list screen.
    var deletingSite by remember { mutableStateOf(false) }

    // Refreshing the owner list when its identity changes or it returns to the foreground.
    LaunchedEffect(ownerId, showingList) {
        if (!showingList) return@LaunchedEffect
        try {
            reloadOwnerSites(campsiteRepository, ownerId, ownerSites)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            context.showCampsiteMessage(error.message ?: "Error loading campsites", long = true)
        }
    }

    OwnerCampsitesScreen(
        campsites = ownerSites,
        onCampsiteClick = onView,
        onAddCampsite = onAdd,
        onEditCampsite = onEdit,
        // Checking ownership before sending a delete request.
        onDeleteCampsite = delete@{ site ->
            if (site.ownerId != ownerId) {
                context.showCampsiteMessage("You cannot delete this campsite")
                return@delete
            }
            if (deletingSite) return@delete

            // Refreshing the list after Firebase confirms the deletion.
            deletingSite = true
            scope.launch {
                try {
                    // Waiting for the existing delete operation before removing the row.
                    campsiteController.deleteCampsite(site.id)
                    ownerSites.removeAll { it.id == site.id }
                    reloadOwnerSites(campsiteRepository, ownerId, ownerSites)
                    context.showCampsiteMessage("Campsite deleted successfully!")
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    // Showing the reported error when the delete flow fails, with a general message as a fallback.
                    context.showCampsiteMessage(error.message ?: "Failed to delete campsite", long = true)
                } finally {
                    deletingSite = false
                }
            }
        }
    )
}

private suspend fun reloadOwnerSites(
    campsiteRepository: CampsiteRepository,
    ownerId: String,
    ownerSites: MutableList<Campsite>
) {
    // Replacing the contents only after the read succeeds.
    val freshSites = campsiteRepository.getByOwner(ownerId)
    ownerSites.clear()
    ownerSites.addAll(freshSites)
}

/** Sharing the existing form between adding and editing a campsite. */
@Composable
private fun CampsiteFormPage(
    campsiteController: CampsiteController,
    ownerId: String,
    ownerSites: MutableList<Campsite>,
    originalSite: Campsite? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Tracking a save while the existing form stays visible.
    var savingSite by remember { mutableStateOf(false) }

    AddCampsiteScreen(
        campsite = originalSite,
        // Returning from the form without submitting its values when the owner cancels.
        onCancel = onBack,
        onSave = save@{ name, location, description, amenities, occupancy, latitude, longitude, tags ->
            if (savingSite) return@save
            // Checking the owner of an existing record before editing.
            if (originalSite != null && originalSite.ownerId != ownerId) {
                context.showCampsiteMessage("You cannot edit this campsite")
                return@save
            }

            // Keeping the original ID, owner, and image when editing existing details.
            // Turning form input into the campsite model while retaining fields already on the selected campsite.
            val blankOrExisting = originalSite ?: Campsite(ownerId = ownerId)
            val updatedSite = blankOrExisting.copy(
                name = name.trim(),
                location = location.trim(),
                description = description.trim(),
                // Converting comma-separated form entries into lists.
                amenities = amenities.commaSeparatedItems(),
                maxOccupancy = occupancy.toIntOrNull() ?: 0,
                latitude = latitude.toDoubleOrNull(),
                longitude = longitude.toDoubleOrNull(),
                tags = tags.commaSeparatedItems()
            )

            // Marking the form busy before launching the request.
            savingSite = true
            scope.launch {
                try {
                    // Registering a new record when the form opened empty.
                    if (originalSite == null) {
                        val newId = campsiteController.registerCampsite(updatedSite)
                        ownerSites.add(updatedSite.copy(id = newId))
                        context.showCampsiteMessage("Campsite saved successfully!")
                    } else {
                        // Saving an edit under the existing campsite ID.
                        campsiteController.updateCampsite(updatedSite)
                        val row = ownerSites.indexOfFirst { it.id == updatedSite.id }
                        if (row >= 0) ownerSites[row] = updatedSite
                        context.showCampsiteMessage("Campsite updated successfully!")
                    }
                    // Closing the form after a successful save.
                    onBack()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    // Keeping the form open after an error and reporting whether registration or editing was attempted.
                    val fallback = if (originalSite == null) "Failed to save campsite" else "Failed to update campsite"
                    context.showCampsiteMessage(error.message ?: fallback, long = true)
                } finally {
                    savingSite = false
                }
            }
        }
    )
}

/** Loading a campsite from its route ID, including after the app recreates the screen. */
@Composable
private fun CampsitePage(
    page: NavBackStackEntry,
    campsiteRepository: CampsiteRepository,
    knownSite: Campsite?,
    onBack: () -> Unit,
    content: @Composable (Campsite) -> Unit
) {
    val campsiteId = page.arguments?.getString(CAMPSITE_ID)
    // Keeping the loaded record and message tied to the route ID.
    //var loadedSite by remember(campsiteId) { mutableStateOf<OwnerCampsite?>(null) }
    var loadedSite by remember(campsiteId) { mutableStateOf<Campsite?>(null) }
    var message by remember(campsiteId) { mutableStateOf("Loading campsite…") }

    // Loading from the repository only when the owner list or last scan has not supplied the record.
    LaunchedEffect(campsiteId, knownSite) {
        if (knownSite != null) return@LaunchedEffect
        // Showing the missing record state immediately when the route does not contain a usable ID.
        if (campsiteId.isNullOrBlank()) {
            message = "Campsite not found"
            return@LaunchedEffect
        }
        try {
            // Filling in a restored destination with the stored campsite.
            loadedSite = campsiteRepository.getById(campsiteId)
            if (loadedSite == null) message = "Campsite not found"
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            message = error.message ?: "Error loading campsite"
        }
    }

    // Using the already-known campsite when possible, and the fetched record otherwise.
    val site = knownSite ?: loadedSite
    // Keeping Back available while loading or after a lookup error.
    if (site == null) {
        // Giving a restored destination a way back while its campsite is unavailable.
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
            TextButton(onClick = onBack) { Text("Back") }
            Text(message)
        }
    } else {
        content(site)
    }
}

// Encoding the ID so it stays one route segment even when a QR code contains punctuation.
private fun siteRoute(route: String, campsiteId: String) = "$route/${Uri.encode(campsiteId)}"

// Checking the current entry before following callbacks from an outgoing screen.
internal fun NavHostController.openPage(page: NavBackStackEntry, route: String) {
    if (currentBackStackEntry == page) {
        // Opening the destination only from the entry that started the action.
        navigate(route) { launchSingleTop = true }
    }
}

internal fun NavHostController.closePage(page: NavBackStackEntry) {
    if (currentBackStackEntry == page) {
        // Removing just the current entry so Back returns to the actual caller.
        popBackStack()
    }
}

private fun String.commaSeparatedItems(): List<String> {
    // Trimming list entries and dropping blanks caused by extra commas.
    return split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

private fun Context.showCampsiteMessage(message: String, long: Boolean = false) {
    val duration = if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, message, duration).show()
}
