package com.example.rtse.domain
import android.content.Context
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

//Class to contain data inside a single .wav file
data class WavFileInfo(
    val audioFile: AudioFile,
    val applicationContext: Context
)
{

    private val TAG = "WavFileInfo"
    //https://medium.com/@rizveeredwan/working-with-wav-files-in-android-52e9500297e
    // Looking like the canonical WAVE file format : http://soundfile.sapp.org/doc/WaveFormat/
    //The canonical WAVE format starts with the RIFF header:
    var chunkID = ""
    var chunkSize = 1
    var format = ""

    //The "fmt " sub-chunk describes the sound data's format:
    var subChunk1ID = ""
    var subChunk1Size = 1
    var audioFormat : Short = 8
    var numChannels : Short = 8
    var sampleRate = 1
    var byteRate = 1
    var blockAlign : Short = 8
    var bitsPerSample : Short = 8

    //The "data" sub-chunk contains the size of the data and the actual sound:
    var subChunk2ID = ""
    var subChunk2Size = 1
    var bytePerSample = 1

    // Number of bytes per field
    val numberOfBytes = arrayOf(4, 4, 4, 4, 4, 2, 2, 4, 4, 2, 2, 4, 4)

    // Endian Format : 0 (big), 1(small)
    val type = arrayOf(0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1)

    val audioData : FloatArray by lazy {
        normalizeAudioData(readAudioFile())
    }


    private fun readAudioFile(): ArrayList<Short> {
        val dataVector = mutableListOf<Short>()
        val resolver = applicationContext.contentResolver

        resolver.openInputStream(audioFile.uri)?.use { inputStream ->
            // Perform operations on "inputStream".
            var byteBuffer: ByteBuffer?
            //Start reading byte blocks
            for ((index, readByteSize) in numberOfBytes.withIndex()) {
                val byteArray = ByteArray(readByteSize)

                val read = inputStream.read(byteArray, 0, readByteSize)
                byteBuffer = byteArrayToNumber(byteArray, readByteSize, type[index])

                if (byteBuffer != null) {
                    when (index) {
                        0 -> {
                            chunkID = String(byteArray)
                            Log.d(TAG, "readAudioFile(): chunkID = $chunkID")
                        }

                        1 -> {
                            chunkSize = byteBuffer.int
                            Log.d(TAG, "readAudioFile(): chunkSize = $chunkSize")
                        }

                        2 -> {
                            format = String(byteArray)
                            Log.d(TAG, "readAudioFile(): format = $format")
                        }

                        3 -> {
                            subChunk1ID = String(byteArray)
                            Log.d(TAG, "readAudioFile(): subChunk1ID = $subChunk1ID")
                        }

                        4 -> {
                            subChunk1Size = byteBuffer.int
                            Log.d(TAG, "readAudioFile(): chunkSize = $subChunk1Size")
                        }

                        5 -> {
                            audioFormat = byteBuffer.short
                            Log.d(TAG, "readAudioFile(): audioFormat = $audioFormat")
                        }

                        6 -> {
                            numChannels = byteBuffer.short
                            Log.d(TAG, "readAudioFile(): numChannels = $numChannels")
                        }

                        7 -> {
                            sampleRate = byteBuffer.int
                            Log.d(TAG, "readAudioFile(): sampleRate = $sampleRate")
                        }

                        8 -> {
                            byteRate = byteBuffer.int
                            Log.d(TAG, "readAudioFile(): byteRate = $byteRate")
                        }

                        9 -> {
                            blockAlign = byteBuffer.short
                            Log.d(TAG, "readAudioFile(): blockAlign = $blockAlign")
                        }

                        10 -> {
                            bitsPerSample = byteBuffer.short
                            Log.d(TAG, "readAudioFile(): bitsPerSample = $bitsPerSample")
                        }

                        11 -> {
                            subChunk2ID = String(byteArray)
                            if (subChunk2ID == "data") {
                                Log.d(TAG, "readAudioFile(): subChunk2ID = $subChunk2ID")
                                continue
                            } else if (subChunk2ID == "LIST") {
                                val byteArray2 = ByteArray(4)
                                var r = inputStream.read(byteArray2, 0, 4)
                                byteBuffer = byteArrayToNumber(byteArray2, 0, 4)
                                val temp = byteBuffer!!.int

                                //redundant data reading
                                val byteArray3 = ByteArray(temp)
                                r = inputStream.read(byteArray3, 0, temp)
                                r = inputStream.read(byteArray2, 0, 4)
                                subChunk2ID = String(byteArray2)
                                Log.d(TAG, "readAudioFile(): subChunk2ID = $subChunk2ID")
                            }

                        }

                        12 -> {
                            subChunk2Size = byteBuffer.int
                            Log.d(TAG, "readAudioFile(): subChunk2Size = $subChunk2Size")
                        }
                    }
                }
            }

            bytePerSample = bitsPerSample / 8
            var value: Short
//            var debugI = 0
            while (true) {
//                if(debugI == 40000) break

                val bArray = ByteArray(bytePerSample)
                val v = inputStream.read(bArray, 0, bytePerSample)
                value = convertToShort(bArray)
//                Log.d(TAG,"readAudioFile(): $debugI th bArray = [${bArray[0]},${bArray[1]}] and convertedValue = $value")
//                Log.d(TAG,"readAudioFile(): $debugI th bArray convertedValue = ${value /32767f}")

                dataVector.add(value)
//                debugI++
                if (v == -1) break
            }

            Log.d(TAG, "readAudioFile(): dataVector.size = ${dataVector.size}")
        }

        Log.d(TAG, "readAudioFile(): returning data with size = ${dataVector.size}")
        return ArrayList(dataVector)
    }

    fun printByteArrayInBits(byteArray: ByteArray) {
        for (byte in byteArray) {
            for (i in 7 downTo 0) {
                val bit = (byte.toInt() shr i) and 1
                print(bit)
            }
            println() // Print a new line after each byte
        }
    }


    private fun byteArrayToNumber(bytes: ByteArray, numOfBytes: Int, type: Int): ByteBuffer? {
        val buffer = ByteBuffer.allocate(numOfBytes)
        if (type == 0) {
            buffer.order(ByteOrder.BIG_ENDIAN) // Check the illustration. If it says little endian, use LITTLE_ENDIAN
        } else {
            buffer.order(ByteOrder.LITTLE_ENDIAN)
        }
        buffer.put(bytes)
        buffer.rewind()
        return buffer
    }


    // Converting the 2 read bytes (16bits) to 16-bit integer values
    private fun convertToShort(array: ByteArray,byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN): Short {
        val buffer = ByteBuffer.wrap(array)
        buffer.order(byteOrder)
        return buffer.short
    }

    //Normalizing scheme like torchaudio
    private fun normalizeAudioData(shortArray: ArrayList<Short>) : FloatArray{
        val maxShortValue = 32767f // Maximum value for a 16-bit signed integer
        val scaleFactor = 1.0f / maxShortValue
        val retArray = shortArray.map { it.toFloat() * scaleFactor }.toFloatArray()

        return retArray
    }
}