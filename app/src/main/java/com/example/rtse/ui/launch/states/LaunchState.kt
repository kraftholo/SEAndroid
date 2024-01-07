package com.example.rtse.ui.launch.states

import com.example.rtse.domain.AudioFile
import com.example.rtse.domain.AudioProcessingState
import com.example.rtse.domain.LoadingFileState
import com.example.rtse.domain.ProgressBarState

data class LaunchState(
    val loadingFilesState : LoadingFileState = LoadingFileState.InProgress,
    val loadFilesProgBarS: ProgressBarState = ProgressBarState.Loading,
    val audioFiles : ArrayList<AudioFile> = arrayListOf(),

    val selectedAudioFile: AudioFile? = null,

    val mediaPlayerState: MediaPlayerState? = null,

    val loadAudioArrayProgBarS : ProgressBarState =  ProgressBarState.Idle,
    val selectedFileFloatingArray : ArrayList<Float>? = null,

    val enhancementProgBarS : ProgressBarState =  ProgressBarState.Idle,

    val audioProcessingState : AudioProcessingState? = null,
)

