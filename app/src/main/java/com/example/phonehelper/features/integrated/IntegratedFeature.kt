package com.example.phonehelper.features.integrated

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

//An integrated feature can listen to all AccessibilityService events and activate (including add/remove overlay UI) at any time.
// It basically attaches to the accessibility service and manages itself.
//TODO make this an interface?
abstract class IntegratedFeature(protected val accessibilityService: AccessibilityService) {

    abstract fun onServiceConnected()
    abstract fun onAccessibilityEvent(event: AccessibilityEvent)
    abstract fun onCurrentAppChanged(appId: String)

}