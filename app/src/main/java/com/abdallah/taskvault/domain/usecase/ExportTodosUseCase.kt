package com.abdallah.taskvault.domain.usecase

import com.abdallah.taskvault.domain.model.Todo
import com.abdallah.taskvault.domain.repository.TodoRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ExportTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend fun asCsv(): String {
        val todos = repository.getAllTodos().first().filter { !it.isDeleted }
        val header = "id,title,description,priority,due_date,is_completed,recurrence,list_id\n"
        val rows = todos.joinToString("\n") { t ->
            "${t.id},\"${t.title.escapeCsv()}\",\"${t.description.escapeCsv()}\"," +
            "${t.priority.name},${t.dueDateMillis ?: ""},${t.isCompleted}," +
            "${t.recurrenceRule.name},${t.listId ?: ""}"
        }
        return header + rows
    }

    suspend fun asJson(): String {
        val todos = repository.getAllTodos().first().filter { !it.isDeleted }
        val items = todos.joinToString(",\n  ", prefix = "[\n  ", postfix = "\n]") { t ->
            """{
    "id": ${t.id},
    "title": "${t.title.escapeJson()}",
    "description": "${t.description.escapeJson()}",
    "priority": "${t.priority.name}",
    "due_date": ${t.dueDateMillis ?: "null"},
    "is_completed": ${t.isCompleted},
    "recurrence": "${t.recurrenceRule.name}",
    "list_id": ${t.listId ?: "null"}
  }"""
        }
        return items
    }

    private fun String.escapeCsv() = replace("\"", "\"\"")
    private fun String.escapeJson() = replace("\\", "\\\\").replace("\"", "\\\"")
}
