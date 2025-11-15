package com.example.bookingcourt.core.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    /**
     * Convert URI to File
     * @param context Android context
     * @param uri URI của file được chọn từ gallery
     * @return File object hoặc null nếu có lỗi
     */
    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver

            // Lấy tên file từ URI
            val fileName = getFileName(context, uri) ?: "temp_image_${System.currentTimeMillis()}.jpg"

            // Tạo file tạm trong cache directory
            val tempFile = File(context.cacheDir, fileName)

            // Copy content từ URI sang file
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            tempFile
        } catch (e: Exception) {
            android.util.Log.e("FileUtils", "Error converting URI to File", e)
            null
        }
    }

    /**
     * Lấy tên file từ URI
     */
    private fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null

        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = it.getString(nameIndex)
                    }
                }
            }
        }

        if (fileName == null) {
            fileName = uri.path?.let { path ->
                val cut = path.lastIndexOf('/')
                if (cut != -1) {
                    path.substring(cut + 1)
                } else {
                    path
                }
            }
        }

        return fileName
    }
}
