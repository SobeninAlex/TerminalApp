package com.example.terminal.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.terminal.ui.theme.TerminalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TerminalTheme {
                val terminalViewModel = viewModel<TerminalViewModel>()
                val screenState = terminalViewModel.state.collectAsState()

                when(val currentState = screenState.value) {
                    is TerminalScreenState.Initial -> {}

                    is TerminalScreenState.Content -> {
                        Log.d("MainActivityTag", currentState.barList.toString())
                    }
                }
            }
        }
    }
}
