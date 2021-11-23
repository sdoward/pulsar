package com.sdoward.pulsar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sdoward.pulsar.ui.theme.PlusarTheme
import java.time.Instant
import java.util.*
import kotlin.math.exp


const val MILLISECONDS_IN_DAY = 86_400_000
const val START_OF_2020 = 1577836800000

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlusarTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val valueRange = 0..1000L
                    val contributions = remember { mutableMapOf<Date, Long>() }
                    (0..364).forEach {
                        val millis = START_OF_2020 * (MILLISECONDS_IN_DAY * it)
                        val date = Date.from(Instant.ofEpochMilli(millis))
                        contributions[date] = valueRange.random()
                    }
                    val shapeExpanded = remember {
                        mutableStateOf(false)
                    }
                    val colorExpanded = remember {
                        mutableStateOf(false)
                    }
                    val shape = remember {
                        mutableStateOf(Shape.Squircle)
                    }
                    val color = remember {
                        mutableStateOf(Color.Blue)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        ShapeControl(shapeExpanded, shape)
                        ColorControl(colorExpanded, color)
                        PulsarChart(
                            contributions = contributions,
                            shape = shape.value,
                            color = color.value
                        )
                    }
                }
            }
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
            Text(text = "Color")
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