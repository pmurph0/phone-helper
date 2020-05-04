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

    override fun onServiceConnected() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when {
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                    && (event.packageName == "com.oneplus.gallery")
                    && isDeviceLocked-> addShareBtn()
        }
    }

    private fun addShareBtn() {
        val view = ImageView(accessibilityService).apply {
            setImageResource(R.drawable.ic_share_black_24dp)
            val outValue = TypedValue()
            context.theme
                .resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
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
            0,
            PixelFormat.TRANSLUCENT
        ))
        view.setOnClickListener {
            it.visibility = View.GONE
            launchShareIntent()
        }
    }

    private fun launchShareIntent() {
        accessibilityService.startActivity(
            ShareMediaActivity.getIntent(
                accessibilityService
            )
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}