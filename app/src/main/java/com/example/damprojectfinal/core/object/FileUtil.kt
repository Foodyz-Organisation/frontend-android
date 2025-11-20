package com.example.damprojectfinal.core.`object`

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

// Wrapper to hold File and MIME type
data class FileWithMime(val file: File, val mimeType: String)

object FileUtil {

    /**
     * Converts a Uri to a physical File with MIME type.
     * Returns null if something goes wrong.
     */
    fun getFileWithMime(context: Context, uri: Uri): FileWithMime? {
        return try {
            // Get MIME type
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

            // Get original file name
            val fileName = getFileName(context, uri) ?: "temp_file"

            // Create temp file in cache dir
            val tempFile = File(context.cacheDir, fileName)

            // Copy contents from Uri to temp file
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            FileWithMime(tempFile, mimeType)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) return cursor.getString(index)
            }
        }
        return uri.lastPathSegment
    }
}
