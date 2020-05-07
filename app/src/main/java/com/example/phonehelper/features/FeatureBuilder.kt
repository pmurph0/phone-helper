package com.example.phonehelper.features

import android.accessibilityservice.AccessibilityService
import com.example.phonehelper.*
import com.example.phonehelper.features.Action.*
import com.example.phonehelper.features.edge.EdgeFeature
import com.example.phonehelper.features.edge.OpenNavDrawerFeature
import com.example.phonehelper.features.edge.VolumeDownFeature
import com.example.phonehelper.features.edge.VolumeUpFeature
import com.example.phonehelper.features.integrated.IntegratedFeature
import com.example.phonehelper.features.integrated.sharemedia.ShareMediaLockedFeature
import java.util.ArrayList

class FeatureBuilder(
    private val preferences: Preferences,
    private val accessibilityService: AccessibilityService
) {
    fun buildIntegratedFeatures(): List<IntegratedFeature> {
        return ArrayList<IntegratedFeature>().apply {
            if (preferences.isShareMediaLockedEnabled) {
                add(ShareMediaLockedFeature(accessibilityService))
            }
        }
    }

    fun buildEdgeFeatures(): Map<EdgeGestureTrigger, EdgeFeature> {
        return HashMap<EdgeGestureTrigger, EdgeFeature>().apply {
            Gesture.values().forEach { gesture ->
                preferences.getLeftEdgeAction(gesture)?.let {
                        action -> put(
                    EdgeGestureTrigger(
                        Edge.LEFT,
                        gesture
                    ), buildFeatureForAction(action))
                }
                preferences.getRightEdgeAction(gesture)?.let {
                        action -> put(
                    EdgeGestureTrigger(
                        Edge.RIGHT,
                        gesture
                    ), buildFeatureForAction(action))
                }
            }
        }
    }

    private fun buildFeatureForAction(action: Action): EdgeFeature {
        return when(action) {
            OPEN_NAV_DRAWER -> OpenNavDrawerFeature(
                accessibilityService,
                preferences
            )
            VOLUME_UP -> VolumeUpFeature(
                context = accessibilityService
            )
            VOLUME_DOWN -> VolumeDownFeature(
                context = accessibilityService
            )
        }
    }
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