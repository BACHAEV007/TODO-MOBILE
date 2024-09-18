package com.example.todomobile

import android.content.Context
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todomobile.ui.theme.TODOMOBILETheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class Task(var text: MutableState<String>, var check: MutableState<Boolean>, var isEditing: MutableState<Boolean>)
data class TaskSerializable(
    val text: String,
    val check: Boolean,
    val isEditing: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TODOMOBILETheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var tasks = remember { mutableStateListOf<Task>() };
                    Greeting(
                        modifier = Modifier.padding(innerPadding),
                        tasks
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier, tasks: MutableList<Task>) {
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current
    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text (
            text = "To Do List",
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )
        Row (
            modifier = modifier
        ){

            TextField(
                value = text,
                onValueChange = { newText ->
                    val maxCharsPerLine = 20
                    val lines = newText.split("\n")
                    val updatedText = lines.map { line ->
                        if (line.length > maxCharsPerLine) {
                            line.chunked(maxCharsPerLine).joinToString("\n")
                        } else {
                            line
                        }
                    }.joinToString("\n")
                    text = updatedText
                },
                label = { Text("Label") }
            )
            FilledButtonExample {
                val newTask = Task(text = mutableStateOf(text), check = mutableStateOf(false), isEditing = mutableStateOf(false))
                tasks.add(newTask)
                text = ""
            }

        }
        Box(
            modifier = modifier
                .fillMaxSize(),
        ){
            LazyColumn (
            ){

                items(tasks) { task ->
                    Row(
                        modifier = modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Checkbox(
                            checked = task.check.value,
                            onCheckedChange = { task.check.value = it }
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        if (task.isEditing.value) {
                            TextField(
                                value = task.text.value,
                                onValueChange = { task.text.value = it },
                                label = { Text("Edit Task") }
                            )
                        } else {
                            Text(
                                text = task.text.value,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { task.isEditing.value = !task.isEditing.value }) {
                            Icon(
                                imageVector = if (task.isEditing.value) Icons.Filled.Check else Icons.Filled.Edit,
                                contentDescription = "Информация о приложении")
                        }
                        IconButton(onClick = {tasks.remove(task)}) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Удалить задачу")
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = { saveTasksToFile(context = context, tasks) }) {
                    Text("Сохранить")
                }

                Button(onClick = {
                    val loadedTasks = loadTasksFromFile(context = context)
                    if (loadedTasks != null) {
                        tasks.clear()
                        tasks.addAll(loadedTasks)
                    }
                }) {
                    Text("Загрузить")
                }
            }
        }
        }

    }

@Composable
fun FilledButtonExample(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onClick() },
    ) {
        Icon(Icons.Filled.Add, "Floating action button.")
    }
}

fun saveTasksToFile(context: Context, tasks: List<Task>) {
    val gson = Gson()
    val serializableTasks = tasks.map { it.toSerializable() }
    val jsonString = gson.toJson(serializableTasks)
    val file = File(context.filesDir, "tasks.json")
    file.writeText(jsonString)
}

fun loadTasksFromFile(context: Context): List<Task>? {
    val file = File(context.filesDir, "tasks.json")
    if (!file.exists()) return null

    val jsonString = file.readText()
    val gson = Gson()
    val taskType = object : TypeToken<List<TaskSerializable>>() {}.type
    val serializableTasks: List<TaskSerializable> = gson.fromJson(jsonString, taskType)
    return serializableTasks.map { it.toTask() }
}

fun Task.toSerializable(): TaskSerializable {
    return TaskSerializable(
        text = this.text.value,
        check = this.check.value,
        isEditing = this.isEditing.value
    )
}

fun TaskSerializable.toTask(): Task {
    return Task(
        text = mutableStateOf(this.text),
        check = mutableStateOf(this.check),
        isEditing = mutableStateOf(this.isEditing)
    )
}

