package com.example.rtse.ui.launch.states

import com.example.rtse.domain.AudioFile

data class MediaPlayerState(
    val isPlaying : Boolean = false,
    val progress : Int = 0,
    val duration : Int = 0,
    val audioFile: AudioFile? = null,
)
