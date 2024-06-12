package de.luh.hci.mi.experiencesampling.notifications

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import de.luh.hci.mi.experiencesampling.ui.sampling.SamplingScreen
import de.luh.hci.mi.experiencesampling.ui.sampling.SamplingViewModel
import de.luh.hci.mi.experiencesampling.ui.theme.ExperienceSamplingTheme

// Shows the survey UI and submits the entered data to database server.
// Started as a response to a notification.
class SamplingActivity : ComponentActivity() {
    // Associates the UI with the activity.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate (response to notification)")
        setContent {
            ExperienceSamplingTheme {
                SamplingScreen(
                    navigateBack = this::finish, // end activity when input is done
                    viewModel(factory = SamplingViewModel.Factory) // create view model (or get from cache)
                )
            }
        }
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }

}
