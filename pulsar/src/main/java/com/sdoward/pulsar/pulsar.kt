package com.sdoward.pulsar

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.util.*
import kotlin.math.ceil
import kotlin.math.min

const val MILLISECONDS_IN_DAY = 86_400_000
const val START_OF_2020 = 1577836800000

@Preview
@Composable
internal fun PulsarChartPreview() {
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
        shape = Shape.Circle,
        color = Color.Green,
        style = Style.AlphaSize(Style.Alpha(max = 0.6F), Style.Size(overShoot = 1.8F)),
        pulsePadding = 0.dp
    )
}

@Preview
@Composable
internal fun PulsarCorePreview() {
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
        shape = Shape.Circle,
        style = Style.AlphaSize(),
        rowCount = 7,
        rowStart = 0,
        values = alphas
    )
}

@Composable
fun PulsarChart(
    modifier: Modifier = Modifier,
    contributions: Map<Date, Long>,
    shape: Shape = Shape.Square,
    color: Color = Color.Red,
    style: Style = Style.Alpha(),
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
        values = alphas,
        shape = shape,
        style = style,
        pulsePadding = pulsePadding
    )
}

@Composable
fun PulsarCore(
    modifier: Modifier,
    color: Color,
    shape: Shape = Shape.Squircle,
    rowCount: Int = 7,
    rowStart: Int = 0,
    style: Style = Style.Alpha(),
    values: List<Float>,
    pulsePadding: Dp = 4.dp
) {
    if (rowStart >= rowCount) {
        throw IllegalStateException("RowStart cannot be higher than RowCount. RowStart: $rowStart RowCount: $rowCount")
    }
    val columnCount = ceil(values.size.toFloat() / rowCount.toFloat())
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
        values.forEach { value ->
            val alpha = when (style) {
                is Style.Alpha -> value * (style.max - style.min) + style.min
                is Style.Size -> 1F
                is Style.AlphaSize -> value * (style.alpha.max - style.alpha.min) + style.alpha.min
            }
            when (shape) {
                Shape.Circle -> drawCirclePulse(color, offset, boxSize, style, alpha, value)
                Shape.Square -> drawSquarePulse(color, offset, boxSize, style, alpha, value)
                Shape.Squircle -> drawSquirclePulse(color, offset, boxSize, style, alpha, value)
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

private fun DrawScope.drawSquarePulse(
    color: Color,
    offset: Offset,
    size: Size,
    style: Style,
    alpha: Float,
    pulseValue: Float,
) {
    val rectifiedSize = when (style) {
        is Style.Alpha -> size
        is Style.Size -> (size * pulseValue) * style.overShoot
        is Style.AlphaSize -> (size * pulseValue) * style.size.overShoot
    }
    val sizeDelta = (size.height - rectifiedSize.height) / 2
    drawRect(
        color = color,
        topLeft = offset + Offset(sizeDelta, sizeDelta),
        alpha = alpha,
        size = rectifiedSize
    )
}

private fun DrawScope.drawSquirclePulse(
    color: Color,
    offset: Offset,
    size: Size,
    style: Style,
    alpha: Float,
    pulseValue: Float
) {
    val rectifiedSize = when (style) {
        is Style.Alpha -> size
        is Style.Size -> (size * pulseValue) * style.overShoot
        is Style.AlphaSize -> (size * pulseValue) * style.size.overShoot
    }
    val sizeDelta = (size.height - rectifiedSize.height) / 2
    drawRoundRect(
        color = color,
        topLeft = offset + Offset(sizeDelta, sizeDelta),
        alpha = alpha,
        size = rectifiedSize,
        cornerRadius = CornerRadius(rectifiedSize.width / 8F, rectifiedSize.height / 8F)
    )
}

private fun DrawScope.drawCirclePulse(
    color: Color,
    offset: Offset,
    size: Size,
    style: Style,
    alpha: Float,
    pulseValue: Float
) {
    val radius = when (style) {
        is Style.Alpha -> size.height / 2F
        is Style.Size -> ((size.height / 2F) * pulseValue) * style.overShoot
        is Style.AlphaSize -> ((size.height / 2F) * pulseValue) * style.size.overShoot
    }
    drawCircle(
        color = color,
        center = offset + size.center,
        alpha = alpha,
        radius = radius
    )
}

enum class Shape {
    Circle,
    Square,
    Squircle
}

sealed class Style {
    data class Alpha(val min: Float = 0F, val max: Float = 1F) : Style()
    data class Size(val overShoot: Float = 1F) : Style()
    data class AlphaSize(val alpha: Alpha = Alpha(), val size: Size = Size()) : Style()
}