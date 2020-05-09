package com.example.phonehelper

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.phonehelper.features.FeatureBuilder
import com.example.phonehelper.features.edge.*
import com.example.phonehelper.features.integrated.IntegratedFeature


class AccessibilityService : AccessibilityService(), EdgeOverlayViewManager.Listener {


    private lateinit var integratedFeatures: List<IntegratedFeature>
    private lateinit var edgeGestureFeatures: Map<EdgeGestureTrigger, EdgeFeature>
    private lateinit var edgeOverlayViewManager: EdgeOverlayViewManager

    private val preferences: Preferences by lazy { Preferences(this) }

    private var currentApp: String = ""
        set(value) {
            field = value
            log("current app: $value")
        }

    override fun onInterrupt() {
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        edgeOverlayViewManager = EdgeOverlayViewManager(this, preferences, this)
        edgeOverlayViewManager.addEdgeOverlayViews()

        val featureBuilder = FeatureBuilder(preferences, this)
        integratedFeatures = featureBuilder.buildIntegratedFeatures()
        edgeGestureFeatures = featureBuilder.buildEdgeFeatures()

        integratedFeatures.forEach {
            it.onServiceConnected()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            currentApp = event.packageName?.toString()?.takeIf { it != AppIds.SYSTEM_UI } ?: currentApp
        }

        integratedFeatures.forEach {
            it.onAccessibilityEvent(event)
        }
    }


    override fun onEdgeGestureTriggered(edge: Edge, gesture: Gesture) {
        if (edgeGestureFeatures.keys.map { it.app }.any { app -> app == currentApp }) {
            val currentAppAction = edgeGestureFeatures[EdgeGestureTrigger(edge, gesture, currentApp)]
            if (currentAppAction != null) {
                currentAppAction.onActionTriggered()
            } else {
                edgeGestureFeatures[EdgeGestureTrigger(edge, gesture, AppIds.ALL_OTHER)]?.onActionTriggered()
            }
        } else {
            edgeGestureFeatures[EdgeGestureTrigger(edge, gesture, AppIds.ALL_OTHER)]?.onActionTriggered()
        }
    }
}