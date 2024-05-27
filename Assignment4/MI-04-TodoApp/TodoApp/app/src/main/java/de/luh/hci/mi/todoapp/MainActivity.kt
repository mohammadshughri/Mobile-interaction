package de.luh.hci.mi.todoapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.luh.hci.mi.todoapp.ui.add_edit.AddEditTodoScreen
import de.luh.hci.mi.todoapp.ui.add_edit.AddEditTodoViewModel
import de.luh.hci.mi.todoapp.ui.list.TodoListScreen
import de.luh.hci.mi.todoapp.ui.list.TodoListViewModel
import de.luh.hci.mi.todoapp.ui.theme.TodoAppTheme

// The main activity sets the content root, which is a navigation graph. Each "composable" in the
// navigation graph is a separate page/screen of the app.
// https://developer.android.com/jetpack/compose/navigation
// https://developer.android.com/guide/navigation/principles
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoAppTheme {
                // The NavController manages navigation in the navigation graph (e.g, back and up).
                val navController = rememberNavController()
                // The NavHost composable is a container for navigation destinations.
                NavHost(
                    navController = navController,
                    startDestination = Routes.TODO_LIST
                ) {
                    // The NavGraph has a composable for each navigation destination.
                    // Each destination is associated with a route (a string).
                    // ViewModels can be scoped to navigation graphs.

                    // navigation destination 1:
                    // This composable shows the list of existing todos.
                    composable(route = Routes.TODO_LIST) {
                        TodoListScreen(
                            onNavigate = navController::navigate, // function used to navigate to another destination
                            viewModel(factory = TodoListViewModel.Factory) // create view model (or get from cache)
                        )
                    }

                    // navigation destination 2:
                    // This composable can be started with an argument that identifies the todo to
                    // edit. If the todoId is omitted, a new todo will be created.
                    composable(
                        route = Routes.ADD_EDIT_TODO + "?todoId={todoId}", // a route with an optional argument
                        arguments = listOf(navArgument("todoId") {
                            type = NavType.IntType; defaultValue = -1
                        })
                    ) {
                        AddEditTodoScreen(
                            navigateBack = navController::popBackStack, // executed when save button is pressed
                            viewModel(factory = AddEditTodoViewModel.Factory) // create view model (or get from cache)
                        )
                    }
                }
            }
        }
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }

}