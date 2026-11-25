package com.example.campsitereservationsystem.qr

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

@Composable
fun QRCodeScannerScreen(
    onQRCodeScanned: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val green = Color(0xFF819F5C)

    val context = LocalContext.current

    val scanner = remember(context) {
        GmsBarcodeScanning.getClient(context)
    }

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
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = green,
                    contentColor = Color.Black
                )
            ) {
                Text("Open QR Scanner")
            }

            errorMessage?.let { message ->

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

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