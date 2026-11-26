
/*
 * QRCodeScreen.kt
 *
 * This file is part of the View in our MVC architecture.
 * Here we show the promotional QR Code of a campsite
 * so the owner can use it to promote their campsite.
 *
 * We use the QRCodeManager to generate the QR Code
 * using the campsite ID.
 *
 * Outstanding issues:
 * - For now, the owner can see the QR Code, but we do not
 *   have a button to download or share it directly.
 */

package com.example.campsitereservationsystem.EntrantPath

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

/**
 * This screen allows the owner to see the promotional
 * QR Code of their campsite.
 *
 * @param campsiteId The ID we use to generate the QR Code.
 * @param campsiteName The name of the campsite we want to display.
 * @param onBack Callback used to go back to the previous screen.
 */
@Composable
fun QRCodeScreen(
    campsiteId: String,
    campsiteName: String,
    onBack: () -> Unit
) {

    BackHandler(onBack = onBack)

    val green = Color(0xFF819F5C)

    // Here we create the manager we use to generate the QR Code
    val qrCodeManager = remember {
        QRCodeManager()
    }

    // We generate the QR Code using the campsite ID.
    // We only generate it again if the campsite ID changes.
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

        Text(
            text = "Promotional QR Code",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // We make this part scrollable so the owner
        // can see all the information on the screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Here we show which campsite the QR Code belongs to
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

            // Here we display the QR Code inside a white card
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

                    // We convert the bitmap so we can display it as an image
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription =
                            "QR Code for $campsiteName",
                        modifier = Modifier.size(250.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // We explain what the QR Code can be used for
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

        // We keep the Back button at the bottom of the screen
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
