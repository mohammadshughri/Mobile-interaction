package de.luh.hci.mi.vibrationpattern

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import de.luh.hci.mi.vibrationpattern.ui.theme.VibrationPatternTheme

class MainActivity : ComponentActivity() {
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vibrator = getSystemService(Vibrator::class.java)
        setContent {
            View()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun View() {
        VibrationPatternTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.LightGray
            ) {
                Column {
                    Text(text = "Play vibration pattern")
                    // Text to display if device has a vibrator
                    Text(text = "Device has a vibrator: ${vibrator.hasVibrator()}")
                    // Text to display if vibrator has amplitude control
                    Text(text = "Vibrator has amplitude control: ${vibrator.hasAmplitudeControl()}")

                    Button(
                        onClick = { vibrator.vibrate(HEART) },
                    ) {
                        Text("Heartbeat")
                    }
                    Button(
                        onClick = { vibrator.vibrate(SOS) },
                    ) {
                        Text("SOS")
                    }
                    Button(
                        onClick = { vibrator.vibrate(WALTZ) },
                    ) {
                        Text("Walts (3/4 time)")
                    }
                    // Buttons for custom vibration patterns
                    Button(
                        onClick = { vibrator.vibrate(EMERGENCY_ALERT) },
                    ) {
                        Text("Emergency Alert")
                    }
                    Button(
                        onClick = { vibrator.vibrate(GENTLE_BREEZE) },
                    ) {
                        Text("Gentle Breeze")
                    }
                    Button(
                        onClick = { vibrator.cancel() },
                    ) {
                        Text("Stop")
                    }                // Button for random pattern
                    Button(
                        onClick = { randomPattern() },
                    ) {
                        Text("Random")
                    }
                }
            }
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