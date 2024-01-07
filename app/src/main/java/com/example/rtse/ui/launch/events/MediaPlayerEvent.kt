package com.example.rtse.ui.launch.events

import android.content.Context
import com.example.rtse.domain.AudioFile


sealed class MediaPlayerEvent{
    // Play, Pause, Seek , Stop

    data class Init(
        val context : Context,
        val audioFile: AudioFile
    ) : MediaPlayerEvent()

    object Play: MediaPlayerEvent()

    object Pause: MediaPlayerEvent()

    data class SeekTo(
        val position : Int = 0
    ) : MediaPlayerEvent()

    object Release: MediaPlayerEvent()

}