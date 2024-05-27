package de.luh.hci.mi.todoapp.data

import kotlinx.coroutines.flow.Flow

// A repository is an interface to (a category of) the app's data.
// The repository interface isolates the data layer from the rest of the app.
// https://developer.android.com/topic/architecture/data-layer
// https://developer.android.com/codelabs/basic-android-kotlin-training-repository-pattern
interface TodoRepository {

    // Inserts the given todo item in the repository, overwriting any existing todo item with the same id.
    suspend fun insertTodo(todo: Todo)

    // Deletes the given todo item from the repository, if it exists.
    suspend fun deleteTodo(todo: Todo)

    // Returns a todo with the given id, if it exists.
    suspend fun getTodoById(id: Int): Todo?

    // Represents the current list of todo items. A Flow is an observable stream to which
    // UI elements can implicitly subscribe to receive updates.
    fun getTodos(): Flow<List<Todo>>
}