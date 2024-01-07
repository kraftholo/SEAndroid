package com.example.rtse.domain.util

import android.util.Log
import com.example.rtse.domain.AudioFile
import com.example.rtse.domain.Constants
import com.jlibrosa.audio.JLibrosa
import com.jlibrosa.audio.process.AudioFeatureExtraction
import kotlin.math.abs
import kotlin.math.ceil

class AudioProcessingPy(
    val audioFile : AudioFile,
    val args: Args
) {

    private val jLibrosa: JLibrosa = JLibrosa()
    private val preProcessedAudioSegments = arrayListOf<FloatArray>()

    init {

    }

    fun preProcessNoisyAudio() : ArrayList<FloatArray> {
        Log.d(Constants.TAG, "AudioProcessingPy.preProcessNoisyAudio():")
        jLibrosa.sampleRate = args.sampleRate

        //TODO: readFile : samplerate 16kHz fixed for now
        val noisyFilePath = audioFile.filePath
        val readArray = jLibrosa.loadAndRead(noisyFilePath,args.sampleRate,-1)
        Log.d(Constants.TAG, "AudioProcessingPy.preProcessNoisyAudio(): readArray size = ${readArray.size}")

        //Normalize
        val maxVal = readArray.max()
        val floatingAudioArray = readArray.map { it / abs(maxVal) }.toFloatArray()

        //Make segments
        val segmentLength = args.length * args.sampleRate
        val numSegments = ceil(floatingAudioArray.size.toDouble() / segmentLength).toInt()
        var lastIndex = -1
        for (start in floatingAudioArray.indices step segmentLength){
            if(start + segmentLength >= floatingAudioArray.size) break

            val slicedData = floatingAudioArray.slice(start until (start + segmentLength))
            preProcessedAudioSegments.add(slicedData.toFloatArray())
            lastIndex = start+segmentLength
        }

        //The last segment has audio samples less than required, so we pad
        if(numSegments != preProcessedAudioSegments.size){
            Log.d(Constants.TAG, "AudioProcessingPy.preProcessNoisyAudio():Padding the last segment")
            val lastSegment = FloatArray(segmentLength.toInt())
            val numAudioSamples = floatingAudioArray.size - lastIndex - 1
            lastSegment.fill(0.0f)

            for(segmentIndex in 0..numAudioSamples){
                val audioIndex = lastIndex + segmentIndex
                lastSegment[segmentIndex] = floatingAudioArray[audioIndex]
            }
            preProcessedAudioSegments.add(lastSegment)
        }
        return preProcessedAudioSegments

    }

    fun postProcessAudio(inferredAudio : ArrayList<FloatArray>) : FloatArray{
        Log.d(Constants.TAG, "AudioProcessingPy.postProcessAudio(): inferredAudio size = ${inferredAudio.size}")
        val combinedArray = arrayListOf<Float>()
        for(outputAudioSegment in inferredAudio){
            combinedArray.addAll(outputAudioSegment.toList())
        }

        //Normalizing
        val maxVal = combinedArray.max()
        val retArray = combinedArray.map {it / abs(maxVal)}.toFloatArray()

        return retArray
    }

}