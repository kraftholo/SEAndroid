package com.example.rtse.ui.launch


import Melon
import OuterSpace
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rtse.domain.AudioFile
import com.example.rtse.domain.AudioProcessingState
import com.example.rtse.domain.Constants
import com.example.rtse.domain.LoadingFileState
import com.example.rtse.domain.ProgressBarState
import com.example.rtse.domain.util.Args
import com.example.rtse.domain.util.ReadFilesUtil.loadAudioFiles
import com.example.rtse.domain.util.ReadFilesUtil.loadSNRFiles
import com.example.rtse.ui.launch.events.AudioTrackEvent
import com.example.rtse.ui.launch.events.LaunchEvent
import com.example.rtse.ui.launch.events.MediaPlayerEvent
import com.example.rtse.ui.launch.states.DenoisedAudioState
import com.example.rtse.ui.launch.states.LaunchState
import com.example.rtse.ui.launch.states.MediaPlayerState
import kotlin.math.floor


@Composable
fun LaunchScreen(
    launchState: LaunchState,
    mediaPlayerState: MediaPlayerState,
    audioProcessingState: AudioProcessingState,
    denoisedAudioState: DenoisedAudioState,
    onTriggerLaunchEvent: (LaunchEvent) -> Unit,
    onTriggerMediaPlayerEvent: (MediaPlayerEvent) -> Unit,
    onTriggerAudioTrackEvent: (AudioTrackEvent) -> Unit
) {

    //TODO: Add functionality for handling UI events for audioProcessingState

    var counter by remember { mutableStateOf(0) }
    SideEffect {
        counter++
        Log.d(Constants.TAG, "LaunchScreen composing = $counter:")
    }
    val context = LocalContext.current

    if (launchState.loadingFilesState == LoadingFileState.InProgress) {
        onTriggerLaunchEvent(LaunchEvent.LoadFileList { -> context.loadAudioFiles() })
        onTriggerLaunchEvent(
            LaunchEvent.LoadPytorchModel(
                "LearningRate001.ptl", Args(nameModel = "LearningRate001.ptl"), context
            )
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text(text = "Local File RTSE") }) }, content = {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
        ) {

            Column(
                modifier = Modifier.padding(it)
            ) {
                Box(Modifier.fillMaxSize()) {
                    Column {

                        //UI Element1: "Selected File:" and the list of wav files
                        Text(
                            text = "Selected File :",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )

                        // Section of scrollable filelist
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp), // Adjust padding as needed
                            elevation = 2.dp, // Adjust elevation as needed
                        ) {
                            FileList(fileListHeader = "Files to choose from:",
                                audioFiles = launchState.audioFiles,
                                onItemSelect = { selectedAudioFile ->
                                    onTriggerLaunchEvent(
                                        LaunchEvent.SelectAudioFile(
                                            selectedAudioFile
                                        )
                                    )
                                })
                        }


                        //UI Element2 :Section of selected file playback
                        AudioPlaybackRow(
                            mediaPlayerState, onTriggerMediaPlayerEvent
                        )

                        Spacer(modifier = Modifier.padding(8.dp))

//                      Toggle this for testing inference times

                        val inferenceTesting = false
                        if (inferenceTesting){
                            Button(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue, contentColor = Color.White),
                                onClick = {onTriggerLaunchEvent(LaunchEvent.LoadFilesForInferenceTest { -> context.loadSNRFiles("0_10") })}
//                              val selectionArgs = arrayOf("%/Music/InferenceTest_${snrRange}/%.wav")
                            ) {
                                Text(
                                    text = "Test Inference Times",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(4.dp)
                                )

                                Spacer(modifier = Modifier.padding(2.dp))
                            }
                        }

                        //UI Element3 :"Prepare file for RTSE" Button
//                        PrepareRTSEButton(
//                            loadInAudioArray = {
//                                launchState.selectedAudioFile?.let {
//                                    onTriggerLaunchEvent(
//                                        LaunchEvent.LoadInAudioFile(
//                                            WavFileInfo(it, context)
//                                        )
//                                    )
//                                }
//                            },
//                            stopLoadInAudioArray = {
//                                onTriggerLaunchEvent(LaunchEvent.StopLoadingFile)
//                            },
//                            loadingInStatus = launchState.loadAudioArrayProgBarS == ProgressBarState.Loading,
//                            selectedAudioFile = launchState.selectedAudioFile,
//                            loadInSameFileAgain = launchState.selectedFileFloatingArray != null
//                        )
//
//                        Spacer(modifier = Modifier.padding(8.dp))
//
//                        Text(text = launchState.selectedFileFloatingArray?.let { "File is prepared :D" }
//                            ?: "The file is not prepared for RTSE!",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Medium,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(horizontal = 16.dp),
//                            color = launchState.selectedFileFloatingArray?.let { Color.Green }
//                                ?: Color.Red)
//                        Spacer(modifier = Modifier.padding(8.dp))


                        //UI Element4 :Steps for performing speech enhancement and button
                        ProgressionText(
                            text = "Preprocessing...",
                            isStepInProgress = audioProcessingState.isPreprocessing,
                            hasStepCompleted = audioProcessingState.inputToModel != null,
                            isEnhancing = launchState.enhancementProgBarS == ProgressBarState.Loading,
                            doneEnhancement = denoisedAudioState.denoisedArray != null
                        )
                        ProgressionText(
                            text = "Inference...",
                            isStepInProgress = audioProcessingState.isInferring,
                            hasStepCompleted = audioProcessingState.outputFromModel != null,
                            isEnhancing = launchState.enhancementProgBarS == ProgressBarState.Loading,
                            doneEnhancement = denoisedAudioState.denoisedArray != null
                        )
                        ProgressionText(
                            text = "PostProcessing...",
                            isStepInProgress = audioProcessingState.isPostProcessing,
                            hasStepCompleted = denoisedAudioState.denoisedArray != null,
                            isEnhancing = launchState.enhancementProgBarS == ProgressBarState.Loading,
                            doneEnhancement = denoisedAudioState.denoisedArray != null
                        )

                        Spacer(modifier = Modifier.padding(4.dp))

                        //"Start RTSE" Button
                        StartRTSEButton(
                            enhance = { toEnhance ->
                                onTriggerLaunchEvent(LaunchEvent.Enhance(toEnhance))
                            },
                            enhancementStatus = launchState.enhancementProgBarS == ProgressBarState.Loading,
                            isEnabled = launchState.selectedAudioFile != null
                        )

                        Spacer(modifier = Modifier.padding(8.dp))
                        //UI Element5 : Denoised audio playback section

                        denoisedAudioState.denoisedArray?.let{
                            Log.d(Constants.TAG,"Denoised array size = ${it.size}!!!!!!!!!!!!!!")
                        }

                        DenoisedAudioPlaybackRow(
                            denoisedAudioState = denoisedAudioState,
                            onTriggerAudioTrackEvent = onTriggerAudioTrackEvent
                        )
                    }

                    //==================================================================================================================================================
                    if (launchState.loadFilesProgBarS == ProgressBarState.Loading) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }

            }
        }
    })


}

@Composable
fun ProgressionText(
    text: String,
    isStepInProgress: Boolean,
    hasStepCompleted: Boolean,
    isEnhancing : Boolean,
    doneEnhancement : Boolean
) {
    Row(
        modifier = Modifier.padding(start = 16.dp)
    ) {
        Text(
            text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (hasStepCompleted || doneEnhancement) Color(red = 24, green = 170, blue = 44) else Color.Red,
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .padding(6.dp)
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(10.dp)) // Add spacing between the text and progress bar

        //If enhancement is hasnt started, show a cross
        if(!isEnhancing && !doneEnhancement){
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "close",
                modifier = Modifier
                    .size(20.dp) // Set the icon size
                    .padding(2.dp)
                    .align(Alignment.CenterVertically)
            )
        }
        else{
            //If enhancement has started, show a progbar corresponding to step running
            if(isStepInProgress){
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(20.dp)
                        .align(Alignment.CenterVertically),
                    strokeWidth = 2.dp
                )
            }

            else if(hasStepCompleted || doneEnhancement){
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "done",
                    modifier = Modifier
                        .size(20.dp) // Set the icon size
                        .padding(2.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

// File list and list item composable
@Composable
fun FileList(
    fileListHeader: String,
    audioFiles: ArrayList<AudioFile>,
    onItemSelect: (item: AudioFile) -> Unit
) {
    var selectedItem by remember { mutableIntStateOf(-1) }
    val minRowCount = 6
    val emptyRowCount = maxOf(minRowCount - audioFiles.size, 0)

    Text(
        text = fileListHeader,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.33f)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(audioFiles) { index, item ->
                SingleSelectableListItem(
                    itemText = item.displayName,
                    onItemClick = {
                        selectedItem = index
                        onItemSelect(item)

                    },
                    isSelected = selectedItem == index,
                )
            }

            items(emptyRowCount) { index ->
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp) // Adjust the height as needed
                        .background(Color.White)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun SingleSelectableListItem(
    itemText: String, onItemClick: () -> Unit, isSelected: Boolean
) {
    val backgroundColor = if (isSelected) OuterSpace else Color.White
    val textColor = if (isSelected) Melon else Color.Black

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Text(
            text = itemText, fontSize = 16.sp, fontStyle = FontStyle.Italic, color = textColor
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.LightGray)
                .padding(top = 1.dp)
        )
    }
}

//This composable will deal with all the mediaplayer stuff and therefore has been given access to the state of the media player and triggering events
@Composable
fun AudioPlaybackRow(
    mediaPlayerState: MediaPlayerState, onTriggerMPEvent: (MediaPlayerEvent) -> Unit
) {

//    var count by remember { mutableIntStateOf(0) }
//    SideEffect {
//        count++
//        Log.d(Constants.TAG, "AudioPlayBackRow is composing = $count")
//    }

    val context = LocalContext.current

    //Initializing the media player in the viewmodel
    mediaPlayerState.audioFile?.let {
        SideEffect {
            Log.d(Constants.TAG, "Init-ing audio player with selected audio file!")
        }
        onTriggerMPEvent(MediaPlayerEvent.Init(context, it))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.padding(8.dp))

        //Selected audio file name text
        Text(text = mediaPlayerState.audioFile?.let { "\"${it.displayName}\"" }
            ?: "No File has been selected yet",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            color = mediaPlayerState.audioFile?.let { Color.Black } ?: Color.Red)

        Spacer(modifier = Modifier.padding(8.dp))

        //Actual audio playback controls
        val isControlsVisible = mediaPlayerState.audioFile != null
        // Not following "State Hoisting" to the T, but the actual events are down in that composable
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(animationSpec = tween(2000)),
            exit = fadeOut(animationSpec = tween(1000))
        ) {
            mediaPlayerState.audioFile?.let {
                PlaybackControls(
                    onTriggerEvent = onTriggerMPEvent,
                    modifier = Modifier.fillMaxWidth(),
                    mediaPlayerState = mediaPlayerState
                )
            }
        }

    }
}

@Composable
fun PlaybackControls(
    modifier: Modifier = Modifier,
    onTriggerEvent: (MediaPlayerEvent) -> Unit,
    mediaPlayerState: MediaPlayerState
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Gray)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause Button
                IconButton(onClick = {
                    if (mediaPlayerState.isPlaying) onTriggerEvent(MediaPlayerEvent.Pause)
                    else onTriggerEvent(MediaPlayerEvent.Play)
                }) {
                    Icon(
                        imageVector = if (mediaPlayerState.isPlaying) Icons.Rounded.Close else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                // Seek Bar
                Slider(
                    value = mediaPlayerState.progress.toFloat(), onValueChange = { newProgress ->
                        onTriggerEvent(MediaPlayerEvent.SeekTo(newProgress.toInt()))
                    }, valueRange = 0.0f..mediaPlayerState.duration.toFloat(), steps = 100
                )
            }

            // Update currentTime
            val currentTime =
                "${formatTime(mediaPlayerState.progress)} / ${formatTime(mediaPlayerState.duration)}"

            // Current Time and Total Duration
            Text(
                text = currentTime, fontSize = 14.sp, color = Color.White
            )
        }
    }
}

// The Prepare RTSE button
@Composable
fun PrepareRTSEButton(
    loadInAudioArray: () -> (Unit),
    stopLoadInAudioArray: () -> (Unit),
    loadingInStatus: Boolean,
    selectedAudioFile: AudioFile?,
    loadInSameFileAgain: Boolean,
) {
    val context = LocalContext.current
    val buttonClick: () -> Unit = {

        if (loadInSameFileAgain) {
            Toast.makeText(context, "This file is already prepared!", Toast.LENGTH_SHORT).show()
        } else {
            if (!loadingInStatus) {
                loadInAudioArray()
                // Start loading in the audio array
            } else {
                // Stop loading in the audio array
                stopLoadInAudioArray()
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        //Section to load in the audio file array
        Button(
            modifier = Modifier.padding(horizontal = 16.dp),
            enabled = selectedAudioFile != null,
            onClick = buttonClick,
            colors = if (!loadingInStatus) ButtonDefaults.buttonColors(
                backgroundColor = Color.Blue, contentColor = Color.White
            ) else ButtonDefaults.buttonColors(
                backgroundColor = Color.Red, contentColor = Color.Black
            )
        ) {
            Text(
                text = if (!loadingInStatus) "Prepare file for RTSE" else "Stop file preparation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(4.dp)
            )

            Spacer(modifier = Modifier.padding(2.dp))
        }

        AnimatedVisibility(
            visible = loadingInStatus,
            enter = fadeIn(animationSpec = tween(2000)),
            exit = fadeOut(animationSpec = tween(1000))
        ) {
            CircularProgressIndicator(
                color = Melon, strokeWidth = 3.dp
            )
        }
    }
}

private fun formatTime(milliseconds: Int): String {
    val seconds = floor((milliseconds / 1000).toDouble()).toInt()
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

//Composable to start real-time speech enhancement
@Composable
fun StartRTSEButton(
    enhance: (doEnhance: Boolean) -> (Unit),
    enhancementStatus: Boolean,
    isEnabled: Boolean ,
) {
    val buttonClick: () -> Unit = {
        if (!enhancementStatus) {
            // Start enhancement
            enhance(true)

        } else {
            // Stop enhancement
            enhance(false)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        //Section to load in the audio file array
        Button(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            enabled = isEnabled,
            onClick = buttonClick,
            colors = if (!enhancementStatus) ButtonDefaults.buttonColors(
                backgroundColor = Color.Blue, contentColor = Color.White
            ) else ButtonDefaults.buttonColors(
                backgroundColor = Color.Red, contentColor = Color.Black
            )
        ) {
            Text(
                text = if (!enhancementStatus) "Start Enhancement" else "Stop Enhancement ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(4.dp)
            )

            Spacer(modifier = Modifier.padding(2.dp))
        }

//        AnimatedVisibility(
//            visible = enhancementStatus,
//            enter = fadeIn(animationSpec = tween(2000)),
//            exit = fadeOut(animationSpec = tween(1000))
//        ) {
//            CircularProgressIndicator(
//                color = Melon, strokeWidth = 3.dp
//            )
//        }
    }
}


//Composable to deal with denoised audio playback
@Composable
fun DenoisedAudioPlaybackRow(
    denoisedAudioState: DenoisedAudioState, onTriggerAudioTrackEvent: (AudioTrackEvent) -> Unit
) {

    val context = LocalContext.current

    //Initializing the media player in the viewmodel
    denoisedAudioState.denoisedArray?.let {
        SideEffect {
            Log.d(Constants.TAG, "Init-ing audio player with selected audio file!")
        }
        onTriggerAudioTrackEvent(AudioTrackEvent.Init(denoisedAudioState.denoisedArray))
    }

    val isEnhanced = denoisedAudioState.denoisedArray != null
    AnimatedVisibility(
        visible = isEnhanced,
        enter = fadeIn(animationSpec = tween(2000)),
        exit = fadeOut(animationSpec = tween(1000))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.padding(8.dp))

            //Selected audio file name text
            Text(
                text = "Denoised audio",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.padding(8.dp))

//            Actual audio playback controls

            DenoisedPlaybackControls(
                onTriggerAudioTrackEvent = onTriggerAudioTrackEvent,
                modifier = Modifier.fillMaxWidth(),
                denoisedAudioState = denoisedAudioState
            )
        }

    }

}

@Composable
fun DenoisedPlaybackControls(
    onTriggerAudioTrackEvent: (AudioTrackEvent) -> Unit,
    modifier: Modifier,
    denoisedAudioState: DenoisedAudioState
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Gray)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause Button
                IconButton(onClick = {
                    if (denoisedAudioState.isPlaying) onTriggerAudioTrackEvent(AudioTrackEvent.Pause)
                    else onTriggerAudioTrackEvent(AudioTrackEvent.Play)
                }) {
                    Icon(
                        imageVector = if (denoisedAudioState.isPlaying) Icons.Rounded.Close else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                // Seek Bar
                Slider(
                    value = denoisedAudioState.progress.toFloat(),
                    onValueChange = { newProgress -> onTriggerAudioTrackEvent(AudioTrackEvent.SeekTo(newProgress.toInt())) },
                    valueRange = 0.0f..denoisedAudioState.duration.toFloat(), steps = 100
                )
            }

            // Update currentTime
//            val currentTime =
//                "${formatTime(denoisedAudioState.progress)} / ${formatTime(denoisedAudioState.duration)}"
//
//            // Current Time and Total Duration
//            Text(
//                text = currentTime, fontSize = 14.sp, color = Color.White
//            )
        }
    }
}











