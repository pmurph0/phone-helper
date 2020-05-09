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


    fun getActionForEdge(edge: Edge, gesture: Gesture): Map<String, Action>? {
        return HashMap<String, Action>().apply {
            when (edge) {
                Edge.LEFT -> when (gesture) {
                    Gesture.FLING_UP -> put(AppIds.ALL_OTHER, Action.OPEN_NAV_DRAWER)
                    Gesture.FLING_DOWN -> null  //do nothing
                    Gesture.SCRUB -> put(AppIds.CAMERA, Action.SWITCH_CAMERA)
                    Gesture.DOUBLE_TAP -> null  //do nothing
                }
                Edge.RIGHT -> when (gesture) {
                    Gesture.FLING_UP -> {
                        put(AppIds.ALL_OTHER, Action.VOLUME_UP)
                        put(AppIds.CAMERA, Action.CAMERA_CAPTURE)
                    }
                    Gesture.FLING_DOWN -> {
                        put(AppIds.ALL_OTHER, Action.VOLUME_DOWN)
                        put(AppIds.CAMERA, Action.CAMERA_CAPTURE)
                    }
                    Gesture.SCRUB -> {
                        put(AppIds.ALL_OTHER, Action.OPEN_NAV_DRAWER)
                        put(AppIds.CAMERA, Action.SWITCH_CAMERA)
                    }
                    Gesture.DOUBLE_TAP -> {
                        put(AppIds.ALL_OTHER, Action.OPEN_NAV_DRAWER)
                    }
                }
            }
        }

    }

}


