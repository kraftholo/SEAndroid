package com.example.rtse.ui.launch.events

import android.content.Context
import com.example.rtse.domain.AudioFile
import com.example.rtse.domain.WavFileInfo
import com.example.rtse.domain.util.Args

//Covers all the events possible on the launch screen
sealed class LaunchEvent {

    data class LoadFileList(
        val loadFiles: () -> (ArrayList<AudioFile>)
    ) : LaunchEvent()

    data class SelectAudioFile(
        val audioFile: AudioFile
    ) : LaunchEvent()

    data class LoadInAudioFile(
        val wavFileInfo: WavFileInfo
    ) : LaunchEvent()

    object StopLoadingFile: LaunchEvent()

    data class Enhance(
        val enhance : Boolean
    ) : LaunchEvent()

    //Model related events
    data class LoadPytorchModel(
        val ptMobileModelName : String,
        val args : Args,
        val context: Context
    ): LaunchEvent()

    data class LoadFilesForInferenceTest(
        val loadFiles: () -> (ArrayList<AudioFile>)
    ) : LaunchEvent()

}

