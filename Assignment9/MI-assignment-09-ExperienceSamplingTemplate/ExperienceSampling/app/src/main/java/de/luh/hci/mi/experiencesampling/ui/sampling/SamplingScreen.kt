package de.luh.hci.mi.experiencesampling.ui.sampling

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mi.experiencesampling.R
import de.luh.hci.mi.experiencesampling.ui.statistics.RadioGroup
import kotlinx.coroutines.launch

@Composable
fun SamplingScreen(
    navigateBack: () -> Unit, // navigate back to previous screen
    viewModel: SamplingViewModel // the view model of this screen
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val thankYouMessage = stringResource(R.string.thank_you)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.submit()
                scope.launch {
                    snackbarHostState.showSnackbar(thankYouMessage)
                    navigateBack()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.submit)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.padding(8.dp))
            Text(stringResource(R.string.context), fontSize = 24.sp)
            Text(stringResource(R.string.context_question))
            RadioGroup(viewModel.contexts, viewModel.selectedContext, viewModel::onContextSelected)
            LazyColumn(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(viewModel.items) { item ->
                    Spacer(Modifier.padding(8.dp))
                    Text(item.title, fontSize = 24.sp)
                    if (item.question.isNotBlank()) {
                        Text(item.question)
                    }
                    Row {
                        Text(item.negativeEnd)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(item.positiveEnd)
                    }
                    val sliderState = viewModel.sliderState(item)
                    Slider(
                        value = sliderState.value,
                        onValueChange = { sliderState.value = it },
                        steps = 4,
                        valueRange = 0f..5f
                    )
                    Spacer(Modifier.padding(8.dp))
                }
            }
        }
    }
}
