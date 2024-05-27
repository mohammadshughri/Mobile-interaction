package de.luh.hci.mi.todoapp.ui.add_edit

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.luh.hci.mi.todoapp.TodoApp
import de.luh.hci.mi.todoapp.data.Todo
import de.luh.hci.mi.todoapp.data.TodoRepository
import kotlinx.coroutines.launch

// ViewModel for adding new todo items and for editing todo items.
class AddEditTodoViewModel(
    private val repository: TodoRepository, // the underlying repository (data model)
    savedStateHandle: SavedStateHandle // a map that contains the todoId
) : ViewModel() {

    // The id of the todo item to edit or null if a new item is being created.
    private val todoId: Int?

    // Observable state for the title text field.
    var title by mutableStateOf("")

    // Observable state for the description text field.
    var description by mutableStateOf("")

    // The "done" flag is not editable in this view.
    private var isDone = false

    // Observable string for message output.
    var message by mutableStateOf<String?>(null)
        private set

    // Indicates whether changes have been saved. Not needed in this variant.
    // var saved by mutableStateOf(false)
    //    private set

    // Initializes the view model by loading the todo item from the repository.
    // Done asynchronously here, because repository methods are typically suspend functions.
    init {
        // The todoId argument is available in a key-value-map (savedStateHandle).
        var id = savedStateHandle.get<Int>("todoId")
        if (id == -1) id = null
        todoId = id
        if (todoId != null) {
            // asynchronously get todo using the id
            viewModelScope.launch {
                repository.getTodoById(todoId)?.let { todo ->
                    title = todo.title
                    description = todo.description
                    isDone = todo.isDone
                }
            }
        }
    }

    // Saves data of this view model to the repository (the data model).
    fun save(navigateBack: () -> Unit) {
        viewModelScope.launch {
            if (title.isBlank()) {
                // Setting message is observed by the UI and a popup will be shown.
                message = "The title cannot be empty"
            } else {
                repository.insertTodo(
                    Todo(
                        title = title,
                        description = description,
                        isDone = isDone,
                        id = todoId
                    )
                )
                // saved = true
                navigateBack()
            }
        }
    }

    // Called by the view to indicate that the message has been shown.
    fun messageShown() {
        message = null
    }

    // Called when this ViewModel is no longer used and will be destroyed. Can be used for cleanup.
    override fun onCleared() {
        log("onCleared")
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }

    companion object {
        // Companion object for creating the view model in the right lifecycle scope.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TodoApp
                val repository = app.repository
                val savedStateHandle = this.createSavedStateHandle()
                AddEditTodoViewModel(
                    repository,
                    savedStateHandle
                )
            }
        }
    }

}