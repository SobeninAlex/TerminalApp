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
    val terminalWidth: Float = 1f,
    val scrolledBy: Float = 0f,
    val terminalHeight: Float = 1f,
) {

    /*Эти поля не добавляем в конструктор, потому что их снаружи передавать не будем.
    * Они зависят только от параметров которые есть в конструкторе.
    * В конструктор отправляем все параметры, которые будем передавать снаружи*/

    val barWidth: Float
        get() = terminalWidth / visibleBarsCount

    private val visibleBars: List<Bar>
        get() {
            val startIndex = (scrolledBy / barWidth).roundToInt().coerceAtLeast(0)
            val endIndex = (startIndex + visibleBarsCount).coerceAtMost(bars.size)
            return bars.subList(startIndex, endIndex)
        }

    /*Используем не присваивание, а переопределение геттера.
    * Если использовать присваивание "=", то значение присвоится в момент создания объекта.
    * А нам нужно, что бы вызывался геттер всегда с актуальными значениями*/

    val max: Float
        get() = visibleBars.maxOf { it.high }
    val min: Float
        get() = visibleBars.minOf { it.low }
    val pxPerPoint: Float
        get() = terminalHeight / (max - min)

    companion object {
        //создали объект Saver, который говорит о том, как можно сохранить, и восстановить объект MutableState<TerminalState>
        val Saver: Saver<MutableState<TerminalState>, Any> = listSaver(
            save = {
                val terminalState = it.value
                listOf(
                    terminalState.bars,
                    terminalState.visibleBarsCount,
                    terminalState.terminalWidth,
                    terminalState.scrolledBy,
                    terminalState.terminalHeight
                )
            },
            restore = {
                val terminalState = TerminalState(
                    bars = it[0] as List<Bar>,
                    visibleBarsCount = it[1] as Int,
                    terminalWidth = it[2] as Float,
                    scrolledBy = it[3] as Float,
                    terminalHeight = it[4] as Float
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
