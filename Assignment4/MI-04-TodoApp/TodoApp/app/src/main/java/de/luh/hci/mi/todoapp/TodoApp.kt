package de.luh.hci.mi.todoapp

import android.app.Application
import android.util.Log
import de.luh.hci.mi.todoapp.data.TodoDatabase
import de.luh.hci.mi.todoapp.data.TodoRepository
import de.luh.hci.mi.todoapp.data.TodoRepositoryImpl

// The application instance exists as long as the app executes and is therefore used for app-wide
// data that is used in activities.
class TodoApp : Application() {
    // officially: use Dagger-Hilt dependency injection, rather than manual dependency injection
    // https://developer.android.com/training/dependency-injection
    // https://developer.android.com/training/dependency-injection/manual

    private lateinit var db: TodoDatabase
    lateinit var repository: TodoRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val databaseFile = applicationContext.getDatabasePath("todos.db")
        log("databaseFile: $databaseFile")
        db = TodoDatabase(databaseFile)
        repository = TodoRepositoryImpl(db)

        val dl = applicationContext.databaseList()
        log("<databases>")
        for (d in dl) log(d)
        log("</databases>")
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }
}