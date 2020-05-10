package com.example.phonehelper

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import com.example.phonehelper.features.FeatureBuilder
import com.example.phonehelper.features.edge.*
import com.example.phonehelper.features.integrated.IntegratedFeature


class PhoneHelperService : AccessibilityService(), EdgeOverlayViewManager.Listener {

    private lateinit var integratedFeatures: List<IntegratedFeature>
    private lateinit var edgeGestureFeatures: Map<EdgeGestureTrigger, EdgeFeature>
    private lateinit var edgeOverlayViewManager: EdgeOverlayViewManager

    private val preferences: Preferences by lazy { Preferences(this) }
    private val displayManager: DisplayManager get() = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    private val isScreenOn: Boolean get() {
        return displayManager.displays.any { it.state != Display.STATE_OFF }
    }

    private var currentApp: String = ""
        set(value) {
            log("currentApp is $value")
            integratedFeatures.forEach { it.onCurrentAppChanged(value) }
            field = value
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
            when (event.packageName) {
                AppIds.SYSTEM_UI -> {
                    if (!isScreenOn) currentApp = AppIds.SYSTEM_UI
                }
                this.packageName -> {}  //do nothing for our package
                currentApp -> {}    //do nothing if unchanged
                else -> currentApp = event.packageName?.toString() ?: currentApp
            }
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