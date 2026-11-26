
/*
 * QRCodeManager.kt
 *
 * This file is part of the Model in our MVC architecture.
 * Here we handle the generation and decoding of QR Codes.
 *
 * We use the campsite ID to generate a unique QR Code
 * for each campsite. When the entrant scans it, we get
 * the campsite ID so we can find the corresponding campsite.
 *
 * Outstanding issues:
 * - For now, we only check the prefix and make sure the ID
 *   is not empty. We check if the campsite actually exists
 *   outside this class.
 */

package com.example.campsitereservationsystem.EntrantPath

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

/**
 * This class handles the creation and decoding of QR Codes
 * for the campsites in our application.
 */
class QRCodeManager {

    /**
     * This function generates the content that we are
     * going to use to create the QR Code.
     *
     * @param campsiteId The ID of the campsite.
     * @return The QR Code content containing the campsite ID.
     */
    fun generateContent(campsiteId: String): String {
        return "cypher://campsite/$campsiteId"
    }

    /**
     * This function generates the actual QR Code image
     * using the content we created before.
     *
     * @param content The content we want to put in the QR Code.
     * @return The QR Code image as a Bitmap.
     */
    fun generateQRImage(content: String): Bitmap {

        // We use 512 pixels for the width and height of the QR Code
        val size = 512

        // Here we use ZXing to generate the QR Code matrix
        val bitMatrix = MultiFormatWriter().encode(
            content, BarcodeFormat.QR_CODE, size, size
        )

        // We create the bitmap where we are going to draw the QR Code
        val bitmap = Bitmap.createBitmap(
            size, size, Bitmap.Config.ARGB_8888
        )

        // We go through each pixel and decide if it should
        // be black or white depending on the matrix
        for (x in 0 until size) {
            for (y in 0 until size) {
                val color = if (bitMatrix[x, y]) {
                    Color.BLACK
                } else {
                    Color.WHITE
                }

                bitmap.setPixel(x, y, color)
            }
        }

        return bitmap
    }

    /**
     * This function extracts the campsite ID from
     * the content of the scanned QR Code.
     *
     * @param content The content we get after scanning the QR Code.
     * @return The campsite ID, or null if the prefix is wrong
     * or the ID is empty.
     */
    fun decodeQRCode(content: String): String? {

        val prefix = "cypher://campsite/"

        // We check if the QR Code follows the format we use in our app
        if (!content.startsWith(prefix)) {
            return null
        }

        // We remove the prefix because we only need the campsite ID
        val campsiteId = content.removePrefix(prefix)

        // If there is no campsite ID, we return null
        if (campsiteId.isBlank()) {
            return null
        }

        return campsiteId
    }
}
