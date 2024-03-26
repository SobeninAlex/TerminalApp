package com.example.terminal.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import com.example.terminal.data.Bar
import kotlin.math.roundToInt

private const val MIN_VISIBLE_BARS_COUNT = 20

@Composable
fun Terminal(bars: List<Bar>) {
    var visibleBarsCount by remember {
        mutableIntStateOf(100)
    }

    var terminalWidth by remember {
        mutableFloatStateOf(0f)
    }

    val barWidth by remember {
        //Если один state зависит от другого, или других, то вместо mutableStateOf можно использовать derivedStateOf
        derivedStateOf {
            terminalWidth / visibleBarsCount
        }
    }

    var scrolledBy by remember {
        mutableFloatStateOf(0f)
    }

    val visibleBars by remember {
        derivedStateOf {
            val startIndex = (scrolledBy / barWidth).roundToInt().coerceAtLeast(0)
            val endIndex = (startIndex + visibleBarsCount).coerceAtMost(bars.size)
            bars.subList(startIndex, endIndex)
        }
    }

    val transformableState = TransformableState { zoomChange, panChange, rotationChange ->
        visibleBarsCount = (visibleBarsCount / zoomChange).roundToInt()
            .coerceIn(
                minimumValue = MIN_VISIBLE_BARS_COUNT,
                maximumValue = bars.size
            )
        scrolledBy = (scrolledBy + panChange.x)
            .coerceAtLeast(0f)
            .coerceAtMost((bars.size * barWidth) - terminalWidth)
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
            .transformable(transformableState)
    ) {
        terminalWidth = size.width
        val max = visibleBars.maxOf { it.high }
        val min = visibleBars.minOf { it.low }
        val pxPerPoint = size.height / (max - min)

        translate(left = scrolledBy) {
            bars.forEachIndexed { index,  bar ->
                val offsetX = size.width - (index * barWidth)
                drawLine(
                    color = Color.White,
                    start = Offset(offsetX, size.height - ((bar.low - min) * pxPerPoint)),
                    end = Offset(offsetX, size.height - ((bar.high - min) * pxPerPoint)),
                    strokeWidth = 1f
                )
                drawLine(
                    color = if (bar.open < bar.close) Color.Green else Color.Red,
                    start = Offset(offsetX, size.height - ((bar.open - min) * pxPerPoint)),
                    end = Offset(offsetX, size.height - ((bar.close - min) * pxPerPoint)),
                    strokeWidth = barWidth / 2
                )
            }
        }
    }
}