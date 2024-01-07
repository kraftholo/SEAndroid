package com.example.rtse.domain

import android.net.Uri

data class AudioFile(
    val id: Long,
    val displayName: String,
    val filePath: String,
    val duration: Long,
    val uri: Uri
)