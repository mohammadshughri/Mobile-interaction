package de.luh.hci.mi.todoapp.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mi.todoapp.data.Todo

@Preview(showBackground = true)
@Composable
fun Preview() {
    TodoItem(
        todo = Todo("title", "description", true, 123),
        deleteTodo = {},
        todoDone = fun(_: Todo, _: Boolean) {}
    )
}

// A single todo item.
@Composable
fun TodoItem(
    todo: Todo,
    deleteTodo: (Todo) -> Unit,
    todoDone: (todo: Todo, isDone: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = todo.isDone,
            onCheckedChange = { isChecked ->
                todoDone(todo, isChecked)
            }
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = todo.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (todo.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = todo.description)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = {
            deleteTodo(todo)
        }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete"
            )
        }
    }
}