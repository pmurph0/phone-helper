package com.example.phonehelper.features.edge

import android.accessibilityservice.AccessibilityService
import com.example.phonehelper.clickView

class CaptureImageCameraFeature(private val accessibilityService: AccessibilityService): EdgeFeature {

    companion object {
        const val CAPTURE_BUTTON_ID = "com.oneplus.camera:id/primary_button_background"
    }

    override fun onActionTriggered() {
        accessibilityService.clickView(CAPTURE_BUTTON_ID)
    }

}