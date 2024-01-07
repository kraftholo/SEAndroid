package com.example.rtse.domain.util

//This file contains arguments similar to the python script
data class Args(
    val nameModel : String,
    val sampleRate : Int = 16000,
    val frameLength: Int = 8064,     // This is the audio reading framelength
    val hopLengthFrame: Int = 8064,  // This is the audio reading hoplength
    val nfft : Int = 256,         // This is the FFT framesize
    val hopLengthFFT: Int = 63,

    val length : Int = 4,

) {
}