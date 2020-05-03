package com.example.phonehelper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.drawerlayout.widget.DrawerLayout
import java.lang.Exception
import java.util.concurrent.TimeUnit


class AccessibilityService : AccessibilityService() {
    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
    }

    override fun onServiceConnected() {

        //Add view
        addOverlayView()
    }

    private fun addOverlayView() {
        val view = View(this).apply {
            background = ColorDrawable(Color.WHITE)
        }
        val windowManager: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(view, WindowManager.LayoutParams(
            getTouchAreaWidth(),
            getTouchAreaHeight(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES
            or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
        })
        val gestureDetector = GestureDetector(this, gestureListener)
        view.setOnTouchListener { _, motionEvent ->
            log("onTouch $motionEvent")
            gestureDetector.onTouchEvent(motionEvent)
        }
    }

    private fun getTouchAreaHeight(): Int {
        return dpToPx(600f)
    }

    private fun getTouchAreaWidth(): Int {
        val dip = 20f
        return dpToPx(dip)
    }

    private fun dpToPx(dip: Float): Int {
        val r: Resources = resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            r.displayMetrics
        ).toInt()
    }

    private val gestureListener = object: GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            log("onFling")
            //TODO reject drag by checking velocity
            if (e1.y > e2.y) {
                //fling up
                volumeUp()
            } else {
                //fling down
                volumeDown()
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            log("onDoubleTap")
            tryOpenNavDrawer()
            return true
        }
    }

    private fun log(msg: String?) {
        if (msg != null) Log.d(com.example.phonehelper.AccessibilityService::class.java.simpleName, msg)
    }

    private fun volumeDown() {
        log("volume down")
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
        //TODO
    }

    private fun volumeUp() {
        log("volume up")
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
        //TODO
    }

    private fun tryOpenNavDrawer() {
//        tryOpenNavDrawerUsingClick()
        try {
            tryOpenNavDrawerUsingGesture()
        } catch (e:Exception) {
            println(e.message)
        }

    }

    private fun tryOpenNavDrawerUsingGesture() {
        dispatchGesture(
            buildTwoFingerSwipeGesture(),
            object: GestureResultCallback() {
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    log("gesture cancelled")
                }

                override fun onCompleted(gestureDescription: GestureDescription?) {
                    log("gesture completed")
                }
            },
            null).also {gestureDispatched ->
            log(if (gestureDispatched) "gesture dispatched" else "gesture not dispatched")
        }
    }

    private fun buildTwoFingerSwipeGesture(): GestureDescription {
        return GestureDescription.Builder()
            .addStroke(buildFinger1Swipe())
            .addStroke(buildFinger2Swipe())
            .build()
    }



    private fun buildFinger1Swipe(): GestureDescription.StrokeDescription {
        return GestureDescription.StrokeDescription(
            Path().apply {
                this.moveTo(0f, dpToPx(300f).toFloat())
                this.lineTo(gestureEndX, dpToPx(300f).toFloat())
            },
            gestureStartDelay,
            TimeUnit.MILLISECONDS.toMillis(gestureDuration)
        )
    }

    private fun buildFinger2Swipe(): GestureDescription.StrokeDescription {
        return GestureDescription.StrokeDescription(
            Path().apply {
                this.moveTo(0f, dpToPx(500f).toFloat())
                this.lineTo(gestureEndX, dpToPx(500f).toFloat())
            },
            gestureStartDelay,
            TimeUnit.MILLISECONDS.toMillis(gestureDuration)
        )
    }

    private val gestureStartDelay = 0L
    private val gestureDuration = 300L
    private val gestureEndX get() = dpToPx(300f).toFloat()

    private fun tryOpenNavDrawerUsingClick() {
        findDrawerLayoutButton(rootInActiveWindow)?.let { node ->
            log("Attempting click on node...")
            if (node.isClickable) {
                log("Performing click on node")
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else {
                log("node is not clickable!")
            }
        }
    }

    private fun findDrawerLayoutButton(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.viewIdResourceName == null) log("view id resource name is null") else log(node.viewIdResourceName)
        if (node.viewIdResourceName?.contains("burger", ignoreCase = true) == true) {
            log("found drawer trigger view, id is ${node.viewIdResourceName}")
            return node
        }
        if (node.childCount > 0) {
            for (i in 0 until node.childCount) {
                val childNode = findDrawerLayoutButton(node.getChild(i))
                if (childNode != null) return node
            }
        }
        return null
    }


}