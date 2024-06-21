package de.luh.hci.mi.wifilocation.ui.locate

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
import de.luh.hci.mi.wifilocation.data.LocationDistance
import de.luh.hci.mi.wifilocation.data.Repository
import de.luh.hci.mi.wifilocation.scanPeriod
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Integer.min

// ViewModel for the locate screen.
class LocateViewModel(
    private val repository: Repository, // the underlying repository (data model)
    private val wifiScan: suspend () -> List<ScanResult>, // used to perform a WiFi scan
) : ViewModel() {

    // The scan result for the current (unknown) location.
    var scanResults: List<ScanResult> by mutableStateOf(listOf())
        private set

    var currentLocation by mutableStateOf("unknown")
        private set

    // Locations with their distance metrics.
    var locationsDistances: List<LocationDistance> by mutableStateOf(listOf())
        private set

    // Gets the set of fingerprints, get the current fingerprint, find the best match.
    init {
        viewModelScope.launch {
            try {
                // get fingerprints for known locations
                val fingerprints = repository.getFingerprints()
                for (fp in fingerprints) log(fp.toString())
                // continuously scan for WiFi base stations
                while (isActive) {
                    scanResults = wifiScan()
                    log("${scanResults.size} scan results")
                    for (result in scanResults) log(result.toString())
                    val currentFingerprint = Fingerprint("unknown", scanResults)
                    bestMatch(currentFingerprint, fingerprints)
                    delay(scanPeriod) // wait before the next scan
                }
            } catch (ex: IOException) {
                log("getFingerprints failed: $ex")
            }
        }
    }

    // Finds the best matching location given the current fingerprint
    // and the set of known (location, fingerprint) pairs.
    private fun bestMatch(currentFingerprint: Fingerprint, fingerprints: List<Fingerprint>) {
        // compute distances to each template fingerprint
        val distances = mutableListOf<LocationDistance>()
        for (fp in fingerprints) {
            distances.add(LocationDistance(fp.location, currentFingerprint.distance(fp)))
        }

        // sort by increasing distance
        distances.sortWith { a: LocationDistance?, b: LocationDistance? ->
            if (a == b) 0
            else if (a == null) -1
            else if (b == null) 1
            else if (a.distance == b.distance) 0
            else if (a.distance < b.distance) -1
            else 1
        }
        locationsDistances = distances
        log("${distances.size} distances:")
        for (d in distances) log(d.toString())

        // compute k-nearest neighbor
        val k = 3
        val votes = mutableMapOf<String, Double>()
        for (i in 0 until min(k, distances.size)) {
            val d = distances[i]
            votes[d.location] = votes.getOrDefault(d.location, 0.0) + 1.0
        }
        var bestLocation: String? = null
        var bestScore = Double.MIN_VALUE
        for ((location, score) in votes) {
            if (bestLocation == null || score > bestScore) {
                bestLocation = location
                bestScore = score
            }
        }
        log("best location = $bestLocation")
        if (bestLocation != null && bestLocation != currentLocation) {
            currentLocation = bestLocation
        }
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
                LocateViewModel(repository, wifiScan)
            }
        }
    }

}