//package com.example.convtasnetktstarter
//
//import WavFileInfo
//import android.content.Context
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.util.Log
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.ActivityResultLauncher
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.core.content.ContextCompat
//import org.pytorch.IValue
//import org.pytorch.LiteModuleLoader
//import org.pytorch.Module
//import org.pytorch.Tensor
//import java.io.BufferedReader
//import java.io.File
//import java.io.FileOutputStream
//import java.io.IOException
//import java.io.InputStreamReader
//import java.util.Arrays
//import android.Manifest
//import android.content.ContentUris
//import android.media.AudioAttributes
//import android.media.MediaPlayer
//import android.os.Build
//import android.provider.MediaStore
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import com.example.convtasnetktstarter.components.MediaPlayRow
//import com.example.convtasnetktstarter.components.paddedButton
//import com.example.convtasnetktstarter.domain.AudioFile
//
//class MainActivity2 : ComponentActivity() {
//
//    private val TAG = "MainActivitySK "
//    private var module: Module? = null
//    private var audioFiles: ArrayList<AudioFile> = ArrayList()
//    private lateinit var currentWavFileInfo : WavFileInfo
//
//    private var mediaPlayer: MediaPlayer? = null
//
//    private var readPermissionGranted = false
//    private var writePermissionGranted = false
//    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//
//            var isAudioArrayPresent by remember{
//                mutableStateOf("False")
//            }
//
//            ConvTasnetKtStarterTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth(0.7f)
//                            .padding(horizontal = 16.dp),
//                        verticalArrangement = Arrangement.Center
//                    ) {
//                        Row(horizontalArrangement = Arrangement.Center) {
//                            paddedButton(buttonText = "Start Inference") {
//                                startInference()
//                            }
//                        }
//                        Row(horizontalArrangement = Arrangement.Center) {
//                            paddedButton(buttonText = "Load the audio files") {
//                                audioFiles = ArrayList(loadAudioFiles().asList())
//                            }
//                        }
//
//                        Row(horizontalArrangement = Arrangement.Center) {
//                            paddedButton(buttonText = "Read the audio files") {
//                                if (audioFiles.isNotEmpty()) {
//                                    currentWavFileInfo = readAudioFile(audioFiles[0])
//                                    isAudioArrayPresent = "True"
//                                } else {
//                                    Toast.makeText(
//                                        applicationContext,
//                                        "No audio files loaded in yet!",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            }
//                        }
//
//                        if(isAudioArrayPresent == "True"){
//                            MediaPlayRow(onPlay = {playPlayer()}, onPause = { pausePlayer() }, onStop = {stopPlayer()})
//                        }
//
//                    }
//
//                }
//            }
//        }
//        // View complete
//
//        // Define the permissions launcher
//        permissionsLauncher =
//            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//                printLogD(TAG, "permissionsLauncher: ")
//                readPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    permissions[Manifest.permission.READ_MEDIA_AUDIO] ?: readPermissionGranted
//                } else {
//                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
//                }
//                writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
//                    ?: writePermissionGranted
//            }
//
//        // Check for required permissions on app start
//        updateOrRequestPermissions()
//
//    }
//
//
//    private fun readAudioFile(audioFile: AudioFile) : WavFileInfo {
//        printLogD(TAG, "readAudioFiles(): ${audioFiles.size}")
//        val wavFileInfo = WavFileInfo(audioFile, applicationContext)
//        val audioData = wavFileInfo.audioData
//        Log.d(TAG, "readAudioFile(): wavFileInfo = $wavFileInfo")
//
//        //Stuff to check if the values correspond to vscode torch.load()
//        val nonZeroCount = audioData.count { it != 0.0f }
//        Log.d(TAG, "readAudioFile(): audioData.size = ${audioData.size}")
//        Log.d(TAG, "readAudioFile(): audioData non-zero values = $nonZeroCount")
//        val first10NonZeroValues = audioData.filter { it != 0.0f }.take(10)
//        Log.d(TAG, "readAudioFile(): first 10 non zero values \n $first10NonZeroValues")
//        Log.d(TAG, "readAudioFile(): 288 index value = ${audioData[288]}")
//        Log.d(TAG, "readAudioFile(): 342 index value = ${audioData[342]}")
//        //
//
//        Toast.makeText(applicationContext,"Audio array loaded in!",Toast.LENGTH_SHORT).show()
//        return wavFileInfo
//    }
//
//
//    //  This function will begin with reading the .wav file
//    private fun startInference() {
//        printLogD(TAG, "startInference(): ")
//
//        if (module == null) {
//            module = LiteModuleLoader.load(
//                assetFilePath(
//                    applicationContext,
//                    "notopt_yespre_convtasnet.ptl"
//                )
//            )
//        }
//
//        printLogD(TAG, "startInference(): model loaded successfully!")
//
////      Load in the csv file
//        val audioArrayStr = testSE_CSV()
//        val inputLength = audioArrayStr.size
//        val modelAudioInput = DoubleArray(inputLength)
//        for (i in audioArrayStr.indices) {
//            modelAudioInput[i] = audioArrayStr[i].toDouble()
//
////          Debugging
//            if (i < 20) {
//                printLogD(TAG, "" + modelAudioInput[i])
//            }
//        }
//
//        printLogD(TAG, "run(): modelAudioInput size = ${modelAudioInput.size}")
//        val inTensorBuffer = Tensor.allocateFloatBuffer(inputLength)
//        for (`val` in modelAudioInput) inTensorBuffer.put(`val`.toFloat())
//        printLogD(TAG, "run(): Parsed to double!")
//        val inTensor = Tensor.fromBlob(inTensorBuffer, longArrayOf(1, 1, inputLength.toLong()))
//
////      Inference
//        printLogD(TAG, "run(): Inference Start!")
//        val outIValue = module!!.forward(IValue.from(inTensor))
//        printLogD(TAG, "run(): Inference Over!")
//
//        val outTensor = outIValue.toTensor()
//        val shape = outTensor.shape()
//        printLogD(TAG, "run(): Shape =${Arrays.toString(shape)}")
//        val outArray = outTensor.dataAsFloatArray
//        val source1 = FloatArray(136960)
//        val source2 = FloatArray(136960)
//        System.arraycopy(outArray, 0, source1, 0, 136960)
//        System.arraycopy(outArray, 136960, source2, 0, 136960)
//
//    }
//
//
//    private fun loadAudioFiles(): Array<AudioFile> {
//        printLogD(TAG, "loadAudioFiles(): ")
//
//        val resolver = applicationContext.contentResolver
//        val audioCollection =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                MediaStore.Audio.Media.getContentUri(
//                    MediaStore.VOLUME_EXTERNAL_PRIMARY
//                )
//            } else {
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//            }
//
////        printLogD(TAG, "loadAudioFile(): audioCollectionURI = $audioCollection")
//
//        val projection = arrayOf(
//            MediaStore.Audio.Media._ID,
//            MediaStore.Audio.Media.DISPLAY_NAME,
//            MediaStore.Audio.Media.DATA,
//            MediaStore.Audio.Media.DURATION,
//        )
//
//        // Define a filter if needed (e.g., only certain file types)
//        val prefix = "Test_Conv"
//        val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?"
//        val selectionArgs = arrayOf("$prefix%")
//
//        val audioFiles = mutableListOf<AudioFile>()
//        // Sort order (if needed)
//        val sortOrder = null
//
//        // Perform the query
//        resolver.query(
//            audioCollection,
//            projection,
//            selection,
//            selectionArgs,
//            sortOrder
//        )?.use { cursor ->
//            // Iterate through the cursor to retrieve audio file information
//            while (cursor.moveToNext()) {
//                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
//                val displayName =
//                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
//                val filePath =
//                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
//                val duration =
//                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
//                val audioUri = ContentUris.withAppendedId(
//                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                    id
//                )
//                // Do something with the retrieved information (e.g., add it to a list)
//                // You can create a data class or structure to store this information.
//                val audioFile = AudioFile(id, displayName, filePath, duration, audioUri)
//                audioFiles.add(audioFile)
//                printLogD(TAG, "loadAudioFiles(): $audioFile")
//                // Add audioFile to your list or perform any other action needed.
//            }
//        }
//
//        return audioFiles.toTypedArray()
//
//    }
//
//
//    private fun updateOrRequestPermissions() {
//        printLogD(TAG, "updateOrRequestPermissions(): ")
//
//        //Checking status of permissions
//        val hasReadPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_MEDIA_AUDIO
//            ) == PackageManager.PERMISSION_GRANTED
//        } else {
//            ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) == PackageManager.PERMISSION_GRANTED
//        }
//        val hasWritePermission = ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        ) == PackageManager.PERMISSION_GRANTED
//
//        //Android version is above the point where I need to request write permission
//        val minSDK29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
//        printLogD(TAG, "updateOrRequestPermissions(): SDK VERSION = ${Build.VERSION.SDK_INT}")
//
//        readPermissionGranted = hasReadPermission
//        writePermissionGranted = hasWritePermission || minSDK29
//
//        printLogD(
//            TAG,
//            "updateOrRequestPermissions(): readPermissionGranted = $readPermissionGranted"
//        )
//        printLogD(
//            TAG,
//            "updateOrRequestPermissions(): writePermissionGranted = $writePermissionGranted"
//        )
//
//
//        val permissionsToRequest = mutableListOf<String>()
//        if (!writePermissionGranted) {
//            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        }
//
//        if (!readPermissionGranted) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
//            } else {
//                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
//            }
//        }
//
//        // Ask for permissions if not given yet
//        if (permissionsToRequest.isNotEmpty()) {
//            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
//        }
//    }
//
//    private fun printLogD(tag: String, s: String) {
//        println("$tag: $s")
//    }
//
//
//    private fun testSE_CSV(): ArrayList<String> {
//        val audioarrayStr = arrayListOf<String>()
//        try {
//            var line: String
//            // Open the audio file from assets
//            val inputStream = assets.open("example1mix.csv")
//
//            val reader = BufferedReader(InputStreamReader(inputStream))
//            while (true) {
//                val line = reader.readLine() ?: break
//                printLogD(TAG, "testSE_CSV(): $line")
////                val tempArray = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//                val tempArray = line.split(",".toRegex())
//                audioarrayStr += tempArray
//            }
//            reader.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        printLogD(TAG, "testSE_CSV(): str Audio array list = $audioarrayStr")
//        printLogD(TAG, "testSE_CSV(): str Audio array size = ${audioarrayStr.size}")
//        return audioarrayStr
//    }
//
//    //  Get asset file path
//    private fun assetFilePath(context: Context, assetName: String): String? {
//        val file = File(context.filesDir, assetName)
//        if (file.exists() && file.length() > 0) {
//            return file.absolutePath
//        }
//        try {
//            context.assets.open(assetName).use { `is` ->
//                FileOutputStream(file).use { os ->
//                    val buffer = ByteArray(4 * 1024)
//                    var read: Int
//                    while (`is`.read(buffer).also { read = it } != -1) {
//                        os.write(buffer, 0, read)
//                    }
//                    os.flush()
//                }
//                return file.absolutePath
//            }
//        } catch (e: IOException) {
//            Log.e(TAG, assetName + ": " + e.localizedMessage)
//        }
//        return null
//    }
//
//
//    //MediaPlayer functions
//    private fun playPlayer() {
//        printLogD(TAG,"playPlayer()")
//        mediaPlayer = MediaPlayer().apply {
//            setAudioAttributes(
//                AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                    .setUsage(AudioAttributes.USAGE_MEDIA)
//                    .build()
//            )
//            setDataSource(applicationContext, currentWavFileInfo.audioFile.uri)
//            prepare()
//            start()
//            setOnCompletionListener { stopPlayer() }
//        }
//    }
//
//
//    private fun pausePlayer() {
//        printLogD(TAG,"pausePlayer()")
//        mediaPlayer?.pause()
//    }
//
//    private fun stopPlayer() {
//        printLogD(TAG,"stopPlayer()")
//        mediaPlayer?.let {
//            it.release()
//            mediaPlayer = null
//            Toast.makeText(applicationContext,"MediaPlayer released",Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//    override fun onStop() {
//        super.onStop()
//        stopPlayer()
//    }
//
//
//}
//
//
