package de.luh.hci.mi.atomikkeyboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mi.atomikkeyboard.ui.theme.AtomikKeyboardTheme
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import kotlin.math.cos
import kotlin.math.sin

// Writer for experiment data.
// To locate the data file, device connected via USB:
// Android Studio > Device Manager > (Your Device) > Open in Device Explorer
// /data/data/de.luh.hci.mi.atomikkeyboard/files
// double click: log<timestamp>.txt
private lateinit var writer: BufferedWriter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModel()
        setContent {
            AtomikKeyboardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    View(viewModel)
                }
            }
        }
        writer =
            BufferedWriter(
                OutputStreamWriter(
                    openFileOutput(
                        "log${System.currentTimeMillis()}.txt",
                        0
                    )
                )
            )
    }
}

// The list of target sentences.
val sentences = arrayOf(
    "THE",
    "THE QUICK BROWN FOX JUMPS",
    "MY LAZY DOG SLEEPS WELL",
    "EAST WEST NORTH SOUTH",
    "UP DOWN LEFT RIGHT"
)

// The state belonging to the view.
class ViewModel {
    // The index of the current sentence to input.
    var sentenceIndex by mutableIntStateOf(0)

    // The input made so far.
    var input by mutableStateOf("")

    // Whether or not the input is complete (and correct).
    var completed by mutableStateOf(false)

    // The number of key presses since the start of input of this sentence.
    private var keyPresses = 0

    // Processes a single key press. '<' is backspace.
    fun keyPress(c: Char) {
        if (!completed) {
            if (c == '<') {
                input = input.dropLast(1)
            } else {
                input += c
            }
            if (input == sentences[sentenceIndex]) {
                completed = true
            }
        }
        keyPresses++
        saveKeyPress(c)
    }

    // Saves the current state and starts the next sentence. Called on each press of the next button.
    fun nextSentence() {
        saveSentence()
        sentenceIndex = (sentenceIndex + 1) % sentences.size
        input = ""
        completed = false
        keyPresses = 0
    }

    // Saves the current key press and state in the data file.
    private fun saveKeyPress(character: Char) {
        val timestamp = System.currentTimeMillis()
        writer.write("KEY;$sentenceIndex;$keyPresses;$character;$input;$timestamp\n")
        writer.flush()
    }

    // Saves the current state and edit distance.
    private fun saveSentence() {
        val timestamp = System.currentTimeMillis()
        val sentence = sentences[sentenceIndex]
        val editDistance = editDistance(input, sentence)
        writer.write("END;$sentenceIndex;$sentence;$keyPresses;$input;$editDistance;$timestamp\n")
        writer.flush()
    }

}

@Preview(showBackground = true)
@Composable
fun PreView() {
    val viewModel = ViewModel()
    viewModel.sentenceIndex = 1
    viewModel.input = "input..."
    View(viewModel)
}

// Returns a point on the unit circle.
private fun circlePoint(alpha: Float): Offset {
    val rad = alpha * Math.PI / 180
    return Offset(cos(rad).toFloat(), sin(rad).toFloat())
}

// The points of the hexagon on the unit circle. Will be scaled to the key size.
val circlePoints: List<Offset> by lazy {
    val points = mutableListOf<Offset>()
    for (alpha in 30..390 step 60) {
        points.add(circlePoint(alpha.toFloat()))
    }
    points
}

// Generates a single key hexagon. This is a hexagon polygon in the background and a text in the
// front.
@Composable
fun RowScope.Key(symbol: Char, clicked: (Char) -> Unit) {
    Box(
        modifier = Modifier
            .drawWithCache {
                onDrawBehind {
                    val radius = size.width * 0.5f / cos(30 * Math.PI / 180).toFloat()
                    val centeredPoints = circlePoints.map {
                        it * radius + center
                    }
                    drawPoints(
                        points = centeredPoints,
                        pointMode = PointMode.Polygon,
                        color = Color.Black,
                        strokeWidth = 2f
                    )
                }
            }
            .weight(1f) // each key has a horizontal weight of 1f
            .aspectRatio(1 / cos(30 * Math.PI / 180).toFloat()) // and a fixed aspect ratio
            .clickable { clicked(symbol) }, // when clicked, inform view model
        contentAlignment = Alignment.Center
    ) {
        Text(symbol.toString(), fontSize = 24.sp)
    }
}

// The keyboard is made of rows. '<' is backspace.
// '_' generates empty space of a single key width.
// '.' generate empty space of half a key width.
val keys = arrayOf(
    charArrayOf('B', 'K', 'D', 'G', '_', '_', '<'),      // row 1
    charArrayOf('.', 'C', 'A', 'N', 'I', 'M', 'Q', '.'), // row 2
    charArrayOf('F', 'L', 'E', ' ', 'S', 'Y', 'X'),      // row 3
    charArrayOf('.', 'J', 'H', 'T', 'O', 'P', 'V', '.'), // row 4
    charArrayOf('_', '_', '_', 'R', 'U', 'W', 'Z')       // row 5
)

// Generates the view of the app, consisting of a single column:
// - target sentence
// - input so far
// - ATOMIK keyboard (plus backspace '<' key)
// - button to get to the next sentence
@Composable
fun View(viewModel: ViewModel, modifier: Modifier = Modifier) {
    // When input matches target, text will be green.
    val green = Color(0f, 0.7f, 0f)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = sentences[viewModel.sentenceIndex],
            fontSize = 20.sp,
            modifier = modifier.padding(top = 32.dp, bottom = 16.dp)
        )
        Text(
            text = "${viewModel.input}|",
            color = if (viewModel.completed) green else Color.Black,
            fontSize = 20.sp,
            modifier = modifier.padding(top = 16.dp, bottom = 40.dp)
        )
        // Generate a list of Rows from the keys array.
        // Each row uses weight modifiers among its children to fill the available space.
        keys.map {
            Row(modifier = Modifier.fillMaxWidth()) {
                it.map {
                    when (it) {
                        '.' -> Spacer(modifier = Modifier.weight(0.5f)) // 1/2 key whitespace
                        '_' -> Spacer(modifier = Modifier.weight(1f)) // 1 key whitespace
                        else -> Key(it, viewModel::keyPress) // a regular key (weight 1f)
                    }
                }
            }
        }

        /*
        // In a previous version, an image of the keyboard was used, rather than polygons.
        Image(
            painterResource(R.drawable.atomik_keyboard),
            contentDescription = null,
            Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures { offset: Offset ->
                        Log.d("MainActivity", "$offset")
                    }
                }
        )
        */

        // The spacer pushes the button to the bottom of the screen.
        Spacer(modifier = Modifier.weight(1f))

        // Advance to the next sentence.
        Button(
            onClick = viewModel::nextSentence,
            modifier = modifier
                .padding(32.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Next Sentence")
        }
    }
}

// Returns the smallest of three values.
private fun min(a: Int, b: Int, c: Int): Int {
    return if (a < b) {
        if (a < c) a else c
    } else {
        if (b < c) b else c
    }
}

// Computes the edit distance (Levenshtein distance) between src and dst.
private fun editDistance(src: String, dst: String): Int {
    val s = src.length
    val t = dst.length
    val d = Array(s + 1) { IntArray(t + 1) }
    for (i in 0..s) {
        for (j in 0..t) {
            if (i == 0) d[i][j] = j // special case row 0
            else if (j == 0) d[i][j] = i // special case column 0
            else { // assert: i > 0 && j > 0
                val del = d[i - 1][j] + 1
                val ins = d[i][j - 1] + 1
                val copRep = d[i - 1][j - 1] + (if (src[i - 1] == dst[j - 1]) 0 else 1)
                d[i][j] = min(del, ins, copRep)
            }
        }
    }
    return d[s][t]
}
