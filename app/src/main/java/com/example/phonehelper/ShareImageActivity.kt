package com.example.phonehelper

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import java.io.File


class ShareImageActivity: Activity() {

    companion object {
        const val EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI"
        fun getIntent(context: Context, imageUri: Uri): Intent {
            return Intent(context, ShareImageActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URI, imageUri)
            }
        }
        fun getIntent(context: Context): Intent {
            return Intent(context, ShareImageActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        unlockPhone()

    }

    private fun unlockPhone() {
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
        val intent = Intent(Intent.ACTION_SEND)

        val apkURI = FileProvider.getUriForFile(
            this, this.getApplicationContext()
                .getPackageName().toString() + ".provider", uri.toFile()
        )
        intent.setDataAndType(apkURI, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Share Image"))
    }

    val uri: Uri get() {
        return intent.getParcelableExtra<Uri>(EXTRA_IMAGE_URI) ?: getLastImageInCameraFolder(this)!!
    }

    private fun getLastImageInCameraFolder(c: Context): Uri? {
        //TODO dont use deprecated APIs
        val resolver = c.contentResolver ?: return null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor =
            resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null, "date_modified DESC")
        val count = cursor!!.count
        val position = 0
        if (!cursor.moveToPosition(position)) {
            return null
        }
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val path = cursor.getString(column_index)
        cursor.close()
        return Uri.fromFile(File(path))
    }

}