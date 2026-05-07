package com.abdallah.taskvault.domain.usecase

import android.content.Context
import com.abdallah.taskvault.domain.model.Priority
import com.abdallah.taskvault.domain.model.RecurrenceRule
import com.abdallah.taskvault.domain.model.Todo
import com.abdallah.taskvault.domain.repository.TodoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class BackupRestoreUseCase @Inject constructor(
    private val todoRepository: TodoRepository,
    private val addTodoUseCase: AddTodoUseCase,
    @ApplicationContext private val context: Context
) {

    suspend fun createBackup(): File {
        val todos = todoRepository.getAllTodos().first()
        val array = JSONArray()
        todos.forEach { todo ->
            array.put(JSONObject().apply {
                put("id", todo.id)
                put("title", todo.title)
                put("description", todo.description)
                put("isCompleted", todo.isCompleted)
                put("priority", todo.priority.name)
                put("dueDateMillis", todo.dueDateMillis ?: JSONObject.NULL)
                put("createdAtMillis", todo.createdAtMillis)
                put("updatedAtMillis", todo.updatedAtMillis)
                put("recurrenceRule", todo.recurrenceRule.name)
                put("listId", todo.listId ?: JSONObject.NULL)
            })
        }
        val root = JSONObject().apply {
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())
            put("todos", array)
        }
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.getExternalFilesDir(null), "taskvault_backup_$dateStr.json")
        file.writeText(root.toString(2))
        return file
    }

    suspend fun restoreBackup(jsonText: String): Int {
        val root = JSONObject(jsonText)
        val array = root.getJSONArray("todos")
        var count = 0
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val now = System.currentTimeMillis()
            val todo = Todo(
                title = obj.getString("title"),
                description = obj.optString("description", ""),
                isCompleted = obj.optBoolean("isCompleted", false),
                priority = runCatching { Priority.valueOf(obj.getString("priority")) }.getOrDefault(Priority.NONE),
                dueDateMillis = if (obj.isNull("dueDateMillis")) null else obj.getLong("dueDateMillis"),
                createdAtMillis = obj.optLong("createdAtMillis", now),
                updatedAtMillis = obj.optLong("updatedAtMillis", now),
                recurrenceRule = runCatching { RecurrenceRule.valueOf(obj.getString("recurrenceRule")) }.getOrDefault(RecurrenceRule.NONE),
                listId = if (obj.isNull("listId")) null else obj.getLong("listId")
            )
            addTodoUseCase(todo)
            count++
        }
        return count
    }
}
