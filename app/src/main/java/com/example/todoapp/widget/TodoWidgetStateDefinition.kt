package com.example.todoapp.widget

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.glance.state.GlanceStateDefinition
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class WidgetTodoItem(
    val id: Long,
    val title: String,
    val dueDateMillis: Long? = null,
    val isCompleted: Boolean = false
)

@Serializable
data class WidgetState(
    val todos: List<WidgetTodoItem> = emptyList(),
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val overdueCount: Int = 0
)

object WidgetStateSerializer : Serializer<WidgetState> {
    override val defaultValue: WidgetState = WidgetState()

    override suspend fun readFrom(input: InputStream): WidgetState {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) return defaultValue
        return try {
            Json.decodeFromString(WidgetState.serializer(), bytes.decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("Cannot read widget state", e)
        }
    }

    override suspend fun writeTo(t: WidgetState, output: OutputStream) {
        output.write(Json.encodeToString(WidgetState.serializer(), t).encodeToByteArray())
    }
}

val Context.widgetDataStore: DataStore<WidgetState> by dataStore(
    fileName = "widget_state.json",
    serializer = WidgetStateSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { WidgetState() }
)

object TodoWidgetStateDefinition : GlanceStateDefinition<WidgetState> {
    private const val DATA_STORE_FILENAME = "widget_state.json"

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<WidgetState> {
        return context.widgetDataStore
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return File(context.filesDir, "datastore/$DATA_STORE_FILENAME")
    }
}
