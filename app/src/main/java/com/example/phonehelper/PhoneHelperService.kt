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
    private var currentApp  = ""
        set(value) {
            log("currentApp is $value")
            field = value
        }

    private val preferences: Preferences by lazy { Preferences(this) }

    private val isScreenOn: Boolean get() {
        val dm = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        for (display in dm.displays) {
            if (display.state != Display.STATE_OFF) {
                return true
            }
        }
        return false
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
        log("${event.eventType.mapToEventType()} ${event.packageName}")

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            when (event.packageName) {
                AppIds.SYSTEM_UI -> {
                    if (!isScreenOn) currentApp = AppIds.SYSTEM_UI
                }
                this.packageName -> {}  //do nothing for our package //TODO ?
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