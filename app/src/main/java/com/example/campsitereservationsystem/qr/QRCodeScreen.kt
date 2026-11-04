package com.example.campsitereservationsystem.qr

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QRCodeScreen(campsiteId:String,
                 campsiteName:String,
                 onBack:()->Unit
) {

    val qrCodeManager = remember{ QRCodeManager() }

    val qrBitmap = remember(campsiteId){
        val content = qrCodeManager.generateContent(campsiteId)
        qrCodeManager.generateQRImage(content)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "GO BACK"
                )
            }
        }
        Spacer(modifier = Modifier.height(50.dp))

        Text(text = "PROMOTIONAL QR CODE",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(100.dp))

        Text(text = "$campsiteName",
            fontSize = 30.sp,
            fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(50.dp))

        Image(
            bitmap = qrBitmap.asImageBitmap(),
            contentDescription = "QR Code for $campsiteName",
            modifier = Modifier.size(250.dp).align(Alignment.CenterHorizontally)
            )

        Spacer(modifier = Modifier.height(50.dp))

        Text(text = "SCAN THIS QR CODE TO VIEW CAMPSITE DETAILS",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(24.dp))




    }



}