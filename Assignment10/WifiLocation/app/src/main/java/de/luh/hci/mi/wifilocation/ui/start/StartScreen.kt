package de.luh.hci.mi.wifilocation.ui.start

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mi.wifilocation.Routes

@Composable
fun StartScreen(
    onNavigate: (route: String) -> Unit, // used to navigate to another screen
    viewModel: StartViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.padding(24.dp))
        Text("Welcome to WiFi-Locator!", fontSize = 24.sp)
        Spacer(Modifier.padding(24.dp))
        Button(onClick = { onNavigate(Routes.MEASURE) }) {
            Text("Measure")
        }
        Spacer(Modifier.padding(24.dp))
        Button(onClick = { onNavigate(Routes.LOCATE) }) {
            Text("Locate")
        }
        Spacer(Modifier.padding(24.dp))
        Button(onClick = viewModel::logFingerprints) {
            Text("Log Fingerprints")
        }
    }
}