package com.example.phonehelper

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

abstract class IntegratedFeature(protected val accessibilityService: AccessibilityService) {

    abstract fun onServiceConnected()
    abstract fun onAccessibilityEvent(event: AccessibilityEvent)

}