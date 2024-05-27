package de.luh.hci.mi.todoapp.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteDatabase.openOrCreateDatabase
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

// An SQLite database that stores todo items.
// officially: Use Room abstraction layer rather than SQLite directly.
// Room is an object-relational mapping library.
// https://developer.android.com/topic/architecture/data-layer
// https://developer.android.com/training/data-storage/room
// https://developer.android.com/reference/android/database/sqlite/package-summary
class TodoDatabase(databaseFile: File) {

    // Represents the underlying SQLite database instance.
    private val db: SQLiteDatabase = openOrCreateDatabase(databaseFile,null)

    // Called to inform listeners of changes in the database.
    private var changed: (() -> Unit)? = null

    // Initializes the database object. Creates the "todo" table, if not yet done.
    init {
        val sql = "CREATE TABLE IF NOT EXISTS todo (" +
                "id INTEGER PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "isDone INTEGER NOT NULL DEFAULT 0)" // SQLite does not have booleans
        db.execSQL(sql)
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }

    // Inserts the given todo item into the database. If id is not null and already exists in the
    // database, then the method updates the existing todo.
    fun insertTodo(todo: Todo) {
        val values = ContentValues()
        values.put("title", todo.title)
        values.put("description", todo.description)
        values.put("isDone", todo.isDone)
        assert(todo.id == null || todo.id > 0)
        values.put("id", todo.id)
        log("$todo")
        val row = db.insertWithOnConflict("todo", null, values, CONFLICT_REPLACE)
        if (row == -1L) {
            throw android.database.SQLException("could not insert todo")
        }
        // the state of the database may have changed, inform listeners
        changed?.invoke()
    }

    // If a todo exists in the database with todo.id, then it will be removed from the database.
    fun deleteTodo(todo: Todo) {
        db.delete("todo", "id = ?", arrayOf(todo.id.toString()))
        // the state of the database may have changed, inform listeners
        changed?.invoke()
    }

    // Returns the todo for the given id, if it can be found in the database. Otherwise returns null.
    fun getTodoById(id: Int): Todo? {
        val cursor = db.rawQuery("SELECT * FROM todo WHERE id = ?", arrayOf(id.toString()))
        val todo = if (cursor.moveToNext()) {
            val title = cursor.getString(1)
            val description = cursor.getString(2)
            val isDone = cursor.getInt(3) != 0
            Todo(title, description, isDone, id)
        } else {
            null
        }
        cursor.close()
        return todo
    }

    // Returns the list of all todos in the database.
    // Note: Reading the full table does not scale to a large number of items.
    private fun getTodos(): List<Todo> {
        val todos = mutableListOf<Todo>()
        val cursor = db.rawQuery("SELECT * FROM todo", null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val title = cursor.getString(1)
            val description = cursor.getString(2)
            val isDone = cursor.getInt(3) != 0
            todos.add(Todo(title, description, isDone, id))
        }
        cursor.close()
        return todos
    }

    // Represents the current list of todo items. A Flow is an observable stream to which
    // UI elements can implicitly subscribe to receive updates.
    // When "changed" is called, it reads the current list of todo items from the database into a
    // list. Then it sends the list to an internal channel, which will cause the flow to update.
    fun getTodosAsFlow(): Flow<List<Todo>> {
        log("getTodos begin")
        val flow = callbackFlow {
            // we are in a coroutine scope here
            changed = {
                log("changed begin: ${Thread.currentThread().name}")
                val todos = getTodos()
                trySend(todos) // send the updated list of todos to a channel
                log("changed end")
            }
            changed?.invoke() // initial flow state

            // wait for the consumer to cancel the coroutine
            awaitClose {
                log("getTodos awaitClose block")
                changed = null
            }
        }
        log("getTodos end")
        return flow
    }

}
