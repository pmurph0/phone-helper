package com.example.phonehelper.features.edge

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.example.phonehelper.features.Gesture
import com.example.phonehelper.log

abstract class GestureListener(context: Context) {

    companion object {
        private const val MOVE_DIRECTION_UP = 1
        private const val MOVE_DIRECTION_NONE = 0
        private const val MOVE_DIRECTION_DOWN = -1

        private const val INVALID = Int.MAX_VALUE.toFloat()
    }

    private val onGestureListener = object: GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent?): Boolean {
            onGesture(Gesture.DOUBLE_TAP)
            return super.onDoubleTap(e)
        }
    }

    private val gestureDetector = GestureDetector(context, onGestureListener)

    val onTouchListener = View.OnTouchListener { _, event ->
        log(event.toString())

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onTouchStart(event)
            }
            MotionEvent.ACTION_MOVE -> {
                onTouchMove(event)
            }
            MotionEvent.ACTION_UP -> {
                onTouchRelease(event)
            }
        }

        gestureDetector.onTouchEvent(event)
        true
    }

    private var isMoving = false
    private var moveDeepestPoint = INVALID
    private var initialMoveDirection = MOVE_DIRECTION_NONE
    private var touchStartPoint = 0f

    private fun onTouchRelease(event: MotionEvent) {
        if (isMoving) {
            val didScrub = when (initialMoveDirection) {
                MOVE_DIRECTION_UP -> (event.y - moveDeepestPoint) > 100
                MOVE_DIRECTION_DOWN -> (moveDeepestPoint - event.y) > 100
                else -> false
            }
            if (didScrub) {
                onGesture(Gesture.SCRUB)
            } else {
                onMovedLinearly(event)
            }
        }

        reset()
    }

    private fun onMovedLinearly(event: MotionEvent) {
        //TODO measure velocity and/or distance to determine if fling
        if (event.y < touchStartPoint) {
            onGesture(Gesture.FLING_UP)
        } else {
            onGesture(Gesture.FLING_DOWN)
        }
    }

    private fun reset() {
        isMoving = false
        moveDeepestPoint = 0f
        initialMoveDirection = MOVE_DIRECTION_NONE
        touchStartPoint = 0f
    }

    private fun onTouchMove(event: MotionEvent) {
        if (initialMoveDirection == MOVE_DIRECTION_NONE) {
            initialMoveDirection = when {
                event.y > touchStartPoint -> MOVE_DIRECTION_DOWN
                event.y < touchStartPoint -> MOVE_DIRECTION_UP
                else -> MOVE_DIRECTION_NONE
            }
        }
        when (initialMoveDirection) {
            MOVE_DIRECTION_DOWN -> moveDeepestPoint = Math.max(moveDeepestPoint, event.y)
            MOVE_DIRECTION_UP -> moveDeepestPoint = Math.min(moveDeepestPoint, event.y)
        }
        isMoving = true
    }

    private fun onTouchStart(event: MotionEvent) {
        touchStartPoint = event.y
        moveDeepestPoint = touchStartPoint
    }

    abstract fun onGesture(gesture: Gesture)
}