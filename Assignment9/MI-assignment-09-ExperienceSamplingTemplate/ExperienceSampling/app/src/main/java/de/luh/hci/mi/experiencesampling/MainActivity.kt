package de.luh.hci.mi.experiencesampling

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.luh.hci.mi.experiencesampling.ui.sampling.SamplingScreen
import de.luh.hci.mi.experiencesampling.ui.sampling.SamplingViewModel
import de.luh.hci.mi.experiencesampling.ui.start.StartScreen
import de.luh.hci.mi.experiencesampling.ui.start.StartViewModel
import de.luh.hci.mi.experiencesampling.ui.statistics.StatisticsScreen
import de.luh.hci.mi.experiencesampling.ui.statistics.StatisticsViewModel
import de.luh.hci.mi.experiencesampling.ui.theme.ExperienceSamplingTheme


// The main activity sets the content root, which is a navigation graph. Each "composable" in the
// navigation graph is a separate page/screen of the app.
// https://developer.android.com/jetpack/compose/navigation
// https://developer.android.com/guide/navigation/principles
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Launch system settings to request the permission
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
        log("onCreate")
        setContent {
            ExperienceSamplingTheme {
                // The NavController manages navigation in the navigation graph (e.g, back and up).
                val navController = rememberNavController()
                // The NavHost composable is a container for navigation destinations.
                NavHost(
                    navController = navController,
                    startDestination = Routes.START
                ) {
                    // The NavGraph has a composable for each navigation destination.
                    // Each destination is associated with a route (a string).
                    // ViewModels can be scoped to navigation graphs.

                    // navigation destination
                    composable(route = Routes.START) {
                        StartScreen(
                            onNavigate = navController::navigate, // function used to navigate to another destination
                            viewModel(factory = StartViewModel.Factory) // create view model (or get from cache)
                        )
                    }

                    // navigation destination
                    composable(route = Routes.STATISTICS) {
                        StatisticsScreen(
                            navigateBack = navController::popBackStack, // executed when save button is pressed
                            viewModel(factory = StatisticsViewModel.Factory) // create view model (or get from cache)
                        )
                    }

                    // navigation destination
                    composable(route = Routes.SAMPLING) {
                        SamplingScreen(
                            navigateBack = navController::popBackStack, // executed when save button is pressed
                            viewModel(factory = SamplingViewModel.Factory) // create view model (or get from cache)
                        )
                    }

                }
            }
        }
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }

}