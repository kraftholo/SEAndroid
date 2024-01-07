package com.example.rtse.domain

import com.example.rtse.domain.util.AudioProcessingPy


data class AudioProcessingState(

    //Offloads the reading and processing of audio to a processor class
    val audioProcessor: AudioProcessingPy ?= null,

    //Pre Processing
    val isPreprocessing : Boolean = false,
    val inputToModel: ArrayList<FloatArray> ? = null,

    //Inference
    val outputFromModel: ArrayList<FloatArray>? = null,
    val isInferring : Boolean = false,

    //Post Processing
    val isPostProcessing : Boolean = false,
)