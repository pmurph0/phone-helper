package com.example.phonehelper

import android.content.Context
import com.example.phonehelper.features.edge.Action
import com.example.phonehelper.features.edge.Edge
import com.example.phonehelper.features.edge.Gesture

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

    fun getActionForEdge(edge: Edge, gesture: Gesture): Action? {
        return when (edge) {
            Edge.LEFT -> when (gesture) {
                Gesture.FLING_UP -> Action.OPEN_NAV_DRAWER
                Gesture.FLING_DOWN -> null
                Gesture.SCRUB -> null
                Gesture.DOUBLE_TAP -> null
            }
            Edge.RIGHT -> when (gesture) {
                Gesture.FLING_UP -> Action.VOLUME_UP
                Gesture.FLING_DOWN -> Action.VOLUME_DOWN
                Gesture.SCRUB -> Action.OPEN_NAV_DRAWER
                Gesture.DOUBLE_TAP -> Action.OPEN_NAV_DRAWER
            }
        }
    }

}


