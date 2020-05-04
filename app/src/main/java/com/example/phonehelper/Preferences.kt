package com.example.phonehelper

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import com.example.phonehelper.Gesture.*

class Preferences(private val context: Context) {

    companion object {
        //TODO keys
    }

    private val DEFAULT_EDGE_WIDTH = context.resources.getDimensionPixelOffset(R.dimen.default_edge_width)

    val isShareMediaLockedEnabled: Boolean
        get() = true //TODO

    val isRightEdgeEnabled: Boolean
        get() = true    //TODO

    val isLeftEdgeEnabled: Boolean
        get() = true    //TODO

    val leftEdgeWidth: Int
        get() = if (isLeftEdgeEnabled) DEFAULT_EDGE_WIDTH else 0 //TODO

    val rightEdgeWidth: Int
        get() = if (isRightEdgeEnabled) DEFAULT_EDGE_WIDTH else 0 //TODO

    val rightEdgeHeight: Int
        get() = 600.toPx(context)

    val leftEdgeHeight: Int
        get() = 600.toPx(context)

    val leftEdgeYPos: Int
        get() = context.screenSize.height - leftEdgeHeight  //TODO

    val rightEdgeYPos: Int
        get() = 0   //TODO

    fun getLeftEdgeAction(gesture: Gesture): Action? {
        //TODO
        return when (gesture) {
            FLING_UP -> Action.OPEN_NAV_DRAWER
            FLING_DOWN -> null
            SCRUB -> null
            DOUBLE_TAP -> null
        }
    }

    fun getRightEdgeAction(gesture: Gesture): Action? {
        //TODO
        return when (gesture) {
            FLING_UP -> Action.VOLUME_UP
            FLING_DOWN -> Action.VOLUME_DOWN
            SCRUB -> null
            DOUBLE_TAP -> Action.OPEN_NAV_DRAWER
        }
    }

}


