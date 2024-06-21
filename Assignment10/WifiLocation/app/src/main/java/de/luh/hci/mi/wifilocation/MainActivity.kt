package de.luh.hci.mi.wifilocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.luh.hci.mi.wifilocation.ui.locate.LocateScreen
import de.luh.hci.mi.wifilocation.ui.locate.LocateViewModel
import de.luh.hci.mi.wifilocation.ui.measure.MeasureScreen
import de.luh.hci.mi.wifilocation.ui.measure.MeasureViewModel
import de.luh.hci.mi.wifilocation.ui.start.StartScreen
import de.luh.hci.mi.wifilocation.ui.start.StartViewModel
import de.luh.hci.mi.wifilocation.ui.theme.WifiLocationTheme

// The main activity sets the content root, which is a navigation graph. Each "composable" in the
// navigation graph is a separate page/screen of the app.
// https://developer.android.com/jetpack/compose/navigation
// https://developer.android.com/guide/navigation/principles
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate")
        setContent {
            WifiLocationTheme {
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
                    composable(route = Routes.MEASURE) {
                        MeasureScreen(
                            navigateBack = navController::popBackStack, // executed when back button is pressed
                            viewModel(factory = MeasureViewModel.Factory) // create view model (or get from cache)
                        )
                    }

                    // navigation destination
                    composable(route = Routes.LOCATE) {
                        LocateScreen(
                            navigateBack = navController::popBackStack, // executed when back button is pressed
                            viewModel(factory = LocateViewModel.Factory) // create view model (or get from cache)
                        )
                    }

                }
            }
        }

        // Permission request results will be reported via onRequestPermissionsResult (below).
        // All required permissions need to be declared in the AndroidManifest.xml.
        // Dangerous permissions also have to be requested at runtime.
        // https://developer.android.com/guide/topics/permissions/overview
        // If the app does not declare/request required permissions, security exceptions will be thrown:
        // java.lang.SecurityException: WifiService: Neither user 10168 nor current process has android.permission.ACCESS_WIFI_STATE.
        // java.lang.SecurityException: WifiService: Neither user 10168 nor current process has android.permission.CHANGE_WIFI_STATE.
        // java.lang.SecurityException: Permission denied (missing INTERNET permission?)
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION, // Protection level: dangerous
                Manifest.permission.ACCESS_FINE_LOCATION, // Protection level: dangerous
                // Manifest.permission.ACCESS_WIFI_STATE, // Protection level: normal
                // Manifest.permission.CHANGE_WIFI_STATE, // Protection level: normal
                // Manifest.permission.ACCESS_NETWORK_STATE, // Protection level: normal
                // Manifest.permission.INTERNET, // Protection level: normal
            ), 1
        )

    }

    // Called as a result of a permission check.
    @Deprecated("This method has been deprecated in favor of using the Activity Result API.")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        log("requestCode: $requestCode")
        log("${permissions.size} permissions:")
        var notAllGranted = false
        for (i in permissions.indices) {
            val permission = permissions[i]
            val granted = grantResults[i] == PackageManager.PERMISSION_GRANTED
            if (!granted) notAllGranted = true
            log("$permission: ${if (granted) "granted" else "denied"}")
        }
        // output should be:
        // requestCode: 1
        // 2 permissions:
        // android.permission.ACCESS_COARSE_LOCATION: granted
        // android.permission.ACCESS_FINE_LOCATION: granted

        // if not all permissions are granted, end this activity
        // (actually, should inform the user that the app cannot run because of missing permissions)
        if (notAllGranted) finish()
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

}