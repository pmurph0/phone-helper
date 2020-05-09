package com.example.phonehelper.features.integrated.sharemedia

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

class MediaManager(private val context: Context) {

    companion object {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,  //TODO dont use deprecated API
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE
        )

        // Return only video and image metadata.
        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

        const val FILE_PROVIDER_AUTHORITY = "provider"
    }

    private fun getRecentMediaFile(position: Int): File? {
        val queryUri = MediaStore.Files.getContentUri("external")

        val cursor = context.contentResolver.query(
            queryUri,
            projection,
            selection,
            null,  // Selection args (none).
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC" // Sort order.
        ) ?: return null

        if (!cursor.moveToPosition(position)) {
            cursor.close()
            return null
        }
        val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val path = cursor.getString(pathIndex)
        cursor.close()
        return File(path)
    }

    fun getRecentMediaFileAsShareableUri(position: Int): Uri? {
        val file = getRecentMediaFile(position) ?: return null
        return FileProvider.getUriForFile(
            context.applicationContext, "${context.applicationContext.packageName}.$FILE_PROVIDER_AUTHORITY", file
        )
    }

}