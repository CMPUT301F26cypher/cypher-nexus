
/*
 * QRCodeScannerScreen.kt
 *
 * This file is part of the View in our MVC architecture.
 * Here we allow the entrant to scan a campsite QR Code
 * so they can open its details.
 *
 * We use Google's barcode scanner to get the content
 * of the QR Code and pass it to the corresponding callback.
 *
 * Outstanding issues:
 * - For now, we only display a general error message
 *   if something goes wrong while scanning.
 */
// US 01.12.01 - View campsite details by scanning the promotional QR code.
package com.example.campsitereservationsystem.EntrantPath

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/**
 * This screen allows the entrant to scan a QR Code
 * and get its content.
 *
 * @param onQRCodeScanned Callback used to pass the content of the QR Code.
 * @param onBack Callback used to go back to the previous screen.
 */
@Composable
fun QRCodeScannerScreen(
    onQRCodeScanned: (String) -> Unit,
    onBack: () -> Unit
) {

    BackHandler(onBack = onBack)

    val green = Color(0xFF819F5C)

    val context = LocalContext.current

    // Here we create the scanner we are going to use
    // to scan the campsite QR Code
    val scanner = remember(context) {
        GmsBarcodeScanning.getClient(context)
    }

    // We use this to store the error if something goes wrong
    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {

        Text(
            text = "Scan QR Code",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Scan a campsite QR code to view its details",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // When the entrant presses this button, we open the scanner
            Button(
                onClick = {

                    // We clear the previous error before scanning again
                    errorMessage = null

                    scanner.startScan()
                        .addOnSuccessListener { barcode ->

                            // Here we get the content of the scanned QR Code
                            val content = barcode.rawValue

                            // If we get the content, we pass it to the callback.
                            // Otherwise, we show an error message.
                            if (content != null) {
                                onQRCodeScanned(content)
                            } else {
                                errorMessage = "QR code is empty"
                            }
                        }
                        .addOnFailureListener {

                            // If something goes wrong while scanning,
                            // we let the entrant know
                            errorMessage = "Unable to scan QR code"
                        }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = green,
                    contentColor = Color.Black
                )
            ) {
                Text("Open QR Scanner")
            }

            // We only show the error message if there is one
            errorMessage?.let { message ->

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // We keep the Back button at the bottom left of the screen
        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.BottomStart),
            colors = ButtonDefaults.buttonColors(
                containerColor = green,
                contentColor = Color.Black
            )
        ) {
            Text("Back")
        }
    }
}
