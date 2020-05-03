package com.example.phonehelper

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ShareImageActivity: Activity() {

    companion object {
        const val EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI"
        fun getIntent(context: Context, imageUri: Uri): Intent {
            return Intent(context, ShareImageActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URI, imageUri)
            }
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
        keyguardManager.requestDismissKeyguard(
            this,
            object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() {
                    launchShareIntent()
                }
            })
    }

    private fun launchShareIntent() {
//        val sendIntent: Intent = Intent().apply {
//            action = Intent.ACTION_SEND
//            putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
//            type = "text/plain"
//        }
//
//        val shareIntent = Intent.createChooser(sendIntent, null)
//        startActivity(shareIntent.apply {
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        })


        val bitmap: Bitmap = ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(
                this.contentResolver,
                intent.getParcelableExtra(EXTRA_IMAGE_URI) as Uri
            )
        )
//        val intent = Intent(Intent.ACTION_SEND)
//        intent.type = "image/*"
//        intent.putExtra(Intent.EXTRA_STREAM, getBitmapFromView(bitmap))
//        startActivity(Intent.createChooser(intent, "Share Image"))


        val intent = Intent(Intent.ACTION_SEND)

        val apkURI = FileProvider.getUriForFile(
            this, this.getApplicationContext()
                .getPackageName().toString() + ".provider", uri.toFile()
        )
        intent.setDataAndType(apkURI, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Share Image"))
    }

    val uri: Uri get() = intent.getParcelableExtra<Uri>(EXTRA_IMAGE_URI) as Uri

    fun getBitmapFromView(bmp: Bitmap?): Uri? {
        var bmpUri: Uri? = null
        try {
            val file = File(this.externalCacheDir, System.currentTimeMillis().toString() + ".jpg")

            val out = FileOutputStream(file)
            bmp?.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.close()
            bmpUri = Uri.fromFile(file)

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmpUri
    }
}