package de.luh.hci.mi.rsvpapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mi.rsvpapp.ui.theme.Purple40
import de.luh.hci.mi.rsvpapp.ui.theme.Purple80
import de.luh.hci.mi.rsvpapp.ui.theme.RSVPAppTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToLong

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RSVPAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Screen()
                }
            }
        }
    }
}

// The integer array of string resource identifiers.
val textIds = mapOf(
    R.string.text1 to "Text 1",
    R.string.text2 to "Text 2",
    R.string.text3 to "Text 3"
)

// Computes the number of milliseconds per word given the words-per-minute rate.
private fun wpmToMs(wpm: Float): Long {
    // x wpm = x/60 wps = x/60000 wpms
    // 200 wpm => 300 mspw
    // 700 wpm =>  86 mspw
    return (60000 / wpm).roundToLong()
}

@Composable
fun Screen(modifier: Modifier = Modifier) {
    var running by remember { mutableStateOf(false) }
    var ticks by remember { mutableIntStateOf(-1) }
    var textId by remember { mutableIntStateOf(R.string.text1) }
    val words = stringResource(textId).split(Regex(" +"))
    val word = if (ticks in words.indices) words[ticks] else "START"
    var wpm by remember { mutableFloatStateOf(300f) }

    // advancing the counter
    LaunchedEffect(running, ticks) {
        if (running && ticks < words.size) {
            delay(wpmToMs(wpm))
//            This naive approach of determining the end of a sentence could fail in several ways:
//            It assumes that a dot always indicates the end of a sentence. However, a dot can also be used in abbreviations, decimal numbers, URLs, email addresses, etc.
//            It does not consider other punctuation marks that can indicate the end of a sentence, such as question marks and exclamation marks.
//            It does not handle sentences that end with a dot followed by a quotation mark or parenthesis.
            if (words[ticks].endsWith(".")) {
                delay(750) // Pause for an additional 750 ms at the end of a sentence
            }
            ticks = (ticks + 1) % words.size
        }
        if (ticks >= words.size) {
            running = false
            ticks = -1
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(8.dp)) {
            textIds.forEach { (id, name) ->
                Button(
                    onClick = { textId = id },
                    colors = ButtonDefaults.buttonColors(if (textId == id) Purple40 else Purple80)
                ) {
                    Text(text = name)
                }
            }
        }
        Spacer(Modifier.weight(1.0f))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = word,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

        }
        Spacer(Modifier.weight(1.0f))

        Slider(
            value = wpm,
            onValueChange = { wpm = it },
            valueRange = 200f..700f,
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "$wpm wpm", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = {
                when {
                    ticks == -1 -> {
                        running = true
                        ticks = 0
                    }

                    running -> running = false
                    else -> running = true
                }
            }) {
                Text(
                    text = when {
                        ticks == -1 -> "Start"
                        running -> "Stop"
                        else -> "Continue"
                    }
                )
            }

            Button(onClick = {
                running = false
                ticks = -1
            }) {
                Text(text = "Reset")
            }
        }

    }
}