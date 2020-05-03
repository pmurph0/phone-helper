package com.example.phonehelper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
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

    @SuppressLint("RtlHardcoded")
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
    }

    private fun volumeUp() {
        log("volume up")
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }

    private fun tryOpenNavDrawer() {
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
                this.moveTo(0f, finger1YPos)
                this.lineTo(gestureEndX, finger1YPos)
            },
            gestureStartDelay,
            TimeUnit.MILLISECONDS.toMillis(gestureDuration)
        )
    }

    private fun buildFinger2Swipe(): GestureDescription.StrokeDescription {
        return GestureDescription.StrokeDescription(
            Path().apply {
                this.moveTo(0f, finger2YPos)
                this.lineTo(gestureEndX, finger2YPos)
            },
            gestureStartDelay,
            TimeUnit.MILLISECONDS.toMillis(gestureDuration)
        )
    }

    private val gestureStartDelay = 0L
    private val gestureDuration = TimeUnit.MILLISECONDS.toMillis(150L)
    private val gestureEndX get() = dpToPx(500f).toFloat()
    private val finger1YPos get() = dpToPx(300f).toFloat()
    private val finger2YPos get() = dpToPx(500f).toFloat()
}