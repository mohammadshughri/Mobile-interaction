package de.luh.hci.mi.vibrationpattern

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mi.vibrationpattern.ui.theme.VibrationPatternTheme

class MainActivity : ComponentActivity() {
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vibrator = getSystemService(Vibrator::class.java)
        setContent {
            @Composable
            fun View() {
                VibrationPatternTheme {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        color = Color.LightGray
                    ) {
                        Column {
                            //The title should be given in fontSize 32.sp and color MaterialTheme.colorScheme.primary.
                            Text(
                                text = "Play vibration pattern",
                                fontSize = 32.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            // Text to display if device has a vibrator
                            Text(
                                text = "Device has a vibrator: ${vibrator.hasVibrator()}",
                                color = MaterialTheme.colorScheme.secondary
                            )
                            // Text to display if vibrator has amplitude control
                            Text(
                                text = "Vibrator has amplitude control: ${vibrator.hasAmplitudeControl()}",
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            // The options should be specified in a set of radio buttons.


                            val radioOptions = listOf(
                                "None",
                                "Heartbeat",
                                "SOS",
                                "Walts (3/4 time)",
                                "Emergency Alert",
                                "Gentle Breeze"
                            )
                            val (selectedOption, onOptionSelected) = remember {
                                mutableStateOf(radioOptions[0])
                            }
                            Column(Modifier.selectableGroup()) {
                                radioOptions.forEach { text ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (text == selectedOption),
                                            onClick = {
                                                onOptionSelected(text)
                                                when (text) {
                                                    "None" -> vibrator.cancel()
                                                    "Heartbeat" -> vibrator.vibrate(HEART)
                                                    "SOS" -> vibrator.vibrate(SOS)
                                                    "Walts (3/4 time)" -> vibrator.vibrate(WALTZ)
                                                    "Emergency Alert" -> vibrator.vibrate(
                                                        EMERGENCY_ALERT
                                                    )

                                                    "Gentle Breeze" -> vibrator.vibrate(
                                                        GENTLE_BREEZE
                                                    )
                                                }
                                            } // `onClick` is now on the RadioButton
                                        )
                                        Text(
                                            text = text,
                                            modifier = Modifier.padding(start = 16.dp),
                                        )
                                    }
                                }
                            }

                        }
                    }
                }
            }
            View()
        }
    }


    fun randomPattern() {
        val randomIndex = (0 until vibes.size).random()
        vibrator.vibrate(vibes[randomIndex])
    }

    companion object {
        private val HEART = VibrationEffect.createWaveform(
            longArrayOf(
                0,
                65,
                297,
                44,
                552 // wait 0ms, vibrate 65 ms, pause 297 ms, vibrate 44 ms, pause 552 ms
            ), 1
        )
        private val SOS = VibrationEffect.createWaveform(
            longArrayOf(
                0, 100, 100, 100, 100, 100, 100,
                300, 100, 300, 100, 300, 100,
                100, 100, 100, 100, 100, 100,
                0, 1000
            ), 1
        )
        private val WALTZ = VibrationEffect.createWaveform(
            longArrayOf(
                0, 100, 200, 70, 230, 70, 230
            ), 1
        )

        // Custom vibration patterns
        private val EMERGENCY_ALERT = VibrationEffect.createWaveform(
            longArrayOf(
                0, 200, 100, 200, 100, 200, 100, 200 // rapid bursts
            ), intArrayOf(255, 150, 255, 50, 255, 80, 255, 60), 1
        )
        private val GENTLE_BREEZE = VibrationEffect.createWaveform(
            longArrayOf(
                0, 500, 1000, 500, 1000, 500, 1000, 500, 1000 // gradual increase, then oscillation
            ), intArrayOf(50, 100, 150, 100, 50, 50, 100, 150, 100), 1
        )
        private val vibes = arrayOf(HEART, SOS, WALTZ, EMERGENCY_ALERT, GENTLE_BREEZE)

    }
}