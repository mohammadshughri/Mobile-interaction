package de.luh.hci.mi.experiencesampling.ui.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.luh.hci.mi.experiencesampling.ui.theme.Pink40
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(
    navigateBack: () -> Unit,
    viewModel: StatisticsViewModel
) {
    val selectedContext = viewModel.selectedContext
    val contexts = viewModel.contexts

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RadioGroup(contexts, selectedContext, viewModel::onContextSelected)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(viewModel.items) { item ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.title,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = item.question,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val ratingState = viewModel.ratingState(item)
                    RatingBar(
                        rating = ratingState.value,
                        startLabel = item.negativeEnd,
                        endLabel = item.positiveEnd
                    )
                }
            }
        }
    }
}


@Composable
fun RadioGroup(options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        options.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = { onOptionSelected(option) }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = { onOptionSelected(option) }
                )
                Text(text = option)
            }
        }
    }
}

@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Float = 0f,
    stars: Int = 5,
    color: Color = Pink40,
    startLabel: String = "",
    endLabel: String = ""
) {
    assert(stars in 0..10)
    assert(rating in 0f..10f)
    assert(rating <= stars)
    val ratingStars = rating.roundToInt()
    val restStars = stars - ratingStars
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (startLabel.isNotBlank() || endLabel.isNotBlank()) {
            Text("(%.1f)".format(rating), modifier = modifier.padding(start = 8.dp))
        }
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(startLabel, modifier = modifier.padding(end = 8.dp))
            repeat(ratingStars) {
                Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = color)
            }
            repeat(restStars) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }
            Text(endLabel, modifier = modifier.padding(start = 8.dp))
        }
    }
}

@Preview
@Composable
fun RatingPreview() {
    RatingBar(rating = 2.5f, startLabel = "very bad", endLabel = "very good")
}
