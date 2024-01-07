package com.example.rtse.ui.launch.viewmodels

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


private fun playAudio(clickedURL: String) {
    var mAudioTrack: AudioTrack? = null
    var mPlayThread: Thread? = null
    val tBufferSize = 512

    // Initialize AudioTrack for audio playback
    val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    mAudioTrack = AudioTrack.Builder()
        .setAudioAttributes(audioAttributes)
        .setAudioFormat(
            AudioFormat.Builder()
                .setSampleRate(16000)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()
        )
        .setBufferSizeInBytes(tBufferSize)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    // Start a thread for audio playback
    mPlayThread = Thread {
        val writeData = ByteArray(tBufferSize)
        var fis: FileInputStream? = null

        try {
            fis = FileInputStream(clickedURL)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        val dis = DataInputStream(fis)

        mAudioTrack?.play()
        val record_audio_len = 0
        val isPlaying = true
//        btn_record_play.setImageDrawable(
//            ResourcesCompat.getDrawable(resources, R.drawable.btn_play_clicked, null)
//        )

        while (isPlaying) {
            try {
                val ret = dis.read(writeData, 0, tBufferSize)

                // Process audio data here (e.g., apply speech enhancement)

                if (ret <= 0) {
//                    runOnUiThread {
//                        isPlaying = false
//                        btn_record_play.setImageDrawable(
//                            ResourcesCompat.getDrawable(
//                                resources,
//                                R.drawable.btn_original_play,
//                                null
//                            )
//                        )
//                    }
                    break
                }

                mAudioTrack?.write(writeData, 0, ret)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        mAudioTrack?.stop()
        mAudioTrack?.release()
        mAudioTrack = null

        try {
            dis.close()
            fis?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    mPlayThread?.start()

    mAudioTrack?.setPlaybackPositionUpdateListener(object :
        AudioTrack.OnPlaybackPositionUpdateListener {
        override fun onMarkerReached(audioTrack: AudioTrack) {
            // Handle marker reached
        }

        override fun onPeriodicNotification(audioTrack: AudioTrack) {
            // Handle periodic notification
        }
    })

}

