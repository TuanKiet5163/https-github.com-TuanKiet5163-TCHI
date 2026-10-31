package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object PhotoStorageUtil {

    private const val PHOTO_DIR = "tchi_photos"

    /**
     * Saves a Bitmap to app internal storage and returns the absolute file path.
     */
    suspend fun saveBitmap(context: Context, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, PHOTO_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "tchi_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        file.absolutePath
    }

    /**
     * Copies an image from a content URI to app internal storage.
     */
    suspend fun saveFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, PHOTO_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "tchi_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            val bitmap = BitmapFactory.decodeStream(input)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
        }
        file.absolutePath
    }
}
