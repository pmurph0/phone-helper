package com.example.phonehelper.features

import android.accessibilityservice.AccessibilityService
import com.example.phonehelper.Preferences
import com.example.phonehelper.features.edge.*
import com.example.phonehelper.features.integrated.IntegratedFeature
import com.example.phonehelper.features.integrated.sharemedia.ShareMediaLockedFeature
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.forEach

class FeatureBuilder(private val preferences: Preferences, private val accessibilityService: AccessibilityService) {

    fun buildIntegratedFeatures(): List<IntegratedFeature> {
        return ArrayList<IntegratedFeature>().apply {
            if (preferences.isShareMediaLockedEnabled) {
                add(ShareMediaLockedFeature(accessibilityService))
            }
        }
    }

    fun buildEdgeFeatures(): Map<EdgeGestureTrigger, EdgeFeature> {
        return HashMap<EdgeGestureTrigger, EdgeFeature>().apply {
            Edge.values().forEach { edge ->
                Gesture.values().forEach {gesture ->
                    preferences.getActionForEdge(edge, gesture)?.let { action ->
                        val trigger = EdgeGestureTrigger(edge, gesture)
                        put(trigger, buildFeatureForAction(action))
                    }
                }
            }
        }
    }

    private fun buildFeatureForAction(action: Action): EdgeFeature {
        return when(action) {
            Action.OPEN_NAV_DRAWER -> OpenNavDrawerFeature(
                accessibilityService,
                preferences
            )
            Action.VOLUME_UP -> VolumeUpFeature(
                context = accessibilityService
            )
            Action.VOLUME_DOWN -> VolumeDownFeature(
                context = accessibilityService
            )
        }
    }
}