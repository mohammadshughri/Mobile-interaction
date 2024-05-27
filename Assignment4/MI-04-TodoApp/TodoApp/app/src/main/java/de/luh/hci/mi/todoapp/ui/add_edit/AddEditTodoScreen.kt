package de.luh.hci.mi.todoapp.ui.add_edit

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

// A screen to add a new todo item or to edit an existing one.
@Composable
fun AddEditTodoScreen(
    navigateBack: () -> Unit, // navigate away from edit screen when finished
    viewModel: AddEditTodoViewModel
) {
    Log.d("Composable", "AddEditTodoScreen")

    // used to show a message
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    // If message is not null, then show it to the user.
    // https://developer.android.com/topic/architecture/ui-layer/events#compose_3
    viewModel.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.messageShown()
        }
    }
    /*
    // observe viewModel.saved and navigate back (onPopBackStack) if true
    // rather: give navigation callback to viewModel.save(onPopBackStack), because this is simpler
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel.saved, lifecycle)  {
        // whenever `saved` changes, check if `saved` is true and navigate
        // when `lifecycle` is at least STARTED
        snapshotFlow { viewModel.saved }
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .first {
                if (it) onPopBackStack()
                return@first it
        }
    }
    */

    // Provides a "scaffold" ("Gerüst") for typical parts of an application screen, such as a
    // floating action button, a bottom bar, and the main content.
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.save(navigateBack) }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            TextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                placeholder = {
                    Text(text = "Title")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                placeholder = {
                    Text(text = "Description")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 5
            )
        }
    }
}