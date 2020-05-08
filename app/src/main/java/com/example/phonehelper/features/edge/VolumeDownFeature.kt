package com.example.phonehelper.features.edge

import android.content.Context
import android.media.AudioManager
import com.example.phonehelper.features.edge.EdgeFeature

//TODO extract base class
class VolumeDownFeature(private val context: Context): EdgeFeature {
    private val audioManager get() = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun onActionTriggered() {
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
    }
}