package com.example.campsitereservationsystem

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

object ProfileImage {
    // image should be under ImageRepository's max size (300kb)
    private const val MAX_BYTES = 250_000

    // Compress the image if it is too big
    fun compress(context: Context, uri: Uri, maxSize: Int = 256): ByteArray? {
        return try {
            val original = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: return null
            val scale = maxSize.toFloat() / maxOf(original.width, original.height)
            val bitmap = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    original,
                    (original.width * scale).toInt(),
                    (original.height * scale).toInt(),
                    true
                )
            } else {
                original
            }
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
            val bytes = out.toByteArray()
            // Checks if the images is the correct size
            if (bytes.size <= MAX_BYTES) bytes else null
        }
        catch (e: Exception) {
            null
        }
    }

    fun toBitmap(bytes: ByteArray?): ImageBitmap? {
        if (bytes == null) {
            return null
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }
}