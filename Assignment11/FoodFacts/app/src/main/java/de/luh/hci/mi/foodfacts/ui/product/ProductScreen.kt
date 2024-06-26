package de.luh.hci.mi.foodfacts.ui.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import org.json.JSONObject

@Composable
fun ProductScreen(
    onNavigate: (route: String) -> Unit, // used to navigate to another screen
    viewModel: ProductViewModel,
) {
    // LazyColumn to display content in a scrollable column
    LazyColumn(
        modifier = Modifier
            .fillMaxSize() // Fill the maximum size of the parent
            .padding(16.dp), // Padding around the entire column
        horizontalAlignment = Alignment.CenterHorizontally // Center align content horizontally
    ) {
        // Display the barcode at the top
        item {
            Text(
                text = "EAN: ${viewModel.barcode}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // Check if any of the product details are available to display
        if (viewModel.name.isNotEmpty() || viewModel.quantity.isNotEmpty() || viewModel.nutriments.isNotEmpty() || viewModel.imageUrl.isNotEmpty() || viewModel.allergens.isNotEmpty()) {
            // Card to hold product details
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth() // Fill the width of the parent
                        .padding(8.dp), // Padding around the card
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface, // Card background color
                        contentColor = MaterialTheme.colorScheme.onSurface // Card content color
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp) // Padding inside the card
                    ) {
                        // Display product name if available
                        if (viewModel.name.isNotEmpty()) {
                            Text(
                                text = "Name",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = viewModel.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        }

                        // Display product quantity if available
                        if (viewModel.quantity.isNotEmpty()) {
                            Text(
                                text = "Quantity",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Text(
                                text = viewModel.quantity,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        }

                        // Display product nutriments if available
                        if (viewModel.nutriments.isNotEmpty()) {
                            Text(
                                text = "Nutriments",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            // Parse and display each nutriment key-value pair
                            val nutriments = JSONObject(viewModel.nutriments)
                            for (key in nutriments.keys()) {
                                Text(
                                    text = "$key: ${nutriments.getString(key)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        }

                        // Display allergens if available
                        if (viewModel.allergens.isNotEmpty()) {
                            Text(
                                text = "Allergens",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Text(
                                text = viewModel.allergens,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        }

                        // Display product image if available
                        if (viewModel.imageUrl.isNotEmpty()) {
                            Text(
                                text = "Image",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Image(
                                painter = rememberImagePainter(data = viewModel.imageUrl),
                                contentDescription = "Product Image",
                                modifier = Modifier
                                    .fillMaxWidth() // Fill the width of the parent
                                    .aspectRatio(1.0f) // Maintain aspect ratio
                                    .padding(bottom = 8.dp),
                                contentScale = ContentScale.Fit // Fit image within the space
                            )
                        }
                    }
                }
            }
        }
    }
}
