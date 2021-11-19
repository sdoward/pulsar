package com.sdoward.pulsar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.sdoward.pulsar.ui.theme.PlusarTheme
import java.time.Instant
import java.util.*


const val MILLISECONDS_IN_DAY = 86_400_000
const val START_OF_2020 = 1577836800000

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlusarTheme {
                Surface(color = MaterialTheme.colors.background) {
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
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PlusarTheme {
        Greeting("Android")
    }
}