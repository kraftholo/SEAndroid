package com.example.rtse

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.rtse.domain.Constants
import com.example.rtse.domain.PermissionCheckState


class MainViewModel(

) : ViewModel(){

    init {
        Log.d(Constants.TAG,"MainViewModel.init()")
    }

    val state: MutableState<MainActivityState> = mutableStateOf(MainActivityState())

    fun onTriggerEvent(event: MainActivityEvent){
        Log.d(Constants.TAG,"MainViewModel.onTriggerEvent():: $event ")

        when(event){
            is MainActivityEvent.PermissionCheckComplete -> {
                state.value = state.value.copy(permissionCheckStatus = PermissionCheckState.Complete)
            }

            is MainActivityEvent.LaunchPermissionRequest -> {
                launchPassedFunction(event.launchRequestFromActivity)
            }

            else -> {}
        }
    }

    // This is kind of a work around for MVI, in this way I'm not passing any activity lifecycle thing into the viewmodel (good practice)
    private fun launchPassedFunction(launchRequestFromActivity: () -> Unit) {
        launchRequestFromActivity()
    }
}