package com.example.campsitereservationsystem.qr


import androidx.compose.ui.graphics.Color
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.DisposableEffect
import com.example.campsitereservationsystem.campsite.CampsiteRepository
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.ui.text.font.FontWeight


@Composable
fun QRCodeScannerScreen(
    onQRCodeScanned: (String) ->Unit,
    onBack:() ->Unit
) {
    val context = LocalContext.current
    val scanner = remember(context) {
        GmsBarcodeScanning.getClient(context)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }
    //the filter to search for the campsites
    val repository = remember { CampsiteRepository() }

    var searchQuery by remember {
        mutableStateOf("")
    }

    DisposableEffect(repository) {
        val listener = repository.watchCampsites()

        onDispose {
            listener.remove()
        }
    }

    val filteredCampsites = repository.campsites.filter { campsite ->
        searchQuery.isNotBlank() &&
                campsite.name.contains(
                    searchQuery.trim(),
                    ignoreCase = true
                )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Button(onClick = onBack) {
            Text("← Back")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Find a Campsite"
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search campsites by name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredCampsites, key = { it.id }) { campsite ->
                Card(
                    onClick = {
                        // TODO: Navigate to campsite details
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = campsite.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Location: ${campsite.location}"
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Maximum occupancy: ${campsite.maxOccupancy} people"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(5) {
                                Icon(
                                    imageVector = Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Not rated yet",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Scan QR Code")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                errorMessage = null
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        val content = barcode.rawValue

                        if (content != null) {
                            onQRCodeScanned(content)
                        } else {
                            errorMessage = "QR code is empty"
                        }
                    }
                    .addOnFailureListener {
                        errorMessage = "Unable to scan QR code"
                    }
            }
        ) {
            Text("Open QR Scanner")
        }

        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message)
        }
    }
}