package com.example.campsitereservationsystem.qr

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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