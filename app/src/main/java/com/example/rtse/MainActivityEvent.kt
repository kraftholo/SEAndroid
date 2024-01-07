package com.example.rtse

sealed class MainActivityEvent {

    data class LaunchPermissionRequest(
        val launchRequestFromActivity : () -> Unit
    ): MainActivityEvent()

    object PermissionCheckComplete: MainActivityEvent()
}