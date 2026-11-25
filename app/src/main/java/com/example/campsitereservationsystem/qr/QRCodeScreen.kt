package com.example.campsitereservationsystem.qr

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QRCodeScreen(
    campsiteId: String,
    campsiteName: String,
    onBack: () -> Unit
) {

    BackHandler(onBack = onBack)

    val green = Color(0xFF819F5C)

    // Initialize the QR Code Manager
    val qrCodeManager = remember {
        QRCodeManager()
    }

    // Generate the promotional QR code
    val qrBitmap = remember(campsiteId) {

        val content = qrCodeManager.generateContent(
            campsiteId
        )

        qrCodeManager.generateQRImage(content)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {

        // Screen title
        Text(
            text = "Promotional QR Code",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Campsite name
            Text(
                text = campsiteName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Share your campsite with prospective guests",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Promotional QR Code
            Card(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, green),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription =
                            "QR Code for $campsiteName",
                        modifier = Modifier.size(250.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Scan this QR code to view campsite details.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Use it on posters, flyers, or social media.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Back button
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = green,
                contentColor = Color.Black
            )
        ) {
            Text("Back")
        }
    }
}