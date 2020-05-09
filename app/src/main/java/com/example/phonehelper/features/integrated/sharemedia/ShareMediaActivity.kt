package com.example.phonehelper.features.integrated.sharemedia

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.example.phonehelper.R
import com.example.phonehelper.log
import java.io.File


//TODO tidy
class ShareMediaActivity: Activity() {

    companion object {
        const val EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI"
        const val EXTRA_MEDIA_POSITION = "EXTRA_MEDIA_POSITION"

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

        fun getIntent(context: Context, imageUri: Uri): Intent {
            return Intent(context, ShareMediaActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URI, imageUri)
            }
        }
        fun getIntent(context: Context, mediaPosition: Int): Intent {
            return Intent(context, ShareMediaActivity::class.java).apply {
                putExtra(EXTRA_MEDIA_POSITION, mediaPosition)
            }
        }
    }

    private val mediaPosition get() = intent.getIntExtra(EXTRA_MEDIA_POSITION, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        displayImage()
        unlockPhoneAndLaunchShareIntent()
    }

    private fun displayImage() {
        //TODO
    }

    private fun unlockPhoneAndLaunchShareIntent() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyguardManager.isDeviceLocked) {
            keyguardManager.requestDismissKeyguard(
                this,
                object : KeyguardManager.KeyguardDismissCallback() {
                    override fun onDismissSucceeded() {
                        launchShareIntent()
                    }
                })
        } else {
            launchShareIntent()
        }
    }

    private fun launchShareIntent() {
        val uri = getLastImageInCameraFolder() ?: return
        val shareableUri = FileProvider.getUriForFile(
            this, this.applicationContext
                .packageName.toString() + ".provider", uri.toFile()
        )
        val shareIntent = ShareCompat.IntentBuilder.from(this)
            .setStream(shareableUri)
            .intent
        shareIntent.data = shareableUri
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val mimeType = contentResolver.getType(shareableUri)
        log("mimeType is $mimeType")
        shareIntent.type = mimeType
        startActivity(Intent.createChooser(shareIntent, getString(R.string.label_share)))
    }

    private fun getLastImageInCameraFolder(): Uri? {
        // Get relevant columns for use later.
        // Get relevant columns for use later.
        val queryUri = MediaStore.Files.getContentUri("external")

        val cursor = contentResolver.query(
            queryUri,
            projection,
            selection,
            null,  // Selection args (none).
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC" // Sort order.
        )

        if (cursor == null) {
            return null
        }

        val position = mediaPosition
        if (!cursor.moveToPosition(position)) {
            cursor.close()
            return null
        }
        val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val path = cursor.getString(pathIndex)
        cursor.close()
        return Uri.fromFile(File(path))
    }

}