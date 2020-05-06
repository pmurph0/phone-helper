package com.example.phonehelper

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.phonehelper.features.edge.EdgeFeature
import com.example.phonehelper.features.EdgeGestureTrigger
import com.example.phonehelper.features.FeatureBuilder
import com.example.phonehelper.features.edge.EdgeOverlayViewManager
import com.example.phonehelper.features.integrated.IntegratedFeature


class AccessibilityService : AccessibilityService(), EdgeOverlayViewManager.Listener {

    private lateinit var integratedFeatures: List<IntegratedFeature>
    private lateinit var edgeGestureFeatures: Map<EdgeGestureTrigger, EdgeFeature>
    private lateinit var edgeOverlayViewManager: EdgeOverlayViewManager

    private val preferences: Preferences by lazy { Preferences(this) }

    override fun onInterrupt() {
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        edgeOverlayViewManager =
            EdgeOverlayViewManager(
                this,
                preferences,
                this
            )

        val featureBuilder =
            FeatureBuilder(preferences, this)
        integratedFeatures = featureBuilder.buildButtonFeatures()
        edgeGestureFeatures = featureBuilder.buildEdgeFeatures()

        integratedFeatures.forEach {
            it.onServiceConnected()
        }
        edgeOverlayViewManager.addEdgeOverlayViews()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        integratedFeatures.forEach {
            it.onAccessibilityEvent(event)
        }
    }


    override fun onEdgeGestureTriggered(gestureTrigger: EdgeGestureTrigger) {
        edgeGestureFeatures[gestureTrigger]?.onActionTriggered()
    }



}