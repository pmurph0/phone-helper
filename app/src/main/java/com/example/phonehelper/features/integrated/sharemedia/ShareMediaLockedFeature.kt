package com.example.phonehelper.features.integrated.sharemedia

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import com.example.phonehelper.R
import com.example.phonehelper.features.integrated.IntegratedFeature
import com.example.phonehelper.log
import com.example.phonehelper.screenSize
import com.example.phonehelper.toPx
import java.lang.IllegalArgumentException
import java.lang.ref.WeakReference

class ShareMediaLockedFeature(accessibilityService: AccessibilityService): IntegratedFeature(accessibilityService) {

    companion object {
        const val GALLERY_APP_PACKAGE_NAME = "com.oneplus.gallery"
    }

    private val isDeviceLocked: Boolean get() = keyguardManager.isDeviceLocked
    private val windowManager: WindowManager get() = accessibilityService.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val keyguardManager get() = accessibilityService.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

    private val shareBtnWidth: Int get() = accessibilityService.resources.getDimensionPixelOffset(
        R.dimen.share_btn_size
    )
    private val shareBtnHeight: Int get() = accessibilityService.resources.getDimensionPixelOffset(
        R.dimen.share_btn_size
    )
    private val shareBtnMarginBottom get() = 45f.toPx(accessibilityService)
    private val shareBtnMarginRight get() = 70f.toPx(accessibilityService)

    private var galleryMediaPositionTracker: GalleryMediaPositionTracker? = null

    private var viewRef: WeakReference<View>? = null

    override fun onServiceConnected() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        galleryMediaPositionTracker?.onAccessibilityEvent(event)    //TODO un-comment

        when(event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                when (event.packageName) {
                    GALLERY_APP_PACKAGE_NAME -> {
                        if (isDeviceLocked) {
                            setUp()
                        } else {
                            reset()
                        }
                    }
                    accessibilityService.applicationContext.packageName -> {
                        //do nothing
                    }
                    else -> {
                        reset()
                    }
                }
            }

        }
    }

    private fun setUp() {
        addShareBtn()
        if (galleryMediaPositionTracker == null) {
            galleryMediaPositionTracker = GalleryMediaPositionTracker(accessibilityService)
        } else {
            log("GallerySwipeImageDetector already initialized :/")
        }
    }

    private fun reset() {
        galleryMediaPositionTracker = null
        hideShareBtn()
        viewRef?.clear()
    }

    private fun hideShareBtn() {
        viewRef?.get()?.let {
            try {
                windowManager.removeView(it)
                log("removed view")
            } catch (e: IllegalArgumentException) {
                log("view not attached")
            }
        }
    }

    private fun addShareBtn() {
        val view = ImageView(accessibilityService).apply {
            setImageResource(R.drawable.ic_share_white_24dp)
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            foreground = accessibilityService.getDrawable(outValue.resourceId)
        }
        viewRef = WeakReference(view)
        val screenSize = accessibilityService.screenSize
        val xPos = (screenSize.width / 2) - shareBtnMarginRight
        val yPos = (screenSize.height / 2) - shareBtnMarginBottom
        windowManager.addView(view, WindowManager.LayoutParams(
            shareBtnWidth,
            shareBtnHeight,
            xPos,
            yPos,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ))
        view.setOnClickListener {
            launchShareIntent()
        }
    }

    private fun launchShareIntent() {
        val uri = MediaManager(accessibilityService)
            .getRecentMediaFileAsShareableUri(galleryMediaPositionTracker?.currentPosition ?: 0)
            ?: return
        accessibilityService.startActivity(
            ShareMediaActivity.getIntent(accessibilityService, uri)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }
}

