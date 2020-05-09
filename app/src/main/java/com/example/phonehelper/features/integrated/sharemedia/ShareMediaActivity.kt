package com.example.phonehelper.features.integrated.sharemedia

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ShareCompat
import com.example.phonehelper.R
import com.example.phonehelper.log

class ShareMediaActivity: Activity() {

    companion object {
        const val EXTRA_SHAREABLE_MEDIA_URI = "EXTRA_SHAREABLE_MEDIA_URI"

        fun getIntent(context: Context, imageUri: Uri): Intent {
            return Intent(context, ShareMediaActivity::class.java).apply {
                putExtra(EXTRA_SHAREABLE_MEDIA_URI, imageUri)
            }
        }
    }

    private val shareableUri: Uri by lazy { intent.getParcelableExtra(EXTRA_SHAREABLE_MEDIA_URI) as Uri }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        unlockPhoneAndLaunchShareIntent()
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

}