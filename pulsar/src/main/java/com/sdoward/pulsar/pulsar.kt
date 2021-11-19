package com.sdoward.pulsar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.util.*

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
        contributions = contributions,
        shape = Shape.Squircle,
        color = Color.Green
    )
}

@Composable
fun PulsarChart(
    contributions: Map<Date, Long>,
    shape: Shape = Shape.Square,
    color: Color = Color.Red
) {
    val maxValue = contributions.values.maxOf { it }
    val alphas = mutableListOf<Float>()
    contributions.keys.sortedBy { it.toInstant().epochSecond }.forEach { date ->
        alphas.add((contributions.getOrDefault(date, 0).toFloat() / maxValue))
    }
    PulsarCore(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        color = color,
        rowCount = 7,
        alphas = alphas,
        shape = shape
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
        rowStart = 2,
        alphas = alphas
    )
}

@Composable
fun PulsarCore(
    modifier: Modifier,
    color: Color,
    shape: Shape = Shape.Square,
    rowCount: Int = 7,
    rowStart: Int = 0,
    alphas: List<Float>
) {
    if (rowStart >= rowCount) {
        throw IllegalStateException("RowStart cannot be higher than RowCount. RowStart: $rowStart RowCount: $rowCount")
    }
    val boxWH = 32.dp
    val boxSize = Size(boxWH.value, boxWH.value)
    val padding = 4.dp
    Canvas(modifier = modifier) {
        var currentRow = rowStart
        var currentColumn = 0
        var offset = Offset(0F, (currentRow * boxSize.height) + (currentRow * padding.value))
        alphas.forEach { alpha ->
            when (shape) {
                Shape.Circle -> drawCirclePulse(color, offset, boxSize, alpha)
                Shape.Square -> drawSquarePulse(color, offset, boxSize, alpha)
                Shape.Squircle -> drawSquirclePulse(color, offset, boxSize, alpha)
            }
            currentRow++
            offset += Offset(0F, boxSize.height + padding.value)
            if (currentRow % rowCount == 0) {
                currentColumn++
                currentRow = 0
                offset = Offset((offset.x + boxSize.width) + padding.value, 0F)
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
        center = offset + Offset(size.width / 2F, size.height / 2F),
        alpha = alpha,
        radius = size.height / 2F
    )
}

enum class Shape {
    Circle,
    Square,
    Squircle
}