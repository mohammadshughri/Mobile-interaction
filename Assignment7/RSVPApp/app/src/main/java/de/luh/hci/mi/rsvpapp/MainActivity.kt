package de.luh.hci.mi.rsvpapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
val textIds = arrayOf(R.string.text1, R.string.text2, R.string.text3)

// Computes the number of milliseconds per word given the words-per-minute rate.
private fun wpmToMs(wpm: Float): Long {
    // x wpm = x/60 wps = x/60000 wpms
    // 200 wpm => 300 mspw
    // 700 wpm =>  86 mspw
    return (60000 / wpm).roundToLong()
}

// The one and only screen of this app.
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
        // todo: change as required
        delay(wpmToMs(wpm))
        ticks = (ticks + 1) % words.size
    }

    Text("implement")
    // @todo: implement

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RSVPAppTheme {
        Screen()
    }
}