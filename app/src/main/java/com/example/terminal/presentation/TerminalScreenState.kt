package com.example.terminal.presentation

import com.example.terminal.data.Bar

sealed class TerminalScreenState {

    data object Initial : TerminalScreenState()

    data object Loading : TerminalScreenState()

    data class Content(val barList: List<Bar>) : TerminalScreenState()

}