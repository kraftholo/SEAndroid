package com.example.rtse.navigation

import androidx.navigation.NamedNavArgument

sealed class Screen(
    val route: String,
    val arguments : List<NamedNavArgument>
){

    object PermissionCheck: Screen(
        route = "permission_check",
        arguments = emptyList()
    )
    object LocalAudio : Screen(
        route = "local_audio",
        arguments = emptyList()
    )

}