package de.luh.hci.mi.todoapp.ui.list

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.luh.hci.mi.todoapp.Routes

// A screen with a list of todo items.
@Composable
fun TodoListScreen(
    onNavigate: (route: String) -> Unit, // used to navigate to another screen
    viewModel: TodoListViewModel
) {
    Log.d("Composable", "TodoListScreen")

    val todos = viewModel.todos.collectAsState(initial = emptyList())

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    // Show a message when a todo is deleted and show an undo button.
    viewModel.deletedTodo?.let { deletedTodo ->
        LaunchedEffect(deletedTodo) {
            val result = snackbarHostState.showSnackbar(
                message = "Todo '${deletedTodo.title}' deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
        }
    }

    // Provides a "scaffold" ("Gerüst") for typical parts of an application screen, such as a
    // floating action button, a bottom bar, and the main content.
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onNavigate(Routes.ADD_EDIT_TODO) // without todoId to create a new todo item
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
    ) { paddingValues ->
        // Show a vertical list (column) of todo items. The items are clickable.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(todos.value) { todo ->
                TodoItem(
                    todo = todo,
                    deleteTodo = viewModel::deleteTodo,
                    todoDone = viewModel::todoDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            // edit item with this id
                            onNavigate(Routes.ADD_EDIT_TODO + "?todoId=${todo.id}")
                        }
                )
            }
        }
    }
}