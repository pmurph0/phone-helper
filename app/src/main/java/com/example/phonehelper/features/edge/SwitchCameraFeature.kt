package com.example.phonehelper.features.edge

import android.accessibilityservice.AccessibilityService
import com.example.phonehelper.clickView

class SwitchCameraFeature(private val accessibilityService: AccessibilityService): EdgeFeature {

    companion object {
        private const val SWITCH_CAMERA_BUTTON_ID = "com.oneplus.camera:id/switch_camera_button"
    }

    override fun onActionTriggered() {
        accessibilityService.clickView(SWITCH_CAMERA_BUTTON_ID)
    }
}