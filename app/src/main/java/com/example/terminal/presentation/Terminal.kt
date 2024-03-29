package com.example.terminal.presentation

import android.annotation.SuppressLint
import android.icu.util.Calendar
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.terminal.R
import com.example.terminal.data.Bar
import com.example.terminal.ui.theme.DarkBlue
import com.example.terminal.ui.theme.Orange
import java.util.Calendar.SHORT
import java.util.Locale
import kotlin.math.roundToInt

private const val MIN_VISIBLE_BARS_COUNT = 20

//Если один state зависит от другого, или других, то вместо mutableStateOf можно использовать derivedStateOf


@Composable
fun Terminal(
    modifier: Modifier = Modifier,
) {

    val terminalViewModel = viewModel<TerminalViewModel>()
    val screenState = terminalViewModel.state.collectAsState()

    when (val currentState = screenState.value) {

        is TerminalScreenState.Initial -> {}

        is TerminalScreenState.Loading -> {
            Box(
                modifier = modifier
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
            val terminalState = rememberTerminalState(bars = currentState.barList)

            Chart(
                modifier = modifier,
                terminalState = terminalState,
                onTerminalStateChanged = {
                    terminalState.value = it
                },
                timeFrame = currentState.timeFrame
            )

            currentState.barList.firstOrNull()?.let {
                Prices(
                    modifier = modifier,
                    terminalState = terminalState,
                    lastPrice = it.close
                )
            }
            TimeFrames(
                modifier = modifier,
                selectedFrame = currentState.timeFrame,
                onTimeFrameSelected = {
                    terminalViewModel.loadBarList(timeframe = it)
                }
            )
        }
    }

}

@Composable
private fun TimeFrames(
    modifier: Modifier = Modifier,
    selectedFrame: TimeFrame,
    onTimeFrameSelected: (TimeFrame) -> Unit
) {

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Row(
            modifier = modifier
                .wrapContentSize()
                .padding(end = 8.dp, bottom = 30.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TimeFrame.entries.forEach { timeframe ->
                val labelResId = when (timeframe) {
                    TimeFrame.MIN_5 -> R.string.timeframe_5_min
                    TimeFrame.MIN_15 -> R.string.timeframe_15_min
                    TimeFrame.MIN_30 -> R.string.timeframe_30_min
                    TimeFrame.HOUR_1 -> R.string.timeframe_1_hour
                }

                val isSelected = selectedFrame == timeframe
                AssistChip(
                    onClick = { onTimeFrameSelected(timeframe) },
                    label = {
                        Text(text = stringResource(id = labelResId))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isSelected) Color.White else Color.Transparent,
                        labelColor = if (isSelected) Color.Black else Color.Green
                    )
                )
            }
        }
    }
}

@Composable
private fun Chart(
    modifier: Modifier = Modifier,
    terminalState: State<TerminalState>,
    onTerminalStateChanged: (TerminalState) -> Unit,
    timeFrame: TimeFrame,
) {
    val currentState = terminalState.value

    val transformableState = rememberTransformableState { zoomChange, panChange, rotationChange ->
        val visibleBarsCount = (currentState.visibleBarsCount / zoomChange).roundToInt()
            .coerceIn(
                minimumValue = MIN_VISIBLE_BARS_COUNT,
                maximumValue = currentState.bars.size
            )
        val scrolledBy = (currentState.scrolledBy + panChange.x)
            .coerceAtLeast(0f)
            .coerceAtMost(currentState.bars.size * currentState.barWidth - currentState.terminalWidth)

        onTerminalStateChanged(
            currentState.copy(
                visibleBarsCount = visibleBarsCount,
                scrolledBy = scrolledBy,
            )
        )
    }

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black)
            .clipToBounds()
            .padding(
                top = 32.dp,
                bottom = 32.dp,
                end = 42.dp
            )
            .transformable(transformableState)
            .onSizeChanged {
                onTerminalStateChanged(
                    currentState.copy(
                        terminalWidth = it.width.toFloat(),
                        terminalHeight = it.height.toFloat()
                    )
                )
            }
    ) {
        val min = currentState.min
        val pxPerPoint = currentState.pxPerPoint

        translate(left = currentState.scrolledBy) {
            currentState.bars.forEachIndexed { index, bar ->
                val offsetX = size.width - index * currentState.barWidth
                drawTimeDelimiter(
                    bar = bar,
                    nextBar = if (index < currentState.bars.size - 1) {
                        currentState.bars[index + 1]
                    } else {
                        null
                    },
                    timeFrame = timeFrame,
                    offsetX = offsetX,
                    textMeasurer = textMeasurer
                )
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
                    strokeWidth = currentState.barWidth / 2
                )
            }
        }
    }
}

@Composable
private fun Prices(
    modifier: Modifier = Modifier,
    terminalState: State<TerminalState>,
    lastPrice: Float,
) {
    val currentState = terminalState.value

    val textMeasurer = rememberTextMeasurer()

    val max = currentState.max
    val min = currentState.min
    val pxPerPoint = currentState.pxPerPoint

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .padding(vertical = 32.dp)
    ) {
        drawPrices(
            max = max,
            pxPerPoint = pxPerPoint,
            min = min,
            lastPrice = lastPrice,
            textMeasurer = textMeasurer
        )
    }
}

private fun DrawScope.drawTimeDelimiter(
    bar: Bar,
    nextBar: Bar?,
    timeFrame: TimeFrame,
    offsetX: Float,
    textMeasurer: TextMeasurer
) {
    val calendar = bar.calendar

    val minutes = calendar.get(Calendar.MINUTE)
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val shouldDrawDelimiter = when(timeFrame) {
        TimeFrame.MIN_5 -> {
            minutes == 0
        }
        TimeFrame.MIN_15 -> {
            minutes == 0 && hours % 2 == 0
        }
        TimeFrame.MIN_30, TimeFrame.HOUR_1 -> {
            val nextBarDay = nextBar?.calendar?.get(Calendar.DAY_OF_MONTH)
            day != nextBarDay
        }
    }

    if (!shouldDrawDelimiter) return

    drawLine(
        color = Color.White.copy(alpha = 0.5f),
        start = Offset(offsetX, 0f),
        end = Offset(offsetX, size.height),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(
                4.dp.toPx(), //ширина пунктирной линии
                4.dp.toPx() //интервал между линиями
            )
        )
    )

    val nameOfMonth = calendar.getDisplayName(Calendar.MONTH, SHORT, Locale.getDefault())

    val text = when(timeFrame) {
        TimeFrame.MIN_5, TimeFrame.MIN_15 -> {
            String.format("%02d:00", hours)
        }
        TimeFrame.MIN_30, TimeFrame.HOUR_1 -> {
            String.format("%s %s", day, nameOfMonth)
        }
    }

    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = Color.White,
            fontSize = 12.sp
        )
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(x = offsetX - textLayoutResult.size.width / 2, y = size.height)
    )
}

private fun DrawScope.drawPrices(
    max: Float,
    min: Float,
    pxPerPoint: Float,
    lastPrice: Float,
    textMeasurer: TextMeasurer,
) {
    // max
    val maxPriceOffsetY = 0f
    drawDashedLine(
        start = Offset(x = 0f, y = maxPriceOffsetY),
        end = Offset(x = size.width, y = maxPriceOffsetY),
    )
    drawTextPrice(
        textMeasurer = textMeasurer,
        price = max,
        textColor = Color.Red,
        offsetY = maxPriceOffsetY
    )

    // last price
    val lastPriceOffsetY = size.height - ((lastPrice - min) * pxPerPoint)
    drawDashedLine(
        start = Offset(x = 0f, y = lastPriceOffsetY),
        end = Offset(x = size.width, y = lastPriceOffsetY),
    )
    drawTextPrice(
        textMeasurer = textMeasurer,
        price = lastPrice,
        textColor = Orange,
        offsetY = lastPriceOffsetY
    )

    // min
    val minPriceOffsetY = size.height
    drawDashedLine(
        start = Offset(x = 0f, y = minPriceOffsetY),
        end = Offset(x = size.width, y = minPriceOffsetY),
    )
    drawTextPrice(
        textMeasurer = textMeasurer,
        price = min,
        textColor = Color.Blue,
        offsetY = minPriceOffsetY
    )
}

private fun DrawScope.drawTextPrice(
    textMeasurer: TextMeasurer,
    price: Float,
    textColor: Color,
    offsetY: Float
) {
    val textLayoutResult = textMeasurer.measure(
        text = price.toString(),
        style = TextStyle(
            color = textColor,
            fontSize = 12.sp
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(x = size.width - textLayoutResult.size.width - 4.dp.toPx(), y = offsetY)
    )
}

private fun DrawScope.drawDashedLine(
    color: Color = Color.White,
    start: Offset,
    end: Offset,
    strokeWidth: Float = 1f,
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(
                4.dp.toPx(), //ширина пунктирной линии
                4.dp.toPx() //интервал между линиями
            )
        )
    )

}













