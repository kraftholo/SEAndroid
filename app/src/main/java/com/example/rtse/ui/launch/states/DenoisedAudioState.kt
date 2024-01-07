package com.example.rtse.ui.launch.states

import com.example.rtse.domain.AudioFile


data class DenoisedAudioState(
    val isPlaying : Boolean = false,
    val progress : Int = 0,
    val duration : Int = 0,
    val displayName: String? = null,
    val denoisedArray : FloatArray? = null,
    val audioFile: AudioFile ?= null,
)