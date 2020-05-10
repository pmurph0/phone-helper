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
import com.example.phonehelper.*
import com.example.phonehelper.features.integrated.IntegratedFeature
import java.lang.IllegalArgumentException
import java.lang.ref.WeakReference

class ShareMediaLockedFeature(accessibilityService: AccessibilityService): IntegratedFeature(accessibilityService) {

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
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            && event.packageName == AppIds.GALLERY && isDeviceLocked) {
            //compensates for unreliable current app change events
            setUp()
        }
        galleryMediaPositionTracker?.onAccessibilityEvent(event)
    }

    override fun onCurrentAppChanged(appId: String) {
        when (appId) {
            AppIds.GALLERY -> {
                if (isDeviceLocked) {
                    setUp()
                } else {
                    tearDown()
                }
            }
            else -> {
                tearDown()
            }
        }
    }

    private fun setUp() {
        if (viewRef == null) {
            addShareBtn()
        }
        if (galleryMediaPositionTracker == null) {
            galleryMediaPositionTracker = GalleryMediaPositionTracker(accessibilityService)
        }
    }

    private fun tearDown() {
        galleryMediaPositionTracker = null
        removeShareBtn()
    }

    private fun removeShareBtn() {
        viewRef?.get()?.let {
            try {
                windowManager.removeView(it)
                log("removed view")
            } catch (e: IllegalArgumentException) {
                log("view not attached")
            }
        }
        viewRef?.clear()
        viewRef = null
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
            it.postDelayed({
                removeShareBtn()
            }, 120)
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

