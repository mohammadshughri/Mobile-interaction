package de.luh.hci.mi.wifilocation.ui.measure

import android.net.wifi.ScanResult
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.luh.hci.mi.wifilocation.WifiLocation
import de.luh.hci.mi.wifilocation.data.Fingerprint
import de.luh.hci.mi.wifilocation.data.Repository
import de.luh.hci.mi.wifilocation.scanPeriod
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException

// ViewModel for the fingerprint measurement screen.
class MeasureViewModel(
    private val repository: Repository, // the underlying repository (data model)
    private val wifiScan: suspend () -> List<ScanResult>, // used to perform a WiFi scan
) : ViewModel() {

    // The list of results of the last WiFi scan.
    var scanResults: List<ScanResult> by mutableStateOf(listOf())

    // The fixed list of locations from the data model.
    val locations = repository.getLocations()

    // The number of fingerprints for a location.
    var fingerprintCounts: Map<String, Int> by mutableStateOf(mapOf())

    // Continuously run WiFi scans using the given scan period.
    // Example scan result:
    // SSID: eduroam, BSSID: 64:d8:14:cc:1c:01, level: -58
    init {
        viewModelScope.launch {
            val locs = repository.getLocations()
            log(locs.toString())
            fingerprintCounts = repository.getFingerprintCounts()
            while (isActive) {
                scanResults = wifiScan()
                log("${scanResults.size} scan results")
                for (sr in scanResults) log(sr.toString())
                delay(scanPeriod)
            }
        }
    }

    // Observable string for message output.
    var message: String? by mutableStateOf(null)
        private set

    // The currently selected location.
    var selectedLocation by mutableStateOf(locations[0])
        private set

    fun onLocationSelected(location: String) {
        selectedLocation = location
    }

    // Submits the sample to the repository.
    fun store() {
        if (selectedLocation.isNotEmpty() && scanResults.isNotEmpty()) {
            val fp = Fingerprint(selectedLocation, scanResults)
            log(fp.toString())
            viewModelScope.launch {
                message = try {
                    log("store::viewModelScope.launch: " + Thread.currentThread().name)
                    repository.storeFingerprint(fp)
                    fingerprintCounts = repository.getFingerprintCounts()
                    "Fingerprint stored for $selectedLocation."
                } catch (ex: IOException) {
                    log("exception trying to store: $ex")
                    "Could not store fingerprint."
                }
            }
        }
    }

    // Deletes the fingerprints for the given location.
    fun deleteLocation(location: String) {
        viewModelScope.launch {
            message = try {
                log("deleteLocation::viewModelScope.launch: " + Thread.currentThread().name)
                repository.deleteLocation(location)
                fingerprintCounts = repository.getFingerprintCounts()
                "$location cleared."
            } catch (ex: IOException) {
                log("exception trying to delete location: $ex")
                "Could not delete location."
            }
        }
    }

    // Makes the message disappear after it has been shown.
    fun messageShown() {
        message = null
    }

    // Called when this ViewModel is no longer used and will be destroyed. Can be used for cleanup.
    override fun onCleared() {
        log("onCleared")
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    companion object {
        // Companion object for creating the view model in the right lifecycle scope.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as WifiLocation
                val repository = app.repository
                val wifiScan = app::wifiScan
                MeasureViewModel(repository, wifiScan)
            }
        }
    }

}
