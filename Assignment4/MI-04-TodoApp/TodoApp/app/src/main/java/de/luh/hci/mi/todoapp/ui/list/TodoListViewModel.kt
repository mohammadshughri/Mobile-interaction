package de.luh.hci.mi.todoapp.ui.list

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.luh.hci.mi.todoapp.TodoApp
import de.luh.hci.mi.todoapp.data.Todo
import de.luh.hci.mi.todoapp.data.TodoRepository
import kotlinx.coroutines.launch

// ViewModel for showing a list of todo items.
class TodoListViewModel(
    private val repository: TodoRepository, // the underlying repository (data model)
) : ViewModel() {

    // An observable list of todo items.
    val todos = repository.getTodos()

    // Stores a just-deleted todo item, in case deletion should be undone.
    var deletedTodo by mutableStateOf<Todo?>(null)
        private set

    // Deletes the given todo.
    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            deletedTodo = todo
            repository.deleteTodo(todo)
        }
    }

    // Undoes the last deletion.
    fun undoDelete() {
        deletedTodo?.let { todo ->
            viewModelScope.launch {
                repository.insertTodo(todo)
                deletedTodo = null
            }
        }
    }

    // Sets the done-state for the given todo.
    fun todoDone(todo: Todo, isDone: Boolean) {
        viewModelScope.launch {
            repository.insertTodo(
                todo.copy(isDone = isDone)
            )
        }
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
                TodoListViewModel(repository)
            }
        }
    }

}