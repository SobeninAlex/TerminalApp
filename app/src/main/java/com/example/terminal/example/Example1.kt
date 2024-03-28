package com.example.terminal.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun Example1() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Blue),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Button(modifier = Modifier.fillMaxSize(), onClick = { /*TODO*/ }) {
                Text(text = "slfksldkfmds")
            }
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = { /*TODO*/ }) {
            Text(text = "slfksldkfmds")
        }

    }
}