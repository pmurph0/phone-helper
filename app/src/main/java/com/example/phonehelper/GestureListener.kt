package com.example.phonehelper

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

abstract class GestureListener(context: Context) {

    private val onGestureListener = object: GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1.y > e2.y) {
                //fling up
                onGesture(Gesture.FLING_UP)
            } else {
                //fling down
                onGesture(Gesture.FLING_DOWN)
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            onGesture(Gesture.DOUBLE_TAP)
            return super.onDoubleTap(e)
        }
    }

    private val gestureDetector = GestureDetector(context, onGestureListener)

    val onTouchListener = View.OnTouchListener { _, event ->
        gestureDetector.onTouchEvent(event)
        true
    }

    abstract fun onGesture(gesture: Gesture)
}