package de.luh.hci.mi.wifilocation

import android.Manifest
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.app.ActivityCompat
import de.luh.hci.mi.wifilocation.data.LocalDatabase
import de.luh.hci.mi.wifilocation.data.Repository
import de.luh.hci.mi.wifilocation.data.RepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

// Time between WiFi scans (in milliseconds).
const val scanPeriod = 3000L // milliseconds

// The application instance exists as long as the app executes and is therefore used for app-wide
// data that is used in activities.
class WifiLocation : Application() {
    // officially: use Dagger-Hilt dependency injection, rather than manual dependency injection
    // https://developer.android.com/training/dependency-injection
    // https://developer.android.com/training/dependency-injection/manual

    private lateinit var localDb: LocalDatabase

    lateinit var repository: Repository
        private set

    // Used to do WiFi scans.
    private lateinit var wifi: WifiManager

    override fun onCreate() {
        super.onCreate()

        val databaseFile = applicationContext.getDatabasePath("fingerprints.db")
        log("databaseFile: $databaseFile")
        localDb = LocalDatabase(databaseFile)
        repository = RepositoryImpl(localDb)

        wifi = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    }

    // Perform a WiFi scan in a suspend function.
    // https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/suspend-cancellable-coroutine.html
    // https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/callback-flow.html
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun wifiScan(): List<ScanResult> = suspendCancellableCoroutine { continuation ->
        // This object will receive the WiFi scan results.
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                log("onReceive: currentThread = ${Thread.currentThread().name}")
                this@WifiLocation.unregisterReceiver(this)
                // permission should have been granted in MainActivity
                val permissionGranted = ActivityCompat.checkSelfPermission(
                    this@WifiLocation,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                log("permissionGranted: $permissionGranted")
                if (permissionGranted) {
                    // scanResults requires ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission
                    // otherwise list will be empty
                    val srs = wifi.scanResults
                    /*
                    log("${srs.size} scan results")
                    for (sr in srs) {
                        log(sr.toString())
                    }
                     */
                    continuation.resume(srs, null)
                } else {
                    continuation.resumeWithException(SecurityException("scanResults requires ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission"))
                }
            }
        }
        this@WifiLocation.registerReceiver(
            receiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )
        continuation.invokeOnCancellation { this@WifiLocation.unregisterReceiver(receiver) }

        // "Android 9: Each foreground app can scan four times in a 2-minute period."
        // "Android 10+: There is a new developer option to toggle the throttling off for local testing"
        // "(under Developer Options > Networking > Wi-Fi scan throttling)."
        // https://developer.android.com/guide/topics/connectivity/wifi-scan#wifi-scan-throttling
        val success = wifi.startScan() // permission: CHANGE_WIFI_STATE
        log("startScan success: $success")

        // At this point the coroutine is suspended by suspendCancellableCoroutine until onReceive
        // is called and the coroutine is resumed with continuation.resume.
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }
}
