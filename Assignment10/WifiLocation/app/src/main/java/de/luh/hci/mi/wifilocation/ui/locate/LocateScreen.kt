package de.luh.hci.mi.wifilocation.ui.locate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mi.wifilocation.data.LocationDistance

const val maxResults = 10

@Composable
fun LocateScreen(
    navigateBack: () -> Unit,
    viewModel: LocateViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Current Location: ${viewModel.currentLocation}", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.scanResults.isEmpty()) {
            Text("Scan running...", fontSize = 18.sp)
        } else {
            Text("Number of Scan Results: ${viewModel.scanResults.size}", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(viewModel.locationsDistances.take(maxResults)) { locationDistance ->
                    LocationDistanceItem(locationDistance)
                }
                if (viewModel.locationsDistances.size > maxResults) {
                    item {
                        Text(
                            "...",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = navigateBack) {
            Text("Back")
        }
    }
}

@Composable
fun LocationDistanceItem(locationDistance: LocationDistance) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = locationDistance.location, fontSize = 16.sp)
        Text(text = "Distance: ${locationDistance.distance}", fontSize = 16.sp)
    }
}
