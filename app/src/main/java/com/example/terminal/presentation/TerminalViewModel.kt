package com.example.terminal.presentation

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.terminal.data.ApiFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TerminalViewModel(

) : ViewModel() {

    private val apiService = ApiFactory.apiService

    private val _state = MutableStateFlow<TerminalScreenState>(TerminalScreenState.Initial)
    val state = _state.asStateFlow()

    private val _selectedTimeFrame = MutableLiveData<TimeFrame>()
    val selectedTimeFrame = _selectedTimeFrame.asFlow()

    private var lastState: TerminalScreenState = TerminalScreenState.Initial

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _state.value = lastState
    }

    init {
        loadBarList()
    }

    fun loadBarList(timeframe: TimeFrame = TimeFrame.HOUR_1) {
        lastState = state.value
        _state.value = TerminalScreenState.Loading
        _selectedTimeFrame.value = timeframe
        viewModelScope.launch(exceptionHandler) {
            val barList = apiService.loadBars(multiplier = timeframe.multiplier, timespan = timeframe.timespan).barList
            _state.value = TerminalScreenState.Content(barList = barList)
        }
    }

}