package com.example.phonehelper.features.edge

import android.content.Context
import android.media.AudioManager
import com.example.phonehelper.features.edge.EdgeFeature

//TODO extract base class
class VolumeUpFeature(private val context: Context):
    EdgeFeature {
    private val audioManager get() = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun onActionTriggered() {
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }

}