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
import com.example.campsitereservationsystem.campsite.Campsite as OwnerCampsite
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

private val campsiteArguments = listOf(navArgument(CAMPSITE_ID) { type = NavType.StringType })

/**
 * Registering the existing campsite and QR screens on the same stack as Welcome.
 * Keeping the owner and QR flows implemented by jmrojas17 in e8087bc.
 */
internal fun NavGraphBuilder.campsiteRoutes(
    navController: NavHostController,
    campsiteRepository: CampsiteRepository,
    ownerId: String,
    ownerSites: MutableList<OwnerCampsite>
) {
    val campsiteController = CampsiteController(campsiteRepository)
    var lastScan: Pair<String, OwnerCampsite>? = null

    // Reusing a campsite already loaded for the owner list.
    fun knownOwnerSite(page: NavBackStackEntry): OwnerCampsite? {
        val campsiteId = page.arguments?.getString(CAMPSITE_ID)
        return ownerSites.firstOrNull { it.id == campsiteId }
    }

    composable(OWNER_CAMPSITES_ROUTE) { page ->
        val currentPage by navController.currentBackStackEntryAsState()
        OwnerListPage(
            campsiteRepository = campsiteRepository,
            campsiteController = campsiteController,
            ownerId = ownerId,
            ownerSites = ownerSites,
            showingList = currentPage == page,
            onAdd = { navController.openPage(page, ADD_CAMPSITE_ROUTE) },
            onView = { site -> navController.openPage(page, siteRoute(OWNER_DETAILS_ROUTE, site.id)) },
            onEdit = { site -> navController.openPage(page, siteRoute(EDIT_CAMPSITE_ROUTE, site.id)) }
        )
    }

    composable(ADD_CAMPSITE_ROUTE) { page ->
        CampsiteFormPage(campsiteController, ownerId, ownerSites, onBack = { navController.closePage(page) })
    }

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
                onViewQRCode = { navController.openPage(page, siteRoute(SHOW_QR_ROUTE, site.id)) },
                onBack = goBack
            )
        }
    }

    composable("$SHOW_QR_ROUTE/{$CAMPSITE_ID}", arguments = campsiteArguments) { page ->
        val goBack = { navController.closePage(page) }
        CampsitePage(page, campsiteRepository, knownOwnerSite(page), goBack) { site ->
            QRCodeScreen(campsiteId = site.id, campsiteName = site.name, onBack = goBack)
        }
    }

    composable(SCAN_CAMPSITE_ROUTE) { page ->
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val qrReader = remember { QRCodeManager() }
        val goBack = { navController.closePage(page) }
        var checkingCode by remember { mutableStateOf(false) }

        QRCodeScannerScreen(
            onQRCodeScanned = scanned@{ code ->
                if (navController.currentBackStackEntry != page || checkingCode) return@scanned
                val campsiteId = qrReader.decodeQRCode(code)
                if (campsiteId == null) {
                    context.showCampsiteMessage("Invalid QR code")
                    return@scanned
                }

                // Checking that the scanned campsite exists before leaving the scanner.
                checkingCode = true
                scope.launch {
                    try {
                        val site = campsiteRepository.getById(campsiteId)
                        if (navController.currentBackStackEntry != page) return@launch
                        if (site == null) {
                            context.showCampsiteMessage("Campsite not found")
                            return@launch
                        }

                        // Carrying the checked record into the details screen.
                        lastScan = campsiteId to site
                        navController.openPage(page, siteRoute(ENTRANT_DETAILS_ROUTE, campsiteId))
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        if (navController.currentBackStackEntry == page) {
                            context.showCampsiteMessage(error.message ?: "Error loading campsite", long = true)
                        }
                    } finally {
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
        val knownSite = lastScan?.takeIf { it.first == campsiteId }?.second
        CampsitePage(page, campsiteRepository, knownSite, goBack) { site ->
            EntrantCampsiteDetailsScreen(
                campsite = site,
                // Keeping the existing demo behavior until the waiting list is connected.
                isLotteryActive = true,
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
    ownerSites: MutableList<OwnerCampsite>,
    showingList: Boolean,
    onAdd: () -> Unit,
    onView: (OwnerCampsite) -> Unit,
    onEdit: (OwnerCampsite) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var deletingSite by remember { mutableStateOf(false) }

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
                    campsiteController.deleteCampsite(site.id)
                    ownerSites.removeAll { it.id == site.id }
                    reloadOwnerSites(campsiteRepository, ownerId, ownerSites)
                    context.showCampsiteMessage("Campsite deleted successfully!")
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
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
    ownerSites: MutableList<OwnerCampsite>
) {
    val freshSites = campsiteRepository.getByOwner(ownerId)
    ownerSites.clear()
    ownerSites.addAll(freshSites)
}

/** Sharing the existing form between adding and editing a campsite. */
@Composable
private fun CampsiteFormPage(
    campsiteController: CampsiteController,
    ownerId: String,
    ownerSites: MutableList<OwnerCampsite>,
    originalSite: OwnerCampsite? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var savingSite by remember { mutableStateOf(false) }

    AddCampsiteScreen(
        campsite = originalSite,
        onCancel = onBack,
        onSave = save@{ name, location, description, amenities, occupancy, latitude, longitude, tags ->
            if (savingSite) return@save
            if (originalSite != null && originalSite.ownerId != ownerId) {
                context.showCampsiteMessage("You cannot edit this campsite")
                return@save
            }

            // Keeping the original ID, owner, and image when editing existing details.
            val blankOrExisting = originalSite ?: OwnerCampsite(ownerId = ownerId)
            val updatedSite = blankOrExisting.copy(
                name = name.trim(),
                location = location.trim(),
                description = description.trim(),
                amenities = amenities.commaSeparatedItems(),
                maxOccupancy = occupancy.toIntOrNull() ?: 0,
                latitude = latitude.toDoubleOrNull(),
                longitude = longitude.toDoubleOrNull(),
                tags = tags.commaSeparatedItems()
            )

            savingSite = true
            scope.launch {
                try {
                    if (originalSite == null) {
                        val newId = campsiteController.registerCampsite(updatedSite)
                        ownerSites.add(updatedSite.copy(id = newId))
                        context.showCampsiteMessage("Campsite saved successfully!")
                    } else {
                        campsiteController.updateCampsite(updatedSite)
                        val row = ownerSites.indexOfFirst { it.id == updatedSite.id }
                        if (row >= 0) ownerSites[row] = updatedSite
                        context.showCampsiteMessage("Campsite updated successfully!")
                    }
                    onBack()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
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
    knownSite: OwnerCampsite?,
    onBack: () -> Unit,
    content: @Composable (OwnerCampsite) -> Unit
) {
    val campsiteId = page.arguments?.getString(CAMPSITE_ID)
    var loadedSite by remember(campsiteId) { mutableStateOf<OwnerCampsite?>(null) }
    var message by remember(campsiteId) { mutableStateOf("Loading campsite…") }

    LaunchedEffect(campsiteId, knownSite) {
        if (knownSite != null) return@LaunchedEffect
        if (campsiteId.isNullOrBlank()) {
            message = "Campsite not found"
            return@LaunchedEffect
        }
        try {
            loadedSite = campsiteRepository.getById(campsiteId)
            if (loadedSite == null) message = "Campsite not found"
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            message = error.message ?: "Error loading campsite"
        }
    }

    val site = knownSite ?: loadedSite
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
        navigate(route) { launchSingleTop = true }
    }
}

internal fun NavHostController.closePage(page: NavBackStackEntry) {
    if (currentBackStackEntry == page) {
        popBackStack()
    }
}

private fun String.commaSeparatedItems(): List<String> {
    return split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

private fun Context.showCampsiteMessage(message: String, long: Boolean = false) {
    val duration = if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, message, duration).show()
}
