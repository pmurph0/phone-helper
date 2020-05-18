package com.example.phonehelper

import android.content.Context
import android.content.SharedPreferences
import com.example.phonehelper.features.edge.Action
import com.example.phonehelper.features.edge.Action.*
import com.example.phonehelper.features.edge.Edge
import com.example.phonehelper.features.edge.Edge.LEFT
import com.example.phonehelper.features.edge.Edge.RIGHT
import com.example.phonehelper.features.edge.Gesture
import com.example.phonehelper.features.edge.Gesture.*
import org.json.JSONObject

class Preferences(private val context: Context) {

    companion object {
        //TODO keys
        //TODO map app IDs?
        private val Edge.storageKey: String
            get() = when(this) {
                LEFT -> "LEFT"
                RIGHT -> "RIGHT"
            }

        private val Gesture.storageKey: String
            get() = when(this) {
                FLING_UP -> "FLING_UP"
                FLING_DOWN -> "FLING_DOWN"
                SCRUB -> "SCRUB"
                DOUBLE_TAP -> "DOUBLE_TAP"
                LONG_DRAG_DOWN -> "LONG_DRAG_DOWN"
            }

        private val Action.storageValue: String
            get() = when (this) {
                OPEN_NAV_DRAWER -> "OPEN_NAV_DRAWER"
                VOLUME_UP -> "VOLUME_UP"
                VOLUME_DOWN -> "VOLUME_DOWN"
                SWITCH_CAMERA -> "SWITCH_CAMERA"
                CAMERA_CAPTURE -> "CAMERA_CAPTURE"
                OPEN_NOTIFICATIONS_DRAWER -> "OPEN_NOTIFICATIONS_DRAWER"
            }

        private fun actionFromValue(storageValue: String): Action? {
            return when (storageValue) {
                OPEN_NOTIFICATIONS_DRAWER.storageValue -> OPEN_NOTIFICATIONS_DRAWER
                OPEN_NAV_DRAWER.storageValue -> OPEN_NAV_DRAWER
                VOLUME_UP.storageValue -> VOLUME_UP
                VOLUME_DOWN.storageValue -> VOLUME_DOWN
                SWITCH_CAMERA.storageValue -> SWITCH_CAMERA
                CAMERA_CAPTURE.storageValue -> CAMERA_CAPTURE
                else -> null
            }
        }

        private fun getKey(edge: Edge, gesture: Gesture): String {
            return "${edge.storageKey}_${gesture.storageKey}"
        }

        private fun SharedPreferences.Editor.putHashMap(key: String, map: Map<String, String>) {
            putString(key, JSONObject(map).toString())
        }

        private fun SharedPreferences.getHashMap(key: String): Map<String, String> {
            val outMap = hashMapOf<String, String>()
            val mapString = getString(key, JSONObject().toString())!!
            val jsonMap = JSONObject(mapString)
            jsonMap.keys().forEach { mapKey ->
                outMap[mapKey] = jsonMap[mapKey].toString()
            }
            return outMap
        }

        private fun Map<String, String>.toStringActionMap(): Map<String, Action> {
            return HashMap<String, Action>().also { stringActionMap ->
                keys.forEach { key ->
                    val action = actionFromValue(get(key) ?: "")
                    if (action != null) stringActionMap[key] = action
                }
            }
        }

    }


    private val sharedPreferences: SharedPreferences by lazy {
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
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

    fun init() {
//        if (sharedPreferences.all.isEmpty()) {
            putDefaultValues()
//        }
    }

    private fun putDefaultValues() {
        sharedPreferences.edit().apply {
            putHashMap(getKey(RIGHT, FLING_UP), mapOf(
                pairOf(AppIds.ALL_OTHER, VOLUME_UP.storageValue),
                pairOf(AppIds.CAMERA, CAMERA_CAPTURE.storageValue)
            ))
            putHashMap(getKey(RIGHT, FLING_DOWN), mapOf(
                pairOf(AppIds.ALL_OTHER, VOLUME_DOWN.storageValue),
                pairOf(AppIds.CAMERA, CAMERA_CAPTURE.storageValue)
            ))
            putHashMap(getKey(RIGHT, SCRUB), mapOf(
                pairOf(AppIds.ALL_OTHER, OPEN_NAV_DRAWER.storageValue),
                pairOf(AppIds.CAMERA, SWITCH_CAMERA.storageValue)
            ))
            putHashMap(getKey(RIGHT, LONG_DRAG_DOWN), mapOf(
                pairOf(AppIds.ALL_OTHER, OPEN_NOTIFICATIONS_DRAWER.storageValue)
            ))
            putHashMap(getKey(LEFT, FLING_UP), mapOf(
                pairOf(AppIds.ALL_OTHER, OPEN_NAV_DRAWER.storageValue)
            ))
            putHashMap(getKey(LEFT, LONG_DRAG_DOWN), mapOf(
                pairOf(AppIds.ALL_OTHER, OPEN_NOTIFICATIONS_DRAWER.storageValue)
            ))

            apply()
        }
    }

    fun getActionForEdge(edge: Edge, gesture: Gesture): Map<String, Action>? {
        return sharedPreferences.getHashMap(getKey(edge, gesture)).toStringActionMap()

    }

}


