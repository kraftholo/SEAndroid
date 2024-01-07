package com.example.rtse.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun paddedButton(buttonText: String, onButtonClick: () -> Unit) {
    Column{

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onButtonClick) {
            Text(text = buttonText)
        }
    }
}

