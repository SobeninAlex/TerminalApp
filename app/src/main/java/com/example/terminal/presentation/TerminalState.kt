package com.example.terminal.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.terminal.data.Bar
import kotlin.math.roundToInt

data class TerminalState(
    val bars: List<Bar>,
    val visibleBarsCount: Int = 100,
    val terminalWidth: Float = 0f,
    val scrolledBy: Float = 0f
) {

    //Эти поля не добавляем в конструктор, потому что их снаружи передавать не будем. Они зависят только от
    //параметров которые есть в конструкторе.
    //В конструктор отправляем все параметры, которые будем передавать снаружи

    val barWidth: Float
        get() = terminalWidth / visibleBarsCount

    val visibleBars: List<Bar>
        get() {
            val startIndex = (scrolledBy / barWidth).roundToInt().coerceAtLeast(0)
            val endIndex = (startIndex + visibleBarsCount).coerceAtMost(bars.size)
            return bars.subList(startIndex, endIndex)
        }

    companion object {
        //создали объект Saver, который говорит о том, как можно сохранить, и восстановить объект MutableState<TerminalState>
        val Saver: Saver<MutableState<TerminalState>, Any> = listSaver(
            save = {
                val terminalState = it.value
                listOf(
                    terminalState.bars,
                    terminalState.visibleBarsCount,
                    terminalState.terminalWidth,
                    terminalState.scrolledBy
                )
            },
            restore = {
                val terminalState = TerminalState(
                    bars = it[0] as List<Bar>,
                    visibleBarsCount = it[1] as Int,
                    terminalWidth = it[2] as Float,
                    scrolledBy = it[3] as Float
                )
                mutableStateOf(terminalState)
            }
        )
    }

}

@Composable
fun rememberTerminalState(bars: List<Bar>) =
    rememberSaveable(saver = TerminalState.Saver) {
        mutableStateOf(TerminalState(bars = bars))
    }
