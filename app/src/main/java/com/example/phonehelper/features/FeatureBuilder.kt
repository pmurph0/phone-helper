package com.example.phonehelper.features

import android.accessibilityservice.AccessibilityService
import com.example.phonehelper.Preferences
import com.example.phonehelper.features.edge.*
import com.example.phonehelper.features.edge.Action.*
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
                    preferences.getActionForEdge(edge, gesture)?.let { appActionMap ->
                        appActionMap.keys.forEach { app ->
                            val trigger = EdgeGestureTrigger(edge, gesture, app)
                            put(trigger, buildFeatureForAction(appActionMap.getValue(app)))
                        }
                    }
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
            SWITCH_CAMERA -> SwitchCameraFeature(accessibilityService)
            CAMERA_CAPTURE -> CaptureImageCameraFeature(accessibilityService)
            OPEN_NOTIFICATIONS_DRAWER -> OpenNotificationsDrawerFeature(accessibilityService)
        }
    }
}