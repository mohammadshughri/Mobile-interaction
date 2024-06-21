package de.luh.hci.mi.foodfacts.ui.product

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProductScreen(
    onNavigate: (route: String) -> Unit, // used to navigate to another screen
    viewModel: ProductViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.padding(24.dp))
        Text("EAN: ${viewModel.barcode}")
        if (viewModel.name.isNotEmpty()) {
            Text("${viewModel.name}, ${viewModel.quantity}")
        }
    }
}
