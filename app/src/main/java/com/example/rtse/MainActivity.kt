package com.example.rtse

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rtse.domain.Constants
import com.example.rtse.domain.PermissionCheckState
import com.example.rtse.domain.util.PermissionUtils.checkStoragePermissions
import com.example.rtse.navigation.Screen
import com.example.rtse.ui.launch.LaunchScreen
import com.example.rtse.ui.launch.viewmodels.LaunchViewModel
import com.example.rtse.ui.theme.ApplicationTheme

class MainActivity : ComponentActivity() {

    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private val mainViewModel: MainViewModel by viewModels()

    private fun launchPermissionRequest(permissionsToRequest: Array<String>) {
        Log.d(Constants.TAG, "MainActivity.launchPermissionRequest():")
        if(permissionsToRequest.isNotEmpty()){
            storagePermissionRequest.launch(permissionsToRequest)
        }else{
            // Already has the required permissions
            mainViewModel.onTriggerEvent(MainActivityEvent.PermissionCheckComplete)
        }
    }

    // PermissionRequestLauncher
    private val storagePermissionRequest = this@MainActivity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        readPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_AUDIO] ?: readPermissionGranted
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
        }
        writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
            ?: writePermissionGranted

        if(readPermissionGranted){
            mainViewModel.onTriggerEvent(MainActivityEvent.PermissionCheckComplete)
        }else{
            // Otherwise it will be just stuck in a loop for now
            // I could handle this later
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(Constants.TAG, "MainActivity.onCreate():")
        //Check for permission on startup

        mainViewModel.onTriggerEvent(MainActivityEvent.LaunchPermissionRequest(launchRequestFromActivity = { -> launchPermissionRequest(checkStoragePermissions()) }))

        setContent {
            ApplicationTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Screen.PermissionCheck.route){
                    //Passed in the state value to navigate when permission check is complete
                    addPermissionCheckScreen(Screen.PermissionCheck.route, emptyList(),navController)

                    addLaunchScreen(route = Screen.LocalAudio.route, emptyList(),navController)
                }
            }
        }
    }

    private fun NavGraphBuilder.addLaunchScreen(
        route: String,
        arguments: List<NamedNavArgument>,
        navController: NavController
    ) {
        Log.d(Constants.TAG, "MainActivity.addLaunchScreen():")
        return composable(
            route = route
        ) {

//            This was a very bad decision
//            1. This composable also recomposes when there is a change in LaunchScreen composable, which causes a new LaunchViewModel to be created
//            val launchViewModel = LaunchViewModel()

            val launchViewModel = remember {LaunchViewModel()}
            LaunchScreen(
                launchState = launchViewModel.launchState.value,
                mediaPlayerState = launchViewModel.mediaPlayerState.value,
                audioProcessingState = launchViewModel.audioProcessingState.value,
                denoisedAudioState = launchViewModel.denoisedAudioState.value,
                onTriggerLaunchEvent = { event ->
                    launchViewModel.onTriggerLaunchEvent(event)
                },
                onTriggerMediaPlayerEvent = { event ->
                    launchViewModel.onTriggerMediaPlayerEvent(event)
                },
                onTriggerAudioTrackEvent = {event ->
                    launchViewModel.onTriggerAudioTrackEvent(event)
                }
            )
        }
    }

    private fun NavGraphBuilder.addPermissionCheckScreen(
        route : String,
        arguments: List<NamedNavArgument>,
        navController: NavController
    ){
        Log.d(Constants.TAG, "MainActivity.addPermissionCheckScreen():")
        return composable(
            route = route
        ){
            PermissionCheck(
                //Passed in the same viewModel that is being used in MainActivity
                mainViewModel.state.value,
                navigateToLaunchScreen = {
                    navController.navigate(Screen.LocalAudio.route)
                }
            )
        }
    }
}

//Almost like a placeholder screen to check for permissions before moving forward
@Composable
fun PermissionCheck(
    state : MainActivityState,
    navigateToLaunchScreen : () -> Unit
) {
    // Never write any non-composable code inside composable function
    SideEffect{
        Log.d(Constants.TAG, "PermissionCheck composing:")
    }

    var navigationTriggered by remember { mutableStateOf(false) }

    Column {
        if(state.permissionCheckStatus == PermissionCheckState.Complete && !navigationTriggered)  {
            SideEffect {
                Log.d(Constants.TAG,"Navigating to Launch Screen")
            }
            navigateToLaunchScreen()
            navigationTriggered = true
        }

        Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }

}
