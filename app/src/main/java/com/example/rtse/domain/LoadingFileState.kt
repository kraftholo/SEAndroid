package com.example.rtse.domain

sealed class LoadingFileState {

    object InProgress : LoadingFileState()

    object Complete : LoadingFileState()
}