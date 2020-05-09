package com.example.phonehelper.features.integrated.sharemedia

import android.app.Activity
import android.app.KeyguardManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ShareCompat
import com.example.phonehelper.R

class ShareMediaActivity: Activity() {

    companion object {
        const val REQ_CODE_SHARE_MEDIA = 333
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
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, shareableUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(contentResolver, getString(R.string.label_preview), shareableUri)
            type = contentResolver.getType(shareableUri)
        }

        startActivityForResult(Intent.createChooser(shareIntent, getString(R.string.label_share)), REQ_CODE_SHARE_MEDIA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE_SHARE_MEDIA -> {
                when (resultCode) {
                    RESULT_OK, RESULT_CANCELED -> finish()
                }
            }
        }
    }
}