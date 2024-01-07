//package com.example.rtse.domain.util
//
//import android.util.Log
//import com.example.rtse.domain.AudioFile
//import com.example.rtse.domain.Constants
//import com.jlibrosa.audio.JLibrosa
//import com.jlibrosa.audio.process.AudioFeatureExtraction
//import org.apache.commons.math3.complex.Complex
//import org.tensorflow.lite.DataType
//import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
//import java.nio.ByteBuffer
//import java.nio.FloatBuffer
//import kotlin.math.cos
//import kotlin.math.ln
//import kotlin.math.max
//import kotlin.math.sin
//
////Names of the functions in this class are kept similar to the ones present in the tensorflow python implementation to avoid confusion
//
//class `AudioProcessing(NotUsed)`(
//    val audioFile : AudioFile,
//    val args: Args
//) {
//
////  jLibrosa's functions all initialize their own AudioFeatureExtraction() object
//    private val jLibrosa: JLibrosa = JLibrosa()
//    private val helperProcessor = AudioFeatureExtraction()
//
//    //For a single noisy audio file
//    private val arrayOfSTFTMags = arrayListOf<Array<DoubleArray>>()
//    private val arrayOfSTFTPhases = arrayListOf<Array<Array<Complex>>>()
//    private var numberOfSTFTFrames = -1
//
//    init {
////      Initialize helper
//        helperProcessor.n_fft = args.nfft
//        helperProcessor.hop_length = args.hopLengthFFT
//        helperProcessor.sampleRate = args.sampleRate.toDouble()
//    }
//
//    //X_in = X_in.reshape(X_in.shape[0],X_in.shape[1],X_in.shape[2],1)
//    //X_in.shape[0] -> arrayOfSTFTMags.size
//    //X_in.shape[1] -> arrayOfSTFTMags[0].size
//    //X_in.shape[1] -> arrayOfSTFTMags[0][0].size
//    fun getInputToModel(): ByteBuffer {
//        Log.d(Constants.TAG, "AudioProcessing.getInputToModel():")
//        // 4 times ( for float) x NumOfSTFTFrames x STFTFrameHeight x STFT FrameWidth
////        val byteBuffer : ByteBuffer = ByteBuffer.allocate(4*(arrayOfSTFTMags.size*arrayOfSTFTMags[0].size*arrayOfSTFTMags[0][0].size))
//
//        val byteBuffer: ByteBuffer =  ByteBuffer.allocate(65536)
//        val inputTensorBuffer: TensorBuffer = TensorBuffer.createDynamic(DataType.FLOAT32)
////        val inpShapeDim: IntArray = intArrayOf(1,arrayOfSTFTMags[0].size,arrayOfSTFTMags[0][0].size,1)
//
//        val inpShapeDim: IntArray = intArrayOf(1,arrayOfSTFTMags[0].size-1,arrayOfSTFTMags[0][0].size-1,1)
//
//        for(i in 0 until arrayOfSTFTMags.size){
//            //Inside 1 stft frame now
//            if(i ==1) break
//
//            val singleStftFrame = arrayOfSTFTMags[i]
//
//            //List<FloatBuffer!>
//            val singleSTFTFrameFloatBufferList = singleStftFrame.map { doubleArray -> FloatBuffer.wrap(doubleArray.map { value -> value.toFloat() }.toFloatArray())}
//
//            //Made a float buffer to have ALL info from the singleSTFT frame
////            val totalSize = singleSTFTFrameFloatBufferList.sumOf { it.capacity() * 4}
//
//            val totalSize = 65536
//            val singleFrameByteBuffer = ByteBuffer.allocateDirect(totalSize)
//
//
////            for (floatBuffer in singleSTFTFrameFloatBufferList) {
////                val floatView = singleFrameByteBuffer.asFloatBuffer()
////                floatView.put(floatBuffer)
////            }
//
//            for (index in singleSTFTFrameFloatBufferList.indices) {
//                if(index == singleSTFTFrameFloatBufferList.size -1) break
//
//                val floatBuffer = singleSTFTFrameFloatBufferList[index]
//                val floatView = singleFrameByteBuffer.asFloatBuffer()
//                floatView.put(floatBuffer)
//            }
//
//            //Put in all the info of 1 stft in the tensorbuffer
//            inputTensorBuffer.loadBuffer(singleFrameByteBuffer,inpShapeDim)
//
//            //Then put tensorbuffer into the main byte buffer
//            val valInBuffer : ByteBuffer = inputTensorBuffer.buffer
//            byteBuffer.put(valInBuffer)
//        }
//
//        byteBuffer.rewind()
//        return byteBuffer
//
//    }
//
//    fun preProcessNoisyAudio(): ByteBuffer {
//        Log.d(Constants.TAG, "AudioProcessing.preProcessNoisyAudio():")
//        //audio_file_to_numpy()
//        jLibrosa.sampleRate = args.sampleRate
//        val noisyFilePath = audioFile.filePath
//        val floatingAudioArray = jLibrosa.loadAndRead(noisyFilePath,args.sampleRate,-1)
//        Log.d(Constants.TAG, "AudioProcessing.audioFileToNumpy(): floatingAudioArray size = ${floatingAudioArray.size}")
//
//        val sequenceSampleLength = floatingAudioArray.size
//        val soundDataList = arrayListOf<FloatArray>()
//
////      The noisyFile is chopped into model specific frames
//        for (start in 0 until sequenceSampleLength - args.frameLength + 1 step args.hopLengthFrame) {
//            // Your code inside the loop
//            val slicedData = floatingAudioArray.slice(start until (start + args.frameLength))
//            soundDataList.add(slicedData.toFloatArray())
//        }
//        Log.d(Constants.TAG, "AudioProcessing.audioFileToNumpy(): Number of audio frames = ${soundDataList.size}")
//        numberOfSTFTFrames = soundDataList.size
//
//        val dimSquareSpec: Int = args.nfft/2 + 1
//        Log.d(Constants.TAG, "AudioProcessing.audioFileToNumpy(): dimSquareSpec = $dimSquareSpec")
//
//        //numpy_audio_to_matrix_spectrogram()
//        numpyAudioToMatrixSpectrogram(soundDataList)
//
//        val processedInput = getInputToModel()
//        return processedInput
//
//    }
//
//    fun postProcessAudio(modelOutput: TensorBuffer) : ArrayList<FloatArray>{
//        Log.d(Constants.TAG, "AudioProcessing.postProcessAudio():")
//
//        val outputByteBuffer = modelOutput.buffer
//        val outputFloatBuffer = outputByteBuffer.asFloatBuffer()
//        val floatArray = FloatArray(outputFloatBuffer.capacity())
//
//        // Copy data from the FloatBuffer to the FloatArray
//        outputFloatBuffer.get(floatArray)
//
//        return arrayListOf(FloatArray(10))
//
//
////        invScaledOut()
////        matrixSpectrogramToNumpyAudio()
//    }
//
//    //PreProcesses all the audio frames
//    private fun numpyAudioToMatrixSpectrogram(soundDataList: ArrayList<FloatArray>) {
//        Log.d(Constants.TAG, "AudioProcessing.numpyAudioToMatrixSpectrogram():")
////      audio_to_magnitude_db_and_phase() functionality
//        for ( soundData: FloatArray in soundDataList){
//                //Dealing with one STFT frame
//
//                val stft = jLibrosa.generateSTFTFeatures(soundData,args.sampleRate,10,args.nfft,10,args.hopLengthFFT)
//
//                //librosa.magphase(stftaudio) implementation
//                val magnitudeVals = returnSpectrogramMag(stft)
//                val logMagnitudeVals = amplitudeToDb(magnitudeVals)
//                val phaseVals = returnSpectrogramPhase(stft)
//
//                //scaled_in(matrix_spec)
//                scaledIn(logMagnitudeVals)
//
//                arrayOfSTFTMags.add(logMagnitudeVals)
//                arrayOfSTFTPhases.add(phaseVals)
//            }
//        Log.d(Constants.TAG, "AudioProcessing.numpyAudioToMatrixSpectrogram(): All stft input frames processed")
//    }
//
//    //getting mag and phase
//    private fun returnSpectrogramMag(stftComplexValues: Array<Array<Complex>>): Array<DoubleArray> {
//        val rows = stftComplexValues.size
//        val cols = stftComplexValues[0].size
//        val mag = Array(rows) { DoubleArray(cols) }
//        for (i in 0 until rows) {
//            for (j in 0 until cols) {
//                mag[i][j] = stftComplexValues[i][j].abs()
//            }
//        }
//
//        return mag
//    }
//    private fun returnSpectrogramPhase(stftComplexValues: Array<Array<Complex>>) : Array<Array<Complex>>{
//        val rows = stftComplexValues.size
//        val cols = stftComplexValues[0].size
//        val phase = Array(rows) { Array(cols) { Complex(0.0,0.0) } }
//
//        for (i in 0 until rows) {
//            for (j in 0 until cols) {
//                val argument = stftComplexValues[i][j].argument
//                phase[i][j] = Complex(cos(argument), sin(argument))
//            }
//        }
//        return phase
//    }
//
//    //To convert to log magnitude spectrum
//    private fun amplitudeToDb(magnitudeVals: Array<DoubleArray>): Array<DoubleArray> {
//        val logSpec = powerToDb(magnitudeVals)
//        return logSpec
//    }
//    private fun powerToDb(stftMag: Array<DoubleArray>): Array<DoubleArray> {
//        val logSpec = Array(stftMag.size) { DoubleArray(stftMag[0].size) }
//        var maxValue = -100.0
//
//        for (i in stftMag.indices) {
//            for (j in stftMag[i].indices) {
//                val magnitude = Math.abs(stftMag[i][j])
//                logSpec[i][j] = if (magnitude > 1.0E-10) {
//                    10.0 * ln(magnitude) / ln(10.0)
//                } else {
//                    -100.0
//                }
//                maxValue = max(maxValue, logSpec[i][j])
//            }
//        }
//
//        for (i in stftMag.indices) {
//            for (j in stftMag[i].indices) {
//                if (logSpec[i][j] < maxValue - 80.0) {
//                    logSpec[i][j] = maxValue - 80.0
//                }
//            }
//        }
//
//        return logSpec
//    }
//
//    //Global scaling helpers
//    fun scaledIn(singleStftArray: Array<DoubleArray>){
//        for (i in singleStftArray.indices) {
//            for (j in singleStftArray[i].indices) {
//                singleStftArray[i][j] = (singleStftArray[i][j]+46)/50
//            }
//        }
//    }
//
//    // inv_scaled_ou()
//    fun invScaledOut(){
//        //TODO: implement
//    }
//
//
//
//
//
//
//
//
////    private fun powerToDb(stftMag: Array<DoubleArray>): Array<DoubleArray> {
////        val log_spec = Array(stftMag.size) {
////            DoubleArray(
////                stftMag[0].size
////            )
////        }
////        var maxValue = -100.0
////        var j: Int
////        var i: Int = 0
////        while (i < stftMag.size) {
////            j = 0
////            while (j < stftMag[0].size) {
////                val magnitude = Math.abs(stftMag[i][j])
////                if (magnitude > 1.0E-10) {
////                    log_spec[i][j] = 10.0 * ln(magnitude) / ln(10.0)
////                } else {
////                    log_spec[i][j] = -100.0
////                }
////                if (log_spec[i][j] > maxValue) {
////                    maxValue = log_spec[i][j]
////                }
////                ++j
////            }
////            ++i
////        }
////        i = 0
////        while (i < stftMag.size) {
////            j = 0
////            while (j < stftMag[0].size) {
////                if (log_spec[i][j] < maxValue - 80.0) {
////                    log_spec[i][j] = maxValue - 80.0
////                }
////                ++j
////            }
////            ++i
////        }
////        return log_spec
////    }
//
//
//
//}