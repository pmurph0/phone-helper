package com.example.phonehelper.features.edge

interface EdgeFeature {
    fun onActionTriggered()
}

enum class Gesture {
    FLING_UP,
    FLING_DOWN,
    SCRUB,
    DOUBLE_TAP
}
enum class Edge {
    LEFT,
    RIGHT
}
enum class Action {
    OPEN_NAV_DRAWER,
    VOLUME_UP,
    VOLUME_DOWN
}

data class EdgeGestureTrigger(val edge: Edge, val gesture: Gesture)