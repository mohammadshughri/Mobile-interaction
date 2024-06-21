package de.luh.hci.mi.foodfacts

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.luh.hci.mi.foodfacts.ui.product.ProductScreen
import de.luh.hci.mi.foodfacts.ui.product.ProductViewModel
import de.luh.hci.mi.foodfacts.ui.scanning.ScanningScreen
import de.luh.hci.mi.foodfacts.ui.scanning.ScanningViewModel
import de.luh.hci.mi.foodfacts.ui.start.StartScreen
import de.luh.hci.mi.foodfacts.ui.start.StartViewModel
import de.luh.hci.mi.foodfacts.ui.theme.FoodFactsTheme

// The main activity sets the content root, which is a navigation graph. Each "composable" in the
// navigation graph is a separate page/screen of the app.
// https://developer.android.com/jetpack/compose/navigation
// https://developer.android.com/guide/navigation/principles
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate")
        setContent {
            FoodFactsTheme {
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
                    composable(route = Routes.SCANNING) {
                        ScanningScreen(
                            onNavigate = navController::navigate, // executed when button is pressed
                            viewModel(factory = ScanningViewModel.Factory) // create view model (or get from cache)
                        )
                    }

                    // navigation destination
                    composable(
                        route = Routes.PRODUCT + "?barcode={barcode}", // a route with an argument
                        arguments = listOf(navArgument("barcode") {
                            type = NavType.StringType; defaultValue = ""
                        })
                    ) {
                        ProductScreen(
                            onNavigate = navController::navigate,
                            viewModel(factory = ProductViewModel.Factory) // create view model (or get from cache)
                        )
                    }
                }
            }
        }

        // Request permissions, if not done yet.
        if (requiredPermissionsGranted()) {
            log("all permissions granted")
        } else {
            log("not all permissions granted")
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    // Checks if all elements of REQUIRED_PERMISSIONS (a string array)
    // pass the given predicate function.
    private fun requiredPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        // For each required permission: Check whether it has been granted (or denied).
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // Called as a result of requesting permissions (see in onCreate above).
    // Finishes the activity if not all the permissions have been granted.
    @Deprecated("This method has been deprecated in favor of using the Activity Result API.")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        log("requestCode: $requestCode")
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            log("${permissions.size} permissions:")
            var notAllGranted = false
            for (i in permissions.indices) {
                val permission = permissions[i]
                val granted = grantResults[i] == PackageManager.PERMISSION_GRANTED
                if (!granted) notAllGranted = true
                log("$permission: ${if (granted) "granted" else "denied"}")
            }
            // if not all permissions are granted, end this activity
            // (actually, should inform the user that the app cannot run because of missing permissions)
            if (notAllGranted) {
                log("onRequestPermissionsResult: not all permissions granted, finishing MainActivity")
                finish()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1
        private val REQUIRED_PERMISSIONS =
            arrayOf(
                Manifest.permission.CAMERA // Protection level: dangerous
            )
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

}