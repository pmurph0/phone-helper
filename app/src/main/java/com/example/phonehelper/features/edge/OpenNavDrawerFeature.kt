package com.example.phonehelper.features.edge

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import com.example.phonehelper.Preferences
import com.example.phonehelper.log
import com.example.phonehelper.toPx
import java.util.concurrent.TimeUnit

class OpenNavDrawerFeature(private val accessibilityService: AccessibilityService,
                           private val preferences: Preferences): EdgeFeature {

    private val gestureStartDelay = 0L
    private val gestureDuration = TimeUnit.MILLISECONDS.toMillis(150L)
    private val gestureEndX get() = 700f.toPx(accessibilityService).toFloat()
    private val finger1YPos get() = 300f.toPx(accessibilityService).toFloat()
    private val finger2YPos get() = 500f.toPx(accessibilityService).toFloat()
    private val gestureStartX get() = preferences.leftEdgeWidth.toFloat()

    override fun onActionTriggered() {
        try {
            dispathGesture()
        } catch (e:Exception) {
            log(e.message)
        }
    }

    private fun dispathGesture() {
        //TODO check layout for DrawerLayout first?
        accessibilityService.dispatchGesture(
            buildTwoFingerSwipeGesture(),
            object : AccessibilityService.GestureResultCallback() {
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    log("gesture cancelled")
                }

                override fun onCompleted(gestureDescription: GestureDescription?) {
                    log("gesture completed")
                }
            },
            null
        ).also { gestureDispatched ->
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
                this.moveTo(gestureStartX, finger1YPos)
                this.lineTo(gestureEndX, finger1YPos)
            },
            gestureStartDelay,
            TimeUnit.MILLISECONDS.toMillis(gestureDuration)
        )
    }

    private fun buildFinger2Swipe(): GestureDescription.StrokeDescription {
        return GestureDescription.StrokeDescription(
            Path().apply {
                this.moveTo(gestureStartX, finger2YPos)
                this.lineTo(gestureEndX, finger2YPos)
            },
            gestureStartDelay,
            TimeUnit.MILLISECONDS.toMillis(gestureDuration)
        )
    }

}