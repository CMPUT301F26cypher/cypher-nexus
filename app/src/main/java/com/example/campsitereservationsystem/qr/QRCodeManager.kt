package com.example.campsitereservationsystem.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter


class QRCodeManager {

    //This function generates de QR Code for each campsite
    fun generateContent(campsiteId:String): String{
        return "cypher://campsite/$campsiteId"
    }

    //This function generates de actual QR Image
    fun generateQRImage(content:String): Bitmap{

        val size =512

        val bitMatrix = MultiFormatWriter().encode(
            content, BarcodeFormat.QR_CODE,size,size
        )

        val bitmap = Bitmap.createBitmap(
            size,size, Bitmap.Config.ARGB_8888
        )

        for(x in 0 until size){
            for(y in 0 until size){
                val color = if(bitMatrix[x,y]){
                    Color.BLACK
                }else{
                    Color.WHITE
                }
                bitmap.setPixel(x,y,color)
            }
        }

        return bitmap
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