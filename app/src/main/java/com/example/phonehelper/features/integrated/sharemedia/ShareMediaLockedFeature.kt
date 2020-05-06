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
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageView
import com.example.phonehelper.R
import com.example.phonehelper.features.integrated.IntegratedFeature
import com.example.phonehelper.log
import com.example.phonehelper.screenSize
import com.example.phonehelper.toPx

class ShareMediaLockedFeature(accessibilityService: AccessibilityService): IntegratedFeature(accessibilityService),
    GallerySwipeImageDetector.Listener {

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

    private var mediaPosition = 0

    private var gallerySwipeImageDetector: GallerySwipeImageDetector? = null

    override fun onServiceConnected() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        log("onAccessibilityEvent ${mapEventType(event.eventType)} ${event.packageName}")

        gallerySwipeImageDetector?.onAccessibilityEvent(event)    //TODO un-comment

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
        mediaPosition = 0
        if (gallerySwipeImageDetector == null) {
            gallerySwipeImageDetector = GallerySwipeImageDetector(accessibilityService, this)
        } else {
            log("GallerySwipeImageDetector already initialized :/")
        }
    }

    private fun reset() {
        gallerySwipeImageDetector = null
        mediaPosition = 0
        hideShareBtn()
    }

    private fun hideShareBtn() {
        //TODO
    }

    private fun addShareBtn() {
        val view = ImageView(accessibilityService).apply {
            setImageResource(R.drawable.ic_share_black_24dp)
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            foreground = accessibilityService.getDrawable(outValue.resourceId)
        }
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
        accessibilityService.startActivity(
            ShareMediaActivity.getIntent(
                accessibilityService,
                mediaPosition
            )
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun onSwipeToNextImage() {
        mediaPosition++
    }

    override fun onSwipeToPreviousImage() {
        mediaPosition--
    }
}

fun mapEventType(type: Int): String {
    return when (type) {
    AccessibilityEvent.TYPE_ANNOUNCEMENT -> "TYPE_ANNOUNCEMENT"
    AccessibilityEvent.TYPE_ASSIST_READING_CONTEXT -> "TYPE_ASSIST_READING_CONTEXT"
    AccessibilityEvent.TYPE_GESTURE_DETECTION_END -> "TYPE_GESTURE_DETECTION_END"
    AccessibilityEvent.TYPE_GESTURE_DETECTION_START -> "TYPE_GESTURE_DETECTION_START"
    AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> "TYPE_NOTIFICATION_STATE_CHANGED"
    AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END -> "TYPE_TOUCH_EXPLORATION_GESTURE_END"
    AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START -> "TYPE_TOUCH_EXPLORATION_GESTURE_START"
    AccessibilityEvent.TYPE_TOUCH_INTERACTION_END -> "TYPE_TOUCH_INTERACTION_END"
    AccessibilityEvent.TYPE_TOUCH_INTERACTION_START -> "TYPE_TOUCH_INTERACTION_START"
    AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED -> "TYPE_VIEW_ACCESSIBILITY_FOCUSED"
    AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED -> "TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED"
    AccessibilityEvent.TYPE_VIEW_CLICKED -> "TYPE_VIEW_CLICKED"
    AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED -> "TYPE_VIEW_CONTEXT_CLICKED"
    AccessibilityEvent.TYPE_VIEW_FOCUSED -> "TYPE_VIEW_FOCUSED"
    AccessibilityEvent.TYPE_VIEW_HOVER_ENTER -> "TYPE_VIEW_HOVER_ENTER"
    AccessibilityEvent.TYPE_VIEW_HOVER_EXIT -> "TYPE_VIEW_HOVER_EXIT"
    AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> "TYPE_VIEW_LONG_CLICKED"
    AccessibilityEvent.TYPE_VIEW_SCROLLED -> "TYPE_VIEW_SCROLLED"
    AccessibilityEvent.TYPE_VIEW_SELECTED -> "TYPE_VIEW_SELECTED"
    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> "TYPE_VIEW_TEXT_CHANGED"
    AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> "TYPE_VIEW_TEXT_SELECTION_CHANGED"
    AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY -> "TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY"
    AccessibilityEvent.TYPE_WINDOWS_CHANGED -> "TYPE_WINDOWS_CHANGED"
    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> "TYPE_WINDOW_CONTENT_CHANGED"
    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> "TYPE_WINDOW_STATE_CHANGED"
        else -> "UNKNOWN"
    }
}