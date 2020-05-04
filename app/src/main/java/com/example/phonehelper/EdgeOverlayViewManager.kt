package com.example.phonehelper

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager

class EdgeOverlayViewManager(private val context: Context, private val preferences: Preferences,
                             private val listener: Listener) {

    private val windowManager: WindowManager get() = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    fun addEdgeOverlayViews() {
        if (preferences.isLeftEdgeEnabled) {
            addLeftEdgeView()
        }
        if (preferences.isRightEdgeEnabled) {
            addRightEdgeView()
        }
    }

    private fun addLeftEdgeView() {
        val gestureListener = object: GestureListener(context) {
            override fun onGesture(gesture: Gesture) {
                listener.onEdgeGestureTriggered(EdgeGestureTrigger(Edge.LEFT, gesture))
            }
        }
        addTranslucentOverlayView(width = preferences.leftEdgeWidth,
            height = preferences.leftEdgeHeight,
            x = 0 - (context.screenSize.width/2)  ,
            y = preferences.leftEdgeYPos,
            onTouchListener = gestureListener.onTouchListener
        )
    }

    private fun addRightEdgeView() {
        val gestureListener = object: GestureListener(context) {
            override fun onGesture(gesture: Gesture) {
                listener.onEdgeGestureTriggered(EdgeGestureTrigger(Edge.RIGHT, gesture))
            }
        }
        addTranslucentOverlayView(width = preferences.rightEdgeWidth,
            height = preferences.rightEdgeHeight,
            x = context.screenSize.width - preferences.rightEdgeWidth,
            y = preferences.rightEdgeYPos,
            onTouchListener = gestureListener.onTouchListener)
    }

    private fun addTranslucentOverlayView(width: Int, height: Int, x: Int, y: Int, onTouchListener: View.OnTouchListener) {
        val view = View(context).apply {
            setOnTouchListener(onTouchListener)
            background = ColorDrawable(Color.WHITE)
        }
        windowManager.addView(view, WindowManager.LayoutParams(
            width,
            height,
            x,
            y,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ))
    }

    interface Listener {
        fun onEdgeGestureTriggered(gestureTrigger: EdgeGestureTrigger)
    }

}
