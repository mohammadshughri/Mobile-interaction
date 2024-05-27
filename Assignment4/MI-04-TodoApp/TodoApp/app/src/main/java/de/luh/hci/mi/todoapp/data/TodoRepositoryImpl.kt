package de.luh.hci.mi.todoapp.data

import kotlinx.coroutines.flow.Flow

// Implementation of the todo repository interface. It relies on a database as a data source,
// which is provided via the constructor (dependency injection).
// https://developer.android.com/topic/architecture/data-layer
// https://developer.android.com/codelabs/basic-android-kotlin-training-repository-pattern
class TodoRepositoryImpl(
    private val db: TodoDatabase
) : TodoRepository {

    override suspend fun insertTodo(todo: Todo) {
        db.insertTodo(todo)
    }

    override suspend fun deleteTodo(todo: Todo) {
        db.deleteTodo(todo)
    }

    override suspend fun getTodoById(id: Int): Todo? {
        return db.getTodoById(id)
    }

    override fun getTodos(): Flow<List<Todo>> {
        return db.getTodosAsFlow()
    }
}