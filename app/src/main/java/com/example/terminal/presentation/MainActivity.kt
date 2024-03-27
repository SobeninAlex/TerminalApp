package com.example.terminal.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.terminal.example.TestEx
import com.example.terminal.ui.theme.DarkBlue
import com.example.terminal.ui.theme.TerminalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            TestEx()
            TerminalTheme {

                val terminalViewModel = viewModel<TerminalViewModel>()
                val screenState = terminalViewModel.state.collectAsState()

                when (val currentState = screenState.value) {
                    is TerminalScreenState.Initial -> {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = DarkBlue
                            )
                        }
                    }

                    is TerminalScreenState.Content -> {
                        Terminal(bars = currentState.barList)
                    }
                }

            }
        }
    }
}
