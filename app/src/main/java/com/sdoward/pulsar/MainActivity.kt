package com.sdoward.pulsar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sdoward.pulsar.ui.theme.PlusarTheme
import kotlin.math.roundToInt

const val MILLISECONDS_IN_DAY = 86_400_000
const val START_OF_2020 = 1577836800000

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val valueRange = 0..1000L
        val values = (0..364).map {
            valueRange.random().toFloat() / 1000F
        }
        setContent {
            PlusarTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val shapeExpanded = remember {
                        mutableStateOf(false)
                    }
                    val colorExpanded = remember {
                        mutableStateOf(false)
                    }
                    val styleExpanded = remember {
                        mutableStateOf(false)
                    }
                    val shape = remember {
                        mutableStateOf(Shape.Circle)
                    }
                    val color = remember {
                        mutableStateOf(Color.Blue)
                    }
                    val padding = remember {
                        mutableStateOf(4F)
                    }
                    val style = remember {
                        mutableStateOf(StyleState())
                    }
                    val pulseSize = remember {
                        mutableStateOf(90F)
                    }
                    val rowCount = remember {
                        mutableStateOf(7F)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        NumberControl(
                            label = "Pulse Count",
                            value = pulseSize.value,
                            min = 1F,
                            max = 364F
                        ) {
                            pulseSize.value = it
                        }
                        NumberControl(
                            label = "Row Count",
                            value = rowCount.value,
                            min = 1F,
                            max = 30F
                        ) {
                            rowCount.value = it
                        }
                        NumberControl(label = "Padding", value = padding.value) {
                            padding.value = it
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ShapeControl(shapeExpanded, shape)
                        ColorControl(colorExpanded, color)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        StyleControl(
                            styleExpanded,
                            style
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        PulsarCore(
                            modifier = Modifier.fillMaxSize(),
                            values = values.take(pulseSize.value.roundToInt()),
                            shape = shape.value,
                            color = color.value,
                            style = style.value.getSelected(),
                            rowCount = rowCount.value.roundToInt(),
                            pulsePadding = padding.value.dp
                        )
                    }
                }
            }
        }
    }
}

data class StyleState(
    val alpha: Style.Alpha = Style.Alpha(),
    val size: Style.Size = Style.Size(),
    val selectedId: Int = 0
) {
    fun getSelected() = when (selectedId) {
        0 -> component1()
        1 -> component2()
        2 -> Style.AlphaSize(component1(), component2())
        else -> error("Unknown value")
    }
}

@Composable
private fun OvershootControl(
    value: MutableState<StyleState>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.25F)) {
            Text(text = "Overshoot", style = MaterialTheme.typography.subtitle1)
            Text(text = "${value.value.size.overShoot}")
        }
        Slider(
            value = value.value.size.overShoot,
            valueRange = 1.0F..3.0F,
            steps = 10,
            onValueChange = {
                val size = Style.Size(overShoot = it)
                value.value = value.value.copy(size = size)
            })
    }
}

@Composable
private fun NumberControl(
    label: String,
    value: Float,
    min: Float = 0F,
    max: Float = 20F,
    steps: Int = max.roundToInt(),
    listener: (Float) -> Unit
) {
    val thisValue = remember {
        mutableStateOf(value)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.25F)) {
            Text(text = "$label", style = MaterialTheme.typography.subtitle1)
            Text(text = "${thisValue.value}", maxLines = 1)
        }
        Slider(
            value = thisValue.value,
            valueRange = min..max,
            steps = steps,
            onValueChange = {
                thisValue.value = it
                listener(it)
            })
    }
}

@Composable
private fun StyleControl(
    expanded: MutableState<Boolean>,
    style: MutableState<StyleState>
) {
    Column {
        Box {
            Button(
                modifier = Modifier.padding(16.dp),
                onClick = { expanded.value = true }) {
                Text(text = style.value.getSelected()::class.java.simpleName)
            }
            DropdownMenu(expanded = expanded.value, onDismissRequest = {}) {
                listOf("Alpha", "Size", "AlphaSize").forEachIndexed { index, name ->
                    DropdownMenuItem(onClick = {
                        expanded.value = false
                        style.value = style.value.copy(selectedId = index)
                    }) {
                        Text(text = name)
                    }
                }
            }
        }
        if (style.value.getSelected() is Style.Alpha || style.value.getSelected() is Style.AlphaSize) {
            NumberControl(
                label = "Alpha Max",
                value = style.value.alpha.max,
                min = style.value.alpha.min,
                max = 1F,
                steps = 20
            ) {
                style.value = style.value.copy(alpha = style.value.alpha.copy(max = it))
            }
            NumberControl(
                label = "Alpha Min",
                value = style.value.alpha.min,
                min = 0F,
                max = style.value.alpha.max,
                steps = style.value.alpha.max.roundToInt() * 20
            ) {
                style.value = style.value.copy(alpha = style.value.alpha.copy(min = it))
            }
        }
        if (style.value.getSelected() is Style.Size || style.value.getSelected() is Style.AlphaSize) {
            OvershootControl(value = style)
        }
    }
}

@Composable
private fun ShapeControl(
    expanded: MutableState<Boolean>,
    shape: MutableState<Shape>
) {
    Box {
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = { expanded.value = true }) {
            Text(text = shape.value.name)
        }
        DropdownMenu(expanded = expanded.value, onDismissRequest = {}) {
            Shape.values().forEach {
                DropdownMenuItem(onClick = {
                    expanded.value = false
                    shape.value = it
                }) {
                    Text(text = it.name)
                }
            }
        }
    }
}

@Composable
private fun ColorControl(
    expanded: MutableState<Boolean>,
    color: MutableState<Color>
) {
    Box {
        Button(
            modifier = Modifier.padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = color.value
            ),
            onClick = { expanded.value = true }) {
            Text(text = "Color", color = Color.White)
        }
        DropdownMenu(expanded = expanded.value, onDismissRequest = {}) {
            listOf(
                Color.Black,
                Color.Red,
                Color.Blue,
                Color.Cyan,
                Color.Green,
                Color.Magenta,
                Color.Yellow
            ).forEach {
                DropdownMenuItem(onClick = {
                    expanded.value = false
                    color.value = it
                }) {
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(32.dp)
                            .background(it)
                    )
                }
            }
        }
    }
}