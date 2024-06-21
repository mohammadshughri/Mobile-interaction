package de.luh.hci.mi.wifilocation.ui.measure

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mi.wifilocation.data.Fingerprint

// Limit the number of scan results to be shown.
const val maxResults = 7

// UI for showing WiFi fingerprints and for saving a fingerprint.
@Composable
fun MeasureScreen(
    navigateBack: () -> Unit, // navigate back to previous screen
    viewModel: MeasureViewModel // the view model of this screen
) {
    // needed for showing a popup message
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->

        // If message is not null, then show it to the user.
        // https://developer.android.com/topic/architecture/ui-layer/events#compose_3
        viewModel.message?.let { message ->
            LaunchedEffect(message) {
                snackbarHostState.showSnackbar(message)
                viewModel.messageShown()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Location selection
            Text("Select Location", fontSize = 24.sp, modifier = Modifier.padding(16.dp))
            viewModel.locations.forEach { location ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    RadioButton(
                        selected = viewModel.selectedLocation == location,
                        onClick = { viewModel.onLocationSelected(location) }
                    )
                    Text(location, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("(${viewModel.fingerprintCounts[location] ?: 0} fingerprints)")
                    Spacer(modifier = Modifier.width(8.dp))
                    if ((viewModel.fingerprintCounts[location] ?: 0) > 0) {
                        Button(onClick = { viewModel.deleteLocation(location) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear")
                        }
                    }
                }
            }

            // Scan results
            if (viewModel.scanResults.isEmpty()) {
                Text("Scan running...", fontSize = 24.sp, modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    items(viewModel.scanResults.take(maxResults)) { result ->
                        Text("SSID: ${result.SSID}, BSSID: ${result.BSSID}, RSSI: ${result.level}", fontSize = 18.sp)
                    }
                }
            }

            // Store button
            if (viewModel.scanResults.isNotEmpty()) {
                Button(
                    onClick = { viewModel.store() },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Store Fingerprint")
                }
            }
        }
    }
}
