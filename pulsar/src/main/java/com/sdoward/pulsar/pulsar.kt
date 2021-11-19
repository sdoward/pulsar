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
import androidx.compose.ui.unit.dp
import java.util.*

@Composable
fun PlusarChart(contributions: Map<Date, Long>,
                shape: Shape = Shape.Square,
                color: Color = Color.Red
) {
    val boxWH = 32.dp
    val boxSize = Size(boxWH.value, boxWH.value)
    val padding = 2.dp
    val maxValue = contributions.values.maxOf { it }
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        var topOffset = Offset(0F, 0F)
        var dayCount = 0
        contributions.keys.sortedBy { it.toInstant().epochSecond }.forEach { date ->
            val alpha = ((contributions.getValue(date).toFloat() / maxValue))
            when (shape) {
                Shape.Square -> drawSquare(topOffset, alpha, boxSize, color)
                Shape.Circle -> drawCircle(topOffset, alpha, boxSize, color)
                Shape.Squircle -> drawSquircle(topOffset, alpha, boxSize, color)
            }
            topOffset = Offset(topOffset.x, topOffset.y + boxSize.height + padding.value)
            dayCount++
            if (dayCount % 7 == 0) {
                topOffset = Offset(((dayCount / 7F) * boxSize.width) + padding.value, 0F)
            }
        }
    }
}

private fun DrawScope.drawCircle(
    topOffset: Offset,
    alpha: Float,
    boxSize: Size,
    color: Color
) {
    drawCircle(
        color = color,
        alpha = alpha,
        center = topOffset,
        radius = boxSize.height / 2F
    )
}

private fun DrawScope.drawSquare(
    topOffset: Offset,
    alpha: Float,
    boxSize: Size,
    color: Color
) {
    drawRect(
        color = color,
        topLeft = topOffset,
        alpha = alpha,
        size = boxSize
    )
}

private fun DrawScope.drawSquircle(
    topOffset: Offset,
    alpha: Float,
    boxSize: Size,
    color: Color
) {
    drawRoundRect(
        color = color,
        topLeft = topOffset,
        alpha = alpha,
        size = boxSize,
        cornerRadius = CornerRadius(boxSize.width / 8F, boxSize.height / 8F)
    )
}

enum class Shape {
    Circle,
    Square,
    Squircle
}