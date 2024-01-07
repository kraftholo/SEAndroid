package com.example.rtse.ui.launch.viewmodels


import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rtse.domain.AudioFile
import com.example.rtse.domain.AudioProcessingState
import com.example.rtse.domain.Constants
import com.example.rtse.domain.LoadingFileState
import com.example.rtse.domain.ProgressBarState
import com.example.rtse.domain.WavFileInfo
import com.example.rtse.domain.util.Args
import com.example.rtse.domain.util.AudioProcessingPy
import com.example.rtse.ui.launch.events.AudioTrackEvent
import com.example.rtse.ui.launch.events.LaunchEvent
import com.example.rtse.ui.launch.events.MediaPlayerEvent
import com.example.rtse.ui.launch.states.DenoisedAudioState
import com.example.rtse.ui.launch.states.LaunchState
import com.example.rtse.ui.launch.states.MediaPlayerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.pow


class LaunchViewModel: ViewModel() {

    val launchState: MutableState<LaunchState> = mutableStateOf(LaunchState())
    val mediaPlayerState: MutableState<MediaPlayerState> = mutableStateOf(MediaPlayerState())


    val audioProcessingState: MutableState<AudioProcessingState> = mutableStateOf(
        AudioProcessingState()
    )

    val denoisedAudioState: MutableState<DenoisedAudioState> = mutableStateOf(DenoisedAudioState())

    private lateinit var ptModule: Module
    private lateinit var ARGS: Args

    private val mediaPlayer = MediaPlayer()
    private var playingAudioFile: AudioFile? = null

    //This job is to load in the audio floating array
    private var audioArrayLoadingJob: Job? = null

    // This job is to do speech enhancement
    private var enhanceJob: Job? = null

    private var loadInModelJob: Job? = null

    private var testInferenceTimesJob: Job? = null

    //Variables for speech enhancement and AudioTrack
    private var SAMPLE_RATE : Int = 0
    private var mAudioTrack: AudioTrack? = null
    private var pcmBufferToPlay : ByteArray?= null
    private var minimumBufferSize: Int = 0

    private var currentPlayingIndex: Int = 0


    init {
    }

    //Functions triggered by events from compose UI =========================================================
    fun onTriggerLaunchEvent(event: LaunchEvent) {
        Log.d(Constants.TAG, "LaunchViewModel.onTriggerLaunchEvent(): $event")

        when (event) {

            is LaunchEvent.LoadFileList -> {
                loadInFilesByMediaStore(event.loadFiles)
            }

            is LaunchEvent.SelectAudioFile -> {
                selectAudioFile(event.audioFile)
            }

            is LaunchEvent.LoadInAudioFile -> {
                loadSelectedFile(event.wavFileInfo)
            }

            is LaunchEvent.StopLoadingFile -> {
                stopLoadingSelectedFile()
            }

            is LaunchEvent.Enhance -> {
                enhance(event.enhance)
            }

            is LaunchEvent.LoadPytorchModel -> {
                loadInModel(event.ptMobileModelName, event.args, event.context)
            }

            is LaunchEvent.LoadFilesForInferenceTest ->{
                performInferenceTest(event.loadFiles)
            }

            else -> {}
        }


    }

    fun onTriggerMediaPlayerEvent(event: MediaPlayerEvent) {
        Log.d(Constants.TAG, "LaunchViewModel.onTriggerMediaPlayerEvent(): $event")

        when (event) {

            is MediaPlayerEvent.Init -> {
                handleInit(event.context, event.audioFile)
            }

            is MediaPlayerEvent.Play -> {
                handlePlay()
            }

            is MediaPlayerEvent.Pause -> {
                handlePause()
            }

            is MediaPlayerEvent.SeekTo -> {
                handleSeekTo(event.position)
            }

            is MediaPlayerEvent.Release -> {
                handleRelease()
            }

            else -> {}
        }

    }

    fun onTriggerAudioTrackEvent(event: AudioTrackEvent) {
        Log.d(Constants.TAG, "LaunchViewModel.onTriggerDenoisedMediaPlayerEvent(): $event")

        when (event) {
            is AudioTrackEvent.Init -> {
                handleDenoisedAudioInit(event.denoisedArray)
            }

            is AudioTrackEvent.Play -> {
                handleDenoisedAudioPlay()
            }

            is AudioTrackEvent.Pause -> {
                handleDenoisedAudioPause()
            }

            is AudioTrackEvent.Stop -> {
                handleDenoisedAudioStop()
            }

            is AudioTrackEvent.SeekTo -> {
                handleDenoisedAudioSeekTo(event.position)
            }

            is AudioTrackEvent.Release -> {
//                handleDenoisedAudioRelease()
            }

            else -> {}
        }
    }

    private fun handleDenoisedAudioStop() {
        Log.d(Constants.TAG,"LaunchViewModel.handleDenoisedAudioStop(): pcmBufferToPlay.size = ${pcmBufferToPlay!!.size}")
        //For instant stop
        mAudioTrack!!.pause()
        mAudioTrack!!.flush()
    }

    // Functions regarding DenoisedAudio player events =============================================================
    private fun handleDenoisedAudioSeekTo(position: Int) {
    }

    private fun handleDenoisedAudioPause() {
        Log.d(Constants.TAG,"LaunchViewModel.handleDenoisedAudioPause(): pcmBufferToPlay.size = ${pcmBufferToPlay!!.size}")
        denoisedAudioState.value = denoisedAudioState.value.copy(isPlaying = false)
        mAudioTrack!!.pause()
    }

    private fun handleDenoisedAudioPlay() {
        Log.d(Constants.TAG,"LaunchViewModel.handleDenoisedAudioPlay(): pcmBufferToPlay.size = ${pcmBufferToPlay!!.size}")
        denoisedAudioState.value = denoisedAudioState.value.copy(isPlaying = false)
        mAudioTrack!!.play()

        mAudioTrack!!.write(pcmBufferToPlay!!,0,pcmBufferToPlay!!.size)

//        for (i in 0..pcmBufferToPlay!!.size) {
//            val playBytes = pcmBufferToPlay!!.slice(i until minimumBufferSize)
//            mAudioTrack!!.write(playBytes.toByteArray(), 0, minimumBufferSize)
//        }
    }

    private fun handleDenoisedAudioInit(denoisedArray: FloatArray) {
        Log.d(Constants.TAG,"LaunchViewModel.handleDenoisedAudioInit(): denoisedArray.size = ${denoisedArray.size}")
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()

        //get sample rate from the args
        SAMPLE_RATE = ARGS.sampleRate

        //This is the minimum playable buffer size
        minimumBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        );

        val pcmDenoisedArray = denoisedArray.map { (it * 32767f).toInt() }

        val byteBuffer = ByteBuffer.allocate(pcmDenoisedArray.size * 2)
        for (i in pcmDenoisedArray.indices) {
            val pcmValue = pcmDenoisedArray[i]
            val lsb = pcmValue.toByte()
            val msb = (pcmValue shr 8).toByte()
            byteBuffer.put(lsb)
            byteBuffer.put(msb)
        }

        byteBuffer.flip()

        //This will be played using AudioTrack
        pcmBufferToPlay = byteBuffer.array()

        mAudioTrack = AudioTrack.Builder().setAudioAttributes(audioAttributes).setAudioFormat(
            AudioFormat.Builder().setSampleRate(SAMPLE_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()
        ).setBufferSizeInBytes(pcmBufferToPlay!!.size).setTransferMode(AudioTrack.MODE_STREAM).build()

    }


    // Functions regarding mediaPlayerEvents==========================================================================
    private fun handleInit(ctx: Context, newAudioFile: AudioFile) {
        playingAudioFile?.let { currFile ->
            if (currFile.displayName == newAudioFile.displayName) {
                Log.d(Constants.TAG, "LaunchViewModel.handleInit(): Trying to reinit the same file")
                return
            }
        }

        Log.d(Constants.TAG, "LaunchViewModel.handleInit(): Init-ing new file :D ")
        mediaPlayer.reset()
        mediaPlayer.setDataSource(ctx, newAudioFile.uri)
        playingAudioFile = newAudioFile
        mediaPlayer.prepareAsync()

        mediaPlayer.setOnPreparedListener {
            mediaPlayerState.value = mediaPlayerState.value.copy(
                progress = 0, duration = newAudioFile.duration.toInt(), isPlaying = false
            )

            //Todo: This coroutine never launches btw!
            viewModelScope.launch(Dispatchers.IO) {
                Log.d(Constants.TAG, "LaunchViewModel.handleInit(): pushing current position")
                while (mediaPlayer.isPlaying) {
                    val currentPosition = mediaPlayer.currentPosition

                    withContext(Dispatchers.Main) {
                        mediaPlayerState.value =
                            mediaPlayerState.value.copy(progress = currentPosition)
                    }
                    delay(1000)
                }
            }
        }
        mediaPlayer.setOnCompletionListener {
            mediaPlayerState.value = mediaPlayerState.value.copy(isPlaying = false)

        }
        mediaPlayer.setOnBufferingUpdateListener { _, percent ->
            val durationMs = mediaPlayer.duration
            val newPosition = (percent / 100.0 * durationMs).toInt()
            mediaPlayerState.value = mediaPlayerState.value.copy(progress = newPosition)

        }

    }

    private fun handlePlay() {
        Log.d(Constants.TAG, "LaunchViewModel.handlePlay():")
        mediaPlayer.start()
        mediaPlayerState.value = mediaPlayerState.value.copy(isPlaying = true)
//        startProgressUpdater()
    }

    private fun handlePause() {
        Log.d(Constants.TAG, "LaunchViewModel.handlePause():")
        mediaPlayer.pause()
        mediaPlayerState.value = mediaPlayerState.value.copy(isPlaying = false)
    }

    private fun handleSeekTo(position: Int) {
        Log.d(Constants.TAG, "LaunchViewModel.handleSeekTo(): position = $position")
        mediaPlayer.seekTo(position)
        mediaPlayerState.value = mediaPlayerState.value.copy(progress = position)
    }

    private fun handleRelease() {
        Log.d(Constants.TAG, "LaunchViewModel.handleRelease():")
        mediaPlayer.release()
        mediaPlayerState.value = mediaPlayerState.value.copy(audioFile = null)
    }


    // Functions regarding launchEvents==========================================================================
    private fun loadInFilesByMediaStore(loadFiles: () -> ArrayList<AudioFile>) {
        val list = loadFiles()
        launchState.value = launchState.value.copy(
            loadingFilesState = LoadingFileState.Complete,
            loadFilesProgBarS = ProgressBarState.Idle,
            audioFiles = list
        )
    }

    private fun selectAudioFile(audioFile: AudioFile) {
        Log.d(Constants.TAG, "LaunchViewModel.selectAudioFile():")
        audioArrayLoadingJob?.cancel()
        enhanceJob?.cancel()

        launchState.value = launchState.value.copy(
            selectedAudioFile = audioFile,
            selectedFileFloatingArray = null,
            loadAudioArrayProgBarS = ProgressBarState.Idle,
            enhancementProgBarS = ProgressBarState.Idle
        )
        mediaPlayerState.value = mediaPlayerState.value.copy(audioFile = audioFile)
        audioProcessingState.value = audioProcessingState.value.copy(
            audioProcessor = null,
            isPreprocessing = false,
            inputToModel = null,
            outputFromModel = null,
            isInferring = false,
            isPostProcessing = false
        )

        denoisedAudioState.value = denoisedAudioState.value.copy(
            denoisedArray = null,
            audioFile = null
        )
    }

    //This function needs to run in the background for sure
    private fun loadSelectedFile(wavFileInfo: WavFileInfo) {
        Log.d(Constants.TAG, "LaunchViewModel.loadSelectedFile():")
        launchState.value =
            launchState.value.copy(loadAudioArrayProgBarS = ProgressBarState.Loading)

        // Launch a coroutine here that can be stopped as well
        audioArrayLoadingJob?.cancel()
        audioArrayLoadingJob = viewModelScope.launch(Dispatchers.IO) {
            Log.d(
                Constants.TAG,
                "LaunchViewModel.loadSelectedFile(): Coroutine here!, starting to load in audio array"
            )
            val retlist = ArrayList<Float>(wavFileInfo.audioData.asList())
            Log.d(
                Constants.TAG,
                "LaunchViewModel.loadSelectedFile(): Coroutine here!, loaded the array!"
            )

            withContext(Dispatchers.Main) {
                launchState.value = launchState.value.copy(
                    selectedFileFloatingArray = retlist,
                    loadAudioArrayProgBarS = ProgressBarState.Idle
                )
            }
        }

    }

    // This will stop the job if it's running
    private fun stopLoadingSelectedFile() {
        Log.d(Constants.TAG, "LaunchViewModel.stopLoadingSelectedFile():")
        audioArrayLoadingJob?.cancel() ?: Log.d(
            Constants.TAG, "LaunchViewModel.stopLoadingSelectedFile(): No job to cancel!"
        )
    }


    //Main function creating the denoising job and calling the pre,inference,post functions
    private fun enhance(enhance: Boolean) {
        Log.d(
            Constants.TAG,
            "LaunchViewModel.enhance(): audioArray.size() = ${launchState.value.selectedFileFloatingArray?.size ?: -1} and do enhancement = $enhance"
        )

        if (enhance) {
            //Do enhancement
            enhanceJob?.cancel()

            //Start Enhancement
            launchState.value =
                launchState.value.copy(enhancementProgBarS = ProgressBarState.Loading)
            audioProcessingState.value = audioProcessingState.value.copy(isPreprocessing = true)

            enhanceJob = viewModelScope.launch(Dispatchers.IO) {

                launchState.value.selectedAudioFile?.let {
                    startPreProcessingInput(it)
                } ?: {Log.e(Constants.TAG, "LaunchViewModel.enhance(): There's no selectedAudioFile!!")}

                audioProcessingState.value.inputToModel?.let { modelInput ->
                    runInference(modelInput, ptModule)
                } ?: {Log.e(Constants.TAG, "LaunchViewModel.enhance(): There's no inputToModel!!")}

                audioProcessingState.value.outputFromModel?.let { outputsFromModel ->
                    startPostProcessing(outputsFromModel)
                } ?: Log.e(Constants.TAG, "LaunchViewModel.enhance(): There's no outputFromModel!!")


                //There has to be some processing here to make the audio playable?

                launchState.value = launchState.value.copy(enhancementProgBarS = ProgressBarState.Idle)


            }

        } else {
            // Stop enhancement
            enhanceJob?.cancel()
            launchState.value = launchState.value.copy(enhancementProgBarS = ProgressBarState.Idle)
            audioProcessingState.value = audioProcessingState.value.copy(
                audioProcessor = null,
                isPreprocessing = false,
                isPostProcessing = false,
                isInferring = false,
                inputToModel = null,
                outputFromModel = null
            )
            denoisedAudioState.value = denoisedAudioState.value.copy(
                denoisedArray = null,
                audioFile = null
            )
        }

    }

    // Model related stuff ======================================================================================

    //This function loads in the model once on viewmodel init
    private fun loadInModel(ptMobileModelName: String, args: Args, context: Context) {
        //init args
        ARGS = args

        loadInModelJob?.cancel()
        loadInModelJob = viewModelScope.launch(Dispatchers.IO) {
            val assetFilePath = assetFilePath(context, ptMobileModelName)
            val module = LiteModuleLoader.load(assetFilePath);
            Log.d(
                Constants.TAG,
                "LaunchViewModel.loadInModel(): Success! ; module.toString() = $module"
            )
            ptModule = module
        }
    }

    //Takes in an audioFile
    //Reads it, normalizes it and splits it into serviceable chunks depending upon the ConvTasnet input length
    private fun startPreProcessingInput(audioFile: AudioFile) {
        Log.d(Constants.TAG, "LaunchViewModel.startPreProcessingInput():")

        //Start the prog bar in UI
        audioProcessingState.value = audioProcessingState.value.copy(
            isPreprocessing = true
        )

        val audioProcessor = AudioProcessingPy(audioFile, ARGS)
        val audioSegments = audioProcessor.preProcessNoisyAudio()

        audioProcessingState.value = audioProcessingState.value.copy(
            isPreprocessing = false, inputToModel = audioSegments, audioProcessor = audioProcessor
        )
    }

    private fun startPreProcessingInput_InferenceTest(audioFile: AudioFile) : ArrayList<FloatArray> {
        Log.d(Constants.TAG, "LaunchViewModel.startPreProcessingInput_InferenceTest():")

        val audioProcessor = AudioProcessingPy(audioFile, ARGS)
        return audioProcessor.preProcessNoisyAudio()
    }



    //Input: Takes in the audio segments and makes inference of each of them
    private fun runInference(inputAudioSegments: ArrayList<FloatArray>, module: Module) {
        Log.d(Constants.TAG, "LaunchViewModel.runInference():")

        audioProcessingState.value = audioProcessingState.value.copy(
            isInferring = true
        )

        val modelOutputs = arrayListOf<FloatArray>()
        var totalInferenceTime = 0L

        //Inference per segment
        for (segment in inputAudioSegments) {
            //Run inference for each segment!
            val inputTensor = Tensor.fromBlob(segment, longArrayOf(1, 1, segment.size.toLong()))
            val start = System.currentTimeMillis()
            val modelOutputTensor = module.forward(IValue.from(inputTensor)).toTensor()
            val timeElapsed = System.currentTimeMillis() - start
            totalInferenceTime+=timeElapsed
            Log.d(
                Constants.TAG, "LaunchViewModel.runInference(): inference times = $timeElapsed"
            )
            // elapsed time = 1.8-2 sec for a 4 sec audio clip

            val output = modelOutputTensor.dataAsFloatArray

            val cleanOP = output.slice(0 until 64000).toFloatArray()
            val noisyOP = output.slice(64000 until 128000).toFloatArray()
            modelOutputs.add(cleanOP)
        }

        Log.d(
            Constants.TAG, "LaunchViewModel.runInference(): totalInferenceTime = $totalInferenceTime"
        )

        //stop progbar
        audioProcessingState.value = audioProcessingState.value.copy(
            isInferring = false, outputFromModel = modelOutputs
        )

    }

    private fun runInference_InferenceTest(inputAudioSegments: ArrayList<FloatArray>, module: Module): Long {
        Log.d(Constants.TAG, "LaunchViewModel.runInference_InferenceTest():")

        audioProcessingState.value = audioProcessingState.value.copy(
            isInferring = true
        )

        val modelOutputs = arrayListOf<FloatArray>()
        var inferenceTime = 0L

        //Inference per segment
        for (segment in inputAudioSegments) {
            //Run inference for each segment!
            val inputTensor = Tensor.fromBlob(segment, longArrayOf(1, 1, segment.size.toLong()))
            val start = System.currentTimeMillis()
            val modelOutputTensor = module.forward(IValue.from(inputTensor)).toTensor()
            val timeElapsed = System.currentTimeMillis() - start
            inferenceTime += timeElapsed
//            Log.d(
//                Constants.TAG, "LaunchViewModel.runInference(): inference times = $timeElapsed"
//            )
            // elapsed time = 1.8-2 sec for a 4 sec audio clip

            val output = modelOutputTensor.dataAsFloatArray

            val cleanOP = output.slice(0 until 64000).toFloatArray()
            modelOutputs.add(cleanOP)
        }

        return inferenceTime

    }
    //Input: Takes in output audio segments. Normalizes them and returns full audio array
    private fun startPostProcessing(outputAudioSegments: ArrayList<FloatArray>) {
        Log.d(Constants.TAG, "LaunchViewModel.startPostProcessing():")

        audioProcessingState.value = audioProcessingState.value.copy(
            isPostProcessing = true
        )

        val audioProcessor = audioProcessingState.value.audioProcessor

        var postProcessedAudio = floatArrayOf()

        audioProcessor?.let {
            Log.d(
                Constants.TAG, "LaunchViewModel.startPostProcessing(): AudioProcessor exists!"
            )
            postProcessedAudio = it.postProcessAudio(outputAudioSegments)
        }

        audioProcessingState.value = audioProcessingState.value.copy(
            isPostProcessing = false
        )

        denoisedAudioState.value = denoisedAudioState.value.copy(
            denoisedArray = postProcessedAudio,
            audioFile = launchState.value.selectedAudioFile
        )

    }


    private fun assetFilePath(context: Context, assetName: String): String? {
        Log.d(
            Constants.TAG, "LaunchViewModel.assetFilePath():"
        )
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        try {
            context.assets.open(assetName).use { `is` ->
                FileOutputStream(file).use { os ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (`is`.read(buffer).also { read = it } != -1) {
                        os.write(buffer, 0, read)
                    }
                    os.flush()
                }
                return file.absolutePath
            }
        } catch (e: IOException) {
            Log.d(
                Constants.TAG, "LaunchViewModel.assetFilePath(): ERRRRORRRRR!"
            )
        }
        return null
    }


    private fun performInferenceTest(loadtestingfiles: () -> ArrayList<AudioFile>) {
        Log.d(Constants.TAG,"LaunchViewModel.performInferenceTest(): STARTOOOOO! ")
        testInferenceTimesJob?.cancel()

        testInferenceTimesJob = viewModelScope.launch(Dispatchers.IO) {
            val list = loadtestingfiles()
            Log.d(Constants.TAG,"LaunchViewModel.performInferenceTest(): number of files : ${list.size} ")
            val longArray = LongArray(list.size)

            for ((i, audioFile) in list.withIndex()) {
                val inputToModel = startPreProcessingInput_InferenceTest(audioFile)
                val inferenceTime = runInference_InferenceTest(inputToModel, ptModule)
                longArray[i] = inferenceTime
                Log.d(Constants.TAG,"LaunchViewModel.performInferenceTest(): ${audioFile.displayName} time = $inferenceTime ")
            }

            val sum = longArray.sum()
            val mean =  sum.toDouble() / longArray.size

            val sumSquaredDiff = longArray.sumOf { (it - mean).pow(2) }
            val variance =  sumSquaredDiff / (longArray.size - 1).toDouble()

            Log.d(Constants.TAG,"INFERENCE TEST RESULT: mean: $mean and variance : $variance")
        }
        Log.d(Constants.TAG,"LaunchViewModel.performInferenceTest(): ENDOOOO! ")

    }
}



