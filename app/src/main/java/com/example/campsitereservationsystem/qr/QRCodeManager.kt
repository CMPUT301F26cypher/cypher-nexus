package com.example.campsitereservationsystem.qr

import android.graphics.Bitmap

class QRCodeManager {

    //This function generates de QR Code for each campsite
    fun generateContent(campsiteId:String): String{
        return "cypher://campsite/$campsiteId"
    }

    //This function generates de actual QR Image
    fun generateQRImage(content:String): Bitmap{
        TODO("Generate QR image")
    }

    //This function extract the campsite ID from the scanned QR content
    fun decodeQRCode(content:String): String? {
        val prefix = "cypher://campsite/"

        if(!content.startsWith(prefix)){
            return null
        }

        val campsiteId = content.removePrefix(prefix)

        if(campsiteId.isBlank()){
            return null
        }
        return campsiteId
    }
}