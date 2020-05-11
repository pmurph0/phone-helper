package com.example.phonehelper.features.edge

import android.accessibilityservice.AccessibilityService

class OpenNotificationsDrawerFeature(private val accessibilityService: AccessibilityService): EdgeFeature {
    override fun onActionTriggered() {
        accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
    }
}