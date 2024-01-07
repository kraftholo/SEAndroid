package com.example.rtse.ui.launch.events

sealed class AudioTrackEvent {

    data class Init(
        val denoisedArray: FloatArray
    ) : AudioTrackEvent()

    object Play: AudioTrackEvent()


    object Pause : AudioTrackEvent()

    object Stop: AudioTrackEvent()

    data class SeekTo(
        val position : Int = 0
    ) : AudioTrackEvent()

    data class Release(
        val placeholder: Int
    ) : AudioTrackEvent()

}