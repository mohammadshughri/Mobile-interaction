package de.luh.hci.mi.experiencesampling.ui.start

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.luh.hci.mi.experiencesampling.R
import de.luh.hci.mi.experiencesampling.Routes
import de.luh.hci.mi.experiencesampling.ui.theme.Purple40
import de.luh.hci.mi.experiencesampling.ui.theme.Purple80

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
        Button(onClick = { onNavigate(Routes.SAMPLING) }) {
            Text(stringResource(R.string.start_survey))
        }
        Spacer(Modifier.padding(24.dp))
        Button(onClick = { onNavigate(Routes.STATISTICS) }) {
            Text(stringResource(R.string.show_statistics))
        }
        Spacer(Modifier.padding(24.dp))
        Button(
            onClick = { viewModel.togglePeriodicSampling() },
            colors = ButtonDefaults.buttonColors(if (viewModel.periodicSampling) Purple80 else Purple40)
        ) {
            if (viewModel.periodicSampling) {
                Text(stringResource(R.string.stop_periodic_sampling))
            } else {
                Text(stringResource(R.string.start_periodic_sampling))
            }
        }
        Spacer(Modifier.padding(24.dp))
    }
}
