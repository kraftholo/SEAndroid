package com.example.rtse.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MediaPlayRow(

    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
){
    Row {
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onPlay) {
            Text(text = "Play")
        }

        Button(onClick = onPause) {
            Text(text = "Pause")
        }

        Button(onClick = onStop) {
            Text(text = "Stop")
        }
    }

}