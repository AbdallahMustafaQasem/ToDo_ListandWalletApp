package com.example.todoapp.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.todoapp.MainActivity
import com.example.todoapp.R
import com.example.todoapp.data.local.TodoDao
import com.example.todoapp.ui.theme.DarkColorScheme
import com.example.todoapp.ui.theme.LightColorScheme
import com.example.todoapp.ui.theme.Purple10
import com.example.todoapp.ui.theme.Purple90
import com.example.todoapp.ui.theme.Teal10
import com.example.todoapp.ui.theme.Teal90
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TodoWidgetEntryPoint {
    fun todoDao(): TodoDao
}

private const val TAG = "TodoWidget"

private val widgetColors = ColorProviders(
    light = LightColorScheme,
    dark  = DarkColorScheme
)

private val OverdueRed    = Color(0xFFB3261E)
private val OverdueBg     = Color(0xFFF9DEDC)
private val OverdueOnBg   = Color(0xFF410E0B)

class TodoWidget : GlanceAppWidget() {

    override val stateDefinition = TodoWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d(TAG, "provideGlance started, id=$id")

        val dao = try {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                TodoWidgetEntryPoint::class.java
            ).todoDao()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire DAO via Hilt EntryPoint", e)
            null
        }

        try {
            val todos = dao?.getUpcomingTodos()?.first()?.map { entity ->
                WidgetTodoItem(
                    id            = entity.id,
                    title         = entity.title,
                    dueDateMillis = entity.dueDateMillis,
                    isCompleted   = entity.isCompleted
                )
            } ?: emptyList()

            val all = dao?.getAllTodos()?.first().orEmpty()
            val now = System.currentTimeMillis()
            val completed = all.count { it.isCompleted }
            val pending   = all.count { !it.isCompleted }
            val overdue   = all.count { !it.isCompleted && (it.dueDateMillis ?: Long.MAX_VALUE) < now }

            updateAppWidgetState(context, TodoWidgetStateDefinition, id) {
                WidgetState(
                    todos          = todos,
                    pendingCount   = pending,
                    completedCount = completed,
                    overdueCount   = overdue
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load todos", e)
        }

        provideContent {
            GlanceTheme(colors = widgetColors) {
                TodoWidgetContent(state = currentState())
            }
        }
    }
}

@androidx.glance.GlanceComposable
@androidx.compose.runtime.Composable
private fun TodoWidgetContent(state: WidgetState) {
    val context = LocalContext.current

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(20.dp)
            .padding(12.dp)
    ) {
        HeaderCard(pending = state.pendingCount, overdue = state.overdueCount)

        Spacer(GlanceModifier.height(10.dp))

        StatsRow(
            pending   = state.pendingCount,
            completed = state.completedCount,
            overdue   = state.overdueCount
        )

        Spacer(GlanceModifier.height(10.dp))

        TodoListSection(todos = state.todos, context = context)

        Spacer(GlanceModifier.defaultWeight())

        AddTodoButton(context = context)
    }
}

@androidx.glance.GlanceComposable
@androidx.compose.runtime.Composable
private fun HeaderCard(pending: Int, overdue: Int) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.primaryContainer)
            .cornerRadius(16.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = context.getString(R.string.title_my_todos),
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimaryContainer,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = buildHeaderSubtitle(context, pending, overdue),
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimaryContainer,
                    fontSize = 11.sp
                )
            )
        }
        Box(
            modifier = GlanceModifier
                .size(34.dp)
                .background(ColorProvider(Color.White.copy(alpha = 0.25f)))
                .cornerRadius(17.dp)
                .clickable(actionRunCallback<RefreshWidgetCallback>()),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(android.R.drawable.ic_menu_rotate),
                contentDescription = context.getString(R.string.cd_refresh),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                modifier = GlanceModifier.size(18.dp)
            )
        }
    }
}

private fun buildHeaderSubtitle(context: Context, pending: Int, overdue: Int): String {
    val parts = mutableListOf<String>()
    parts += if (pending == 1) context.getString(R.string.widget_subtitle_pending_one)
             else context.getString(R.string.widget_subtitle_pending_other, pending)
    if (overdue > 0) {
        parts += if (overdue == 1) context.getString(R.string.widget_subtitle_overdue_one)
                 else context.getString(R.string.widget_subtitle_overdue_other, overdue)
    }
    return parts.joinToString(" · ")
}

@androidx.glance.GlanceComposable
@androidx.compose.runtime.Composable
private fun StatsRow(pending: Int, completed: Int, overdue: Int) {
    val context = LocalContext.current
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        StatChip(
            value       = pending,
            label       = context.getString(R.string.widget_pending),
            container   = ColorProvider(Purple90),
            onContainer = ColorProvider(Purple10),
            modifier    = GlanceModifier.defaultWeight()
        )
        Spacer(GlanceModifier.width(8.dp))
        StatChip(
            value       = completed,
            label       = context.getString(R.string.widget_done),
            container   = ColorProvider(Teal90),
            onContainer = ColorProvider(Teal10),
            modifier    = GlanceModifier.defaultWeight()
        )
        Spacer(GlanceModifier.width(8.dp))
        StatChip(
            value       = overdue,
            label       = context.getString(R.string.widget_overdue),
            container   = if (overdue > 0) ColorProvider(OverdueBg) else GlanceTheme.colors.surfaceVariant,
            onContainer = if (overdue > 0) ColorProvider(OverdueOnBg) else GlanceTheme.colors.onSurfaceVariant,
            modifier    = GlanceModifier.defaultWeight()
        )
    }
}

@androidx.glance.GlanceComposable
@androidx.compose.runtime.Composable
private fun StatChip(
    value: Int,
    label: String,
    container: ColorProvider,
    onContainer: ColorProvider,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier
            .background(container)
            .cornerRadius(12.dp)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = TextStyle(
                color = onContainer,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = label,
            style = TextStyle(
                color = onContainer,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@androidx.glance.GlanceComposable
@androidx.compose.runtime.Composable
private fun TodoListSection(todos: List<WidgetTodoItem>, context: Context) {
    if (todos.isEmpty()) {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.surfaceVariant)
                .cornerRadius(12.dp)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = context.getString(R.string.widget_all_clear),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }
    } else {
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            todos.take(3).forEachIndexed { index, todo ->
                TodoWidgetRow(todo = todo, context = context)
                if (index < todos.take(3).lastIndex) {
                    Spacer(GlanceModifier.height(4.dp))
                }
            }
        }
    }
}

@androidx.glance.GlanceComposable
@androidx.compose.runtime.Composable
private fun TodoWidgetRow(todo: WidgetTodoItem, context: Context) {
    val detailIntent = Intent(context, MainActivity::class.java).apply {
        putExtra(MainActivity.EXTRA_OPEN_DETAIL_TODO_ID, todo.id)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }

    val todoIdKey = ActionParameters.Key<Long>("todoId")
    val isOverdue = (todo.dueDateMillis ?: Long.MAX_VALUE) < System.currentTimeMillis() && !todo.isCompleted

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.surfaceVariant)
            .cornerRadius(12.dp)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .clickable(actionStartActivity(detailIntent)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(
                if (todo.isCompleted) android.R.drawable.checkbox_on_background
                else android.R.drawable.checkbox_off_background
            ),
            contentDescription = if (todo.isCompleted) context.getString(R.string.cd_completed) else context.getString(R.string.cd_mark_complete),
            colorFilter = ColorFilter.tint(
                if (todo.isCompleted) GlanceTheme.colors.primary
                else GlanceTheme.colors.onSurfaceVariant
            ),
            modifier = GlanceModifier
                .size(22.dp)
                .clickable(
                    actionRunCallback<ToggleCompletionCallback>(
                        actionParametersOf(todoIdKey to todo.id)
                    )
                )
        )

        Spacer(GlanceModifier.width(10.dp))

        Text(
            text = todo.title,
            style = TextStyle(
                color = if (todo.isCompleted) GlanceTheme.colors.onSurfaceVariant
                else GlanceTheme.colors.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )

        todo.dueDateMillis?.let { millis ->
            Text(
                text = formatShortDate(millis),
                style = TextStyle(
                    color = if (isOverdue) ColorProvider(OverdueRed)
                    else GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                )
            )
        }
    }
}

@androidx.glance.GlanceComposable
@androidx.compose.runtime.Composable
private fun AddTodoButton(context: Context) {
    val addIntent = Intent(context, MainActivity::class.java).apply {
        putExtra(MainActivity.EXTRA_OPEN_ADD_SCREEN, true)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.primary)
            .cornerRadius(14.dp)
            .padding(vertical = 10.dp)
            .clickable(actionStartActivity(addIntent)),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(android.R.drawable.ic_input_add),
                contentDescription = null,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimary),
                modifier = GlanceModifier.size(16.dp)
            )
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = context.getString(R.string.fab_add_todo),
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

private fun formatShortDate(millis: Long): String =
    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))

class RefreshWidgetCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        try {
            TodoWidget().updateAll(context)
        } catch (e: Exception) {
            Log.e(TAG, "RefreshWidgetCallback failed", e)
        }
    }
}

class ToggleCompletionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val todoId = parameters[ActionParameters.Key<Long>("todoId")] ?: return
        try {
            val dao = EntryPointAccessors.fromApplication(
                context.applicationContext,
                TodoWidgetEntryPoint::class.java
            ).todoDao()
            val entity = dao.getTodoById(todoId) ?: return
            dao.updateCompletion(todoId, !entity.isCompleted, System.currentTimeMillis())
            TodoWidget().updateAll(context)
        } catch (e: Exception) {
            Log.e(TAG, "ToggleCompletionCallback failed", e)
        }
    }
}
