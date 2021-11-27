package com.sdoward.pulsar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

const val MILLISECONDS_IN_DAY = 86_400_000
const val START_OF_2020 = 1577836800000

@Preview
@Composable
fun PulsarChartPreview() {
    val valueRange = 0..1000L
    val contributions = mutableMapOf<Date, Long>()
    (0..364).forEach {
        val millis = START_OF_2020 * (MILLISECONDS_IN_DAY * it)
        val date = Date.from(Instant.ofEpochMilli(millis))
        contributions[date] = valueRange.random()
    }
    PulsarChart(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contributions = contributions,
        shape = Shape.Square,
        color = Color.Green,
        pulsePadding = 0.dp
    )
}

@Composable
fun PulsarChart(
    modifier: Modifier = Modifier,
    contributions: Map<Date, Long>,
    shape: Shape = Shape.Square,
    color: Color = Color.Red,
    pulsePadding: Dp = 4.dp
) {
    val earliestDate = contributions.keys.minOf { it }
    val dayOfWeek = Calendar.getInstance().apply {
        time = earliestDate
    }.get(Calendar.DAY_OF_WEEK)
    val maxValue = contributions.values.maxOf { it }
    val alphas = mutableListOf<Float>()
    contributions.keys.sortedBy { it.toInstant().epochSecond }.forEach { date ->
        alphas.add((contributions.getOrDefault(date, 0).toFloat() / maxValue))
    }
    PulsarCore(
        modifier = modifier,
        color = color,
        rowCount = 7,
        rowStart = dayOfWeek,
        alphas = alphas,
        shape = shape,
        pulsePadding = pulsePadding
    )
}

@Preview
@Composable
fun PulsarCorePreview() {
    //   val alphas = (0..364).map { 1F }
    val alphas = listOf(
        0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7,
        0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7,
        0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7,
        0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7,
        0.1, 0.2, 0.3, 0.4,
    ).map { it.toFloat() }
    PulsarCore(
        modifier = Modifier
            .fillMaxSize()
            .padding(9.dp),
        color = Color.Red,
        rowCount = 7,
        rowStart = 0,
        alphas = alphas
    )
}

@Composable
fun PulsarCore(
    modifier: Modifier,
    color: Color,
    shape: Shape = Shape.Squircle,
    rowCount: Int = 7,
    rowStart: Int = 0,
    alphas: List<Float>,
    pulsePadding: Dp = 4.dp
) {
    if (rowStart >= rowCount) {
        throw IllegalStateException("RowStart cannot be higher than RowCount. RowStart: $rowStart RowCount: $rowCount")
    }
    val columnCount = ceil(alphas.size.toFloat() / rowCount.toFloat())
    Canvas(modifier = modifier) {
        val columnWidth = (size.width / columnCount) - pulsePadding.value
        val rowWidth = (size.height / rowCount) - pulsePadding.value
        val pulseDimension = min(columnWidth, rowWidth)
        val boxSize = Size(pulseDimension, pulseDimension)
        var currentRow = rowStart
        var currentColumn = 0
        val y =
            (currentRow * boxSize.height) + (currentRow * pulsePadding.value) + (pulsePadding.value / 2)
        var offset = Offset(
            pulsePadding.value / 2,
            y
        )
        alphas.forEach { alpha ->
            when (shape) {
                Shape.Circle -> drawCirclePulse(color, offset, boxSize, alpha)
                Shape.Square -> drawSquarePulse(color, offset, boxSize, alpha)
                Shape.Squircle -> drawSquirclePulse(color, offset, boxSize, alpha)
            }
            currentRow++
            offset += Offset(0F, boxSize.height + pulsePadding.value)
            if (currentRow % rowCount == 0) {
                currentColumn++
                currentRow = 0
                offset =
                    Offset((offset.x + boxSize.width) + pulsePadding.value, pulsePadding.value / 2)
            }
        }
    }
}

private fun DrawScope.drawSquarePulse(color: Color, offset: Offset, size: Size, alpha: Float) {
    drawRect(
        color = color,
        topLeft = offset,
        alpha = alpha,
        size = size
    )
}

private fun DrawScope.drawSquirclePulse(color: Color, offset: Offset, size: Size, alpha: Float) {
    drawRoundRect(
        color = color,
        topLeft = offset,
        alpha = alpha,
        size = size,
        cornerRadius = CornerRadius(size.width / 8F, size.height / 8F)
    )
}

private fun DrawScope.drawCirclePulse(color: Color, offset: Offset, size: Size, alpha: Float) {
    drawCircle(
        color = color,
        center = offset + size.center,
        alpha = alpha,
        radius = size.height / 2F
    )
}

enum class Shape {
    Circle,
    Square,
    Squircle
}