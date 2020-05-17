package com.example.phonehelper.features.edge

// An EdgeFeature is a single action that is triggered by an edge gesture.
// The trigger is managed externally.
interface EdgeFeature {
    fun onActionTriggered()
}

enum class Gesture {
    FLING_UP,
    FLING_DOWN,
    SCRUB,
    DOUBLE_TAP,
    LONG_DRAG_DOWN
}
enum class Edge {
    LEFT,
    RIGHT
}
enum class Action {
    OPEN_NAV_DRAWER,
    VOLUME_UP,
    VOLUME_DOWN,
    SWITCH_CAMERA,
    CAMERA_CAPTURE,
    OPEN_NOTIFICATIONS_DRAWER
}


data class EdgeGestureTrigger constructor(val edge: Edge, val gesture: Gesture, val app: String)