package de.luh.hci.mi.todoapp.data

// Represents a single todo item. (Each instance will be a single row in an SQLite database.)
data class Todo(
    val title: String,
    val description: String,
    val isDone: Boolean,
    val id: Int? = null // primary key for database
)
