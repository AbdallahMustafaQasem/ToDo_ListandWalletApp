# TODo App — Full Requirements Document

**Version:** 1.0  
**Date:** 2026-04-19  
**Author:** Project Manager  
**Audience:** Senior Android Developer  

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Functional Requirements](#2-functional-requirements)
3. [Screen & Navigation Specifications](#3-screen--navigation-specifications)
4. [Technical Requirements](#4-technical-requirements)
5. [Data Model](#5-data-model)
6. [Architecture Specification](#6-architecture-specification)
7. [Project Structure](#7-project-structure)
8. [Widget Specification](#8-widget-specification)
9. [Notification & Reminder Specification](#9-notification--reminder-specification)
10. [Dependency Catalog](#10-dependency-catalog)
11. [Non-Functional Requirements](#11-non-functional-requirements)
12. [Out of Scope](#12-out-of-scope)

---

## 1. Project Overview

A native Android Todo application with full CRUD, due-date support, local notifications/reminders, and a home screen widget. The app is built entirely in Kotlin using Jetpack Compose (Material Design 3), Clean Architecture, and the latest stable Jetpack libraries.

**Application ID:** `com.example.todoapp`  
**Min SDK:** 24 (Android 7.0)  
**Target SDK / Compile SDK:** 36  
**Language:** Kotlin only — no Java, no cross-platform  
**UI toolkit:** Jetpack Compose + Material Design 3  

---

## 2. Functional Requirements

### 2.1 Todo Management (CRUD)

| ID | Requirement |
|----|-------------|
| F-01 | User can create a new todo with: title (required), description (optional), due date/time (optional), priority (None / Low / Medium / High), and reminder toggle. |
| F-02 | User can view a scrollable list of all todos on the Home screen, ordered by due date ascending (no due date sorts to the bottom), then by creation date descending within the same due date. |
| F-03 | User can tap a todo to open a detail/edit screen pre-populated with current values. |
| F-04 | User can update any field of an existing todo and save changes. |
| F-05 | User can delete a todo via swipe-to-dismiss on the list, or via a Delete button on the detail screen. A Snackbar with "Undo" action must appear for 4 seconds post-deletion. |
| F-06 | User can mark a todo as complete/incomplete via a checkbox on the list row. Completed todos are visually distinguished (strikethrough title, reduced opacity). |
| F-07 | User can filter todos by: All, Active (not completed), Completed. Filter state persists in memory only (reset on app relaunch). |
| F-08 | User can search todos by title via a search bar on the Home screen. Search is performed locally, in-memory, on the current filtered list. |

### 2.2 Due Date Support

| ID | Requirement |
|----|-------------|
| F-09 | Due date field stores date AND time (stored as epoch milliseconds). |
| F-10 | When a todo is past its due date and is not completed, it must be visually flagged (red due-date chip). |
| F-11 | Date/time picker uses the Material Design 3 DatePickerDialog and TimePickerDialog from Compose Material3. |

### 2.3 Reminders / Notifications

| ID | Requirement |
|----|-------------|
| F-12 | When reminder is enabled and a due date is set, an exact alarm notification fires at the due date/time. |
| F-13 | Notification shows: todo title as the notification title, description (or "No description") as body text, and two action buttons — "Mark Done" and "Open App". |
| F-14 | Tapping "Mark Done" in the notification marks the todo complete without opening the app. |
| F-15 | Tapping the notification or "Open App" deep-links into the Todo Detail screen for that todo. |
| F-16 | If a todo's due date or reminder toggle is changed, any previously scheduled alarm is cancelled and rescheduled (or removed if reminder is turned off). |
| F-17 | Reminders must survive device reboot (BroadcastReceiver listens for BOOT_COMPLETED to reschedule all active reminders). |
| F-18 | On Android 13+ (API 33+) the app requests POST_NOTIFICATIONS permission at runtime on first launch. |
| F-19 | On Android 12+ (API 31+) the app requests SCHEDULE_EXACT_ALARM permission and gracefully degrades to inexact alarms if not granted. |

### 2.4 Home Screen Widget

| ID | Requirement |
|----|-------------|
| F-20 | A 4×2 home screen widget displays up to 5 upcoming, non-completed todos ordered by due date ascending (no due date goes last). |
| F-21 | Each row in the widget shows: checkbox icon (tapping marks complete), todo title, and due date label (or empty if none). |
| F-22 | Widget has an "+ Add" button that opens the Add Todo screen in the app. |
| F-23 | Widget updates automatically whenever the todo list changes (observe Room DB). |
| F-24 | Widget supports light and dark mode (respects system theme). |
| F-25 | Widget title bar shows the app name and a refresh button. |

---

## 3. Screen & Navigation Specifications

### 3.1 Navigation Graph

Use Jetpack Navigation Component with a single-activity architecture. All screens are Composable destinations.

```
NavGraph (startDestination = home)
├── home              — TodoListScreen
├── add               — AddEditTodoScreen (no argument)
├── detail/{todoId}   — AddEditTodoScreen (todoId: Long)
└── about             — AboutScreen (optional, via top-bar overflow menu)
```

Deep link into `detail/{todoId}` must be handled from notification PendingIntent.

### 3.2 TodoListScreen (Home)

- **Top App Bar:** App title "My Todos", search icon (toggles inline search bar), overflow menu (About).
- **Filter chips row:** All | Active | Completed — horizontally scrollable, single selection.
- **Content area:** `LazyColumn` of `TodoItem` composables.
- **FAB:** "+" navigates to `add`.
- **Empty state:** Illustrated empty state message when no todos match the current filter/search.

### 3.3 AddEditTodoScreen

- **Top App Bar:** Back button, title = "New Todo" or "Edit Todo", Save action button (text button, enabled only when title is non-empty).
- **Form fields (vertical scroll):**
  1. Title — single-line `OutlinedTextField`, required, max 100 chars.
  2. Description — multiline `OutlinedTextField`, optional, max 500 chars.
  3. Due Date — clickable `OutlinedTextField` (read-only input), shows DatePickerDialog on tap. Clear icon when value is set.
  4. Due Time — same pattern as Due Date, only enabled when a due date is selected.
  5. Priority — segmented button group: None / Low / Medium / High.
  6. Reminder — `Switch` row. Enabled only when a due date+time is set. Disables and resets to off if due date is cleared.
- **Delete button** — shown only in edit mode, at the bottom, destructive red style.

### 3.4 TodoItem Composable (List Row)

- Checkbox (leading).
- Title (strikethrough if completed).
- Optional due-date chip (red if overdue, neutral otherwise).
- Priority indicator dot (color-coded: grey / green / orange / red).
- Trailing: more-options icon or swipe-to-dismiss background.

---

## 4. Technical Requirements

### 4.1 Platform

| Property | Value |
|----------|-------|
| Language | Kotlin 2.x |
| Min SDK | 24 |
| Target SDK | 36 |
| Compile SDK | 36 |
| JVM Target | 11 |
| Build system | Gradle with Kotlin DSL (`.gradle.kts`) |

### 4.2 Architecture

Strict Clean Architecture with three layers:

1. **Data layer** — Room database, DAOs, repository implementations, WorkManager schedulers, alarm managers.
2. **Domain layer** — Plain Kotlin. Entities, repository interfaces, use cases. Zero Android framework imports.
3. **Presentation layer** — Jetpack Compose UI, ViewModels, UI state classes.

### 4.3 Dependency Injection

Hilt is the mandatory DI framework. Every ViewModel, UseCase, Repository, and WorkManager Worker must be injected via Hilt. No manual dependency graphs.

### 4.4 Concurrency

- All database operations use Kotlin Coroutines + `Dispatchers.IO`.
- Repository exposes `Flow<List<TodoEntity>>` for reactive UI updates.
- ViewModels use `viewModelScope` and `stateIn` to convert cold flows to hot `StateFlow`.
- No `LiveData` — use `StateFlow` and `SharedFlow` only.

### 4.5 UI

- Jetpack Compose only — no XML layouts except `widget_layout.xml` which is required by the Glance framework internals. Glance composables handle the widget UI.
- Material Design 3 throughout (`androidx.compose.material3`).
- Dynamic color enabled (Material You) — `dynamicColorScheme` on API 31+, fallback to a branded static scheme.
- Support both light and dark theme.

---

## 5. Data Model

### 5.1 TodoEntity (Room `@Entity`)

```kotlin
@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,                        // Required, max 100 chars

    @ColumnInfo(name = "description")
    val description: String = "",             // Optional, max 500 chars

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "due_date_millis")
    val dueDateMillis: Long? = null,          // Epoch ms; null = no due date

    @ColumnInfo(name = "priority")
    val priority: Priority = Priority.NONE,   // Stored as String via TypeConverter

    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean = false,     // Only meaningful when dueDateMillis != null

    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,                // Set once at creation time

    @ColumnInfo(name = "updated_at_millis")
    val updatedAtMillis: Long                 // Updated on every save
)
```

### 5.2 Priority Enum (Domain Layer)

```kotlin
enum class Priority { NONE, LOW, MEDIUM, HIGH }
```

Stored in Room via a `@TypeConverter` that converts to/from `String`.

### 5.3 Domain Model — `Todo`

A plain Kotlin data class mirroring `TodoEntity` used in domain and presentation layers. A mapper extension (`TodoEntity.toDomain()` / `Todo.toEntity()`) lives in the data layer.

### 5.4 Database

| Property | Value |
|----------|-------|
| Class | `TodoDatabase` |
| Version | 1 |
| Entities | `TodoEntity` |
| Export schema | `true` (committed to repo under `app/schemas/`) |

---

## 6. Architecture Specification

### 6.1 Layer Diagram

```
┌─────────────────────────────────────────┐
│          Presentation Layer             │
│  Composables  ─►  ViewModel  ─►  UI State│
│  (com.example.todoapp.ui)               │
└────────────────────┬────────────────────┘
                     │ uses UseCases
┌────────────────────▼────────────────────┐
│            Domain Layer                 │
│  UseCases  ─►  Repository (interface)   │
│  Todo (domain model), Priority          │
│  (com.example.todoapp.domain)           │
└────────────────────┬────────────────────┘
                     │ implements
┌────────────────────▼────────────────────┐
│             Data Layer                  │
│  RepositoryImpl ─► DAO ─► Room DB       │
│  AlarmScheduler, ReminderWorker         │
│  (com.example.todoapp.data)             │
└─────────────────────────────────────────┘
```

### 6.2 Use Cases (Domain Layer)

Each use case is a single-responsibility class with an `invoke` operator. All are injected via Hilt.

| Use Case Class | Responsibility |
|----------------|---------------|
| `GetAllTodosUseCase` | Returns `Flow<List<Todo>>` from repository |
| `GetTodoByIdUseCase` | Returns `Todo?` for a given id (suspend) |
| `AddTodoUseCase` | Validates and inserts a new `Todo`; schedules reminder if enabled |
| `UpdateTodoUseCase` | Updates an existing `Todo`; reschedules or cancels reminder |
| `DeleteTodoUseCase` | Deletes a `Todo`; cancels any scheduled reminder |
| `ToggleTodoCompletionUseCase` | Flips `isCompleted`; cancels reminder if now complete |

### 6.3 Repository

**Interface (domain layer):**

```kotlin
interface TodoRepository {
    fun getAllTodos(): Flow<List<Todo>>
    suspend fun getTodoById(id: Long): Todo?
    suspend fun insertTodo(todo: Todo): Long   // returns new row id
    suspend fun updateTodo(todo: Todo)
    suspend fun deleteTodo(todo: Todo)
    suspend fun toggleCompletion(id: Long, isCompleted: Boolean)
}
```

**Implementation (data layer):** `TodoRepositoryImpl` — depends on `TodoDao` and maps entities to domain models.

### 6.4 DAO

```kotlin
@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY due_date_millis ASC, created_at_millis DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): TodoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity): Long

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    @Query("UPDATE todos SET is_completed = :isCompleted, updated_at_millis = :now WHERE id = :id")
    suspend fun updateCompletion(id: Long, isCompleted: Boolean, now: Long)

    // Used by widget to fetch top 5 upcoming non-completed todos
    @Query("SELECT * FROM todos WHERE is_completed = 0 ORDER BY due_date_millis ASC LIMIT 5")
    fun getUpcomingTodos(): Flow<List<TodoEntity>>
}
```

### 6.5 ViewModels

#### `TodoListViewModel`

State: `TodoListUiState` (sealed class or data class with filter, search query, todo list, loading flag).  
Exposes: `StateFlow<TodoListUiState>`.  
Actions (functions called by UI): `onFilterChanged`, `onSearchQueryChanged`, `onToggleCompletion`, `onDeleteTodo`, `onUndoDelete`.

Undo delete logic: store the last deleted `Todo` in a private field; reinsert it if undo is triggered within 4 seconds (use a `Job` with a `delay`).

#### `AddEditTodoViewModel`

Receives optional `todoId: Long` via `SavedStateHandle`.  
State: `AddEditTodoUiState` (all form fields as individual properties + a `isSaving` flag + `savedEvent: Boolean`).  
Actions: `onTitleChanged`, `onDescriptionChanged`, `onDueDateChanged`, `onDueTimeChanged`, `onPriorityChanged`, `onReminderToggled`, `onSave`, `onDelete`.

---

## 7. Project Structure

```
com.example.todoapp
│
├── di/
│   ├── AppModule.kt             — Hilt module: provides DB, DAO, AlarmManager
│   ├── RepositoryModule.kt      — Binds TodoRepositoryImpl → TodoRepository
│   └── UseCaseModule.kt         — Provides all use cases (or use @Inject constructors)
│
├── data/
│   ├── local/
│   │   ├── TodoDatabase.kt
│   │   ├── TodoDao.kt
│   │   ├── TodoEntity.kt
│   │   └── converter/
│   │       └── PriorityConverter.kt
│   ├── mapper/
│   │   └── TodoMapper.kt        — TodoEntity ↔ Todo
│   ├── repository/
│   │   └── TodoRepositoryImpl.kt
│   ├── alarm/
│   │   ├── AlarmScheduler.kt    — Interface
│   │   └── AlarmSchedulerImpl.kt — Uses AlarmManager
│   └── worker/
│       └── ReminderWorker.kt    — WorkManager Worker (fallback / boot reschedule)
│
├── domain/
│   ├── model/
│   │   ├── Todo.kt
│   │   └── Priority.kt
│   ├── repository/
│   │   └── TodoRepository.kt    — Interface
│   └── usecase/
│       ├── GetAllTodosUseCase.kt
│       ├── GetTodoByIdUseCase.kt
│       ├── AddTodoUseCase.kt
│       ├── UpdateTodoUseCase.kt
│       ├── DeleteTodoUseCase.kt
│       └── ToggleTodoCompletionUseCase.kt
│
├── ui/
│   ├── MainActivity.kt
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Screen.kt            — Sealed class of route strings
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── todolist/
│   │   ├── TodoListScreen.kt
│   │   ├── TodoListViewModel.kt
│   │   ├── TodoListUiState.kt
│   │   └── components/
│   │       ├── TodoItem.kt
│   │       ├── FilterChipsRow.kt
│   │       └── EmptyStateView.kt
│   └── addedit/
│       ├── AddEditTodoScreen.kt
│       ├── AddEditTodoViewModel.kt
│       └── AddEditTodoUiState.kt
│
├── widget/
│   ├── TodoWidget.kt            — GlanceAppWidget subclass
│   ├── TodoWidgetReceiver.kt    — GlanceAppWidgetReceiver subclass
│   └── TodoWidgetStateDefinition.kt
│
└── receiver/
    ├── ReminderBroadcastReceiver.kt  — Fires notification on alarm
    ├── BootReceiver.kt               — BOOT_COMPLETED → reschedule alarms
    └── NotificationActionReceiver.kt — Handles "Mark Done" action from notification
```

---

## 8. Widget Specification

### 8.1 Framework

Use **Jetpack Glance** (`androidx.glance:glance-appwidget`). Do not use RemoteViews manually.

### 8.2 Widget Metadata (`res/xml/todo_widget_info.xml`)

```xml
<appwidget-provider
    minWidth="250dp"
    minHeight="110dp"
    targetCellWidth="4"
    targetCellHeight="2"
    updatePeriodMillis="0"
    resizeMode="horizontal|vertical"
    widgetCategory="home_screen"
    initialLayout="@layout/glance_default_loading_layout" />
```

`updatePeriodMillis="0"` — widget is updated programmatically via `TodoWidget().updateAll(context)` called from the repository layer whenever data changes, not on a timer.

### 8.3 Widget UI Layout (Glance Composables)

```
┌──────────────────────────────────────┐
│  [App Icon] My Todos       [Refresh] │  ← Column header row
├──────────────────────────────────────┤
│  [ ] Buy groceries    — Tomorrow     │
│  [ ] Call dentist     — Overdue      │
│  [✓] Read book        ─ (completed)  │  ← up to 5 rows
│  ...                                 │
├──────────────────────────────────────┤
│              [+ Add Todo]            │  ← Footer button
└──────────────────────────────────────┘
```

### 8.4 Widget State

- Use a custom `GlanceStateDefinition` backed by `DataStore<Preferences>`.
- Store a serialized JSON list of the top 5 upcoming todos as widget state.
- `TodoWidgetReceiver` must be registered in `AndroidManifest.xml` with `android.appwidget.action.APPWIDGET_UPDATE`.

### 8.5 Widget Interactions

| Element | Action |
|---------|--------|
| Row checkbox | `ActionCallback` → `ToggleTodoCompletionUseCase` → `updateAll` |
| "+" Add button | `actionStartActivity<MainActivity>` with intent extra `OPEN_ADD_SCREEN=true` |
| Refresh icon | `ActionCallback` → reloads state from DB → `updateAll` |
| Todo row tap | `actionStartActivity<MainActivity>` with `OPEN_DETAIL_TODO_ID=<id>` |

---

## 9. Notification & Reminder Specification

### 9.1 Alarm Strategy

- Use `AlarmManager.setExactAndAllowWhileIdle()` for scheduling.
- Alarm ID = `todo.id.toInt()` (guaranteed unique per todo).
- On API 31+ check `alarmManager.canScheduleExactAlarms()`. If false, use `setAndAllowWhileIdle()` (inexact) and show a one-time in-app Snackbar informing the user.
- `PendingIntent` flag: `PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE`.

### 9.2 Notification Channel

| Property | Value |
|----------|-------|
| Channel ID | `"todo_reminders"` |
| Name | `"Todo Reminders"` |
| Importance | `NotificationManager.IMPORTANCE_HIGH` |
| Sound | Default |
| Vibration | Enabled |

Channel must be created in `Application.onCreate()` (or Hilt `AppModule` via an initializer).

### 9.3 Notification Structure

```
Title:   <todo.title>
Body:    <todo.description> or "No description added."
Icon:    R.drawable.ic_notification  (monochrome, 24dp)
Color:   Primary brand color

Actions:
  [Mark Done]  — PendingIntent → NotificationActionReceiver (ACTION_MARK_DONE, extra: todoId)
  [Open App]   — PendingIntent → MainActivity (deep link to detail/{todoId})

Content intent: same as "Open App"
Auto-cancel: true
```

### 9.4 Boot Rescheduling

`BootReceiver` (registered for `BOOT_COMPLETED` and `LOCKED_BOOT_COMPLETED`):
1. Query all todos where `reminderEnabled = true AND isCompleted = false AND dueDateMillis > System.currentTimeMillis()`.
2. Call `AlarmSchedulerImpl.schedule(todo)` for each.

This is a quick synchronous operation; use `goAsync()` + a coroutine to avoid ANR.

### 9.5 WorkManager Role

WorkManager is used **only** for the BOOT_COMPLETED reschedule orchestration as a fallback if the BroadcastReceiver is killed before finishing, and for any future background sync features. Do not use WorkManager to fire the actual notification (AlarmManager handles exact timing).

---

## 10. Dependency Catalog

All versions in `gradle/libs.versions.toml`. Below are the required libraries and minimum versions.

```toml
[versions]
agp                     = "8.5.0"
kotlin                  = "2.0.0"
coreKtx                 = "1.13.1"
lifecycleRuntimeKtx     = "2.8.3"
activityCompose         = "1.9.0"
composeBom              = "2024.06.00"
navigationCompose       = "2.7.7"
room                    = "2.6.1"
hilt                    = "2.51.1"
hiltNavigationCompose   = "1.2.0"
workManager             = "2.9.0"
hiltWork                = "1.2.0"
glance                  = "1.1.0"
coroutines              = "1.8.1"
ksp                     = "2.0.0-1.0.22"
datastore               = "1.1.1"
kotlinxSerializationJson= "1.7.1"

[libraries]
# Core
androidx-core-ktx                   = { group = "androidx.core",          name = "core-ktx",                   version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx      = { group = "androidx.lifecycle",     name = "lifecycle-runtime-ktx",      version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose= { group = "androidx.lifecycle",     name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose           = { group = "androidx.activity",      name = "activity-compose",           version.ref = "activityCompose" }

# Compose
androidx-compose-bom                = { group = "androidx.compose",       name = "compose-bom",                version.ref = "composeBom" }
androidx-compose-ui                 = { group = "androidx.compose.ui",    name = "ui" }
androidx-compose-ui-graphics        = { group = "androidx.compose.ui",    name = "ui-graphics" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui",    name = "ui-tooling-preview" }
androidx-compose-material3          = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons     = { group = "androidx.compose.material", name = "material-icons-extended" }

# Navigation
androidx-navigation-compose         = { group = "androidx.navigation",    name = "navigation-compose",         version.ref = "navigationCompose" }

# Room
androidx-room-runtime               = { group = "androidx.room",          name = "room-runtime",               version.ref = "room" }
androidx-room-ktx                   = { group = "androidx.room",          name = "room-ktx",                   version.ref = "room" }
androidx-room-compiler              = { group = "androidx.room",          name = "room-compiler",              version.ref = "room" }

# Hilt
hilt-android                        = { group = "com.google.dagger",      name = "hilt-android",               version.ref = "hilt" }
hilt-android-compiler               = { group = "com.google.dagger",      name = "hilt-android-compiler",      version.ref = "hilt" }
hilt-navigation-compose             = { group = "androidx.hilt",          name = "hilt-navigation-compose",    version.ref = "hiltNavigationCompose" }
hilt-work                           = { group = "androidx.hilt",          name = "hilt-work",                  version.ref = "hiltWork" }
hilt-compiler                       = { group = "androidx.hilt",          name = "hilt-compiler",              version.ref = "hiltWork" }

# WorkManager
androidx-work-runtime-ktx           = { group = "androidx.work",          name = "work-runtime-ktx",           version.ref = "workManager" }

# Glance (Widget)
androidx-glance-appwidget           = { group = "androidx.glance",        name = "glance-appwidget",           version.ref = "glance" }
androidx-glance-material3           = { group = "androidx.glance",        name = "glance-material3",           version.ref = "glance" }

# Coroutines
kotlinx-coroutines-android          = { group = "org.jetbrains.kotlinx",  name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# DataStore
androidx-datastore-preferences      = { group = "androidx.datastore",     name = "datastore-preferences",      version.ref = "datastore" }

# Serialization (for widget state)
kotlinx-serialization-json          = { group = "org.jetbrains.kotlinx",  name = "kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }

[plugins]
android-application  = { id = "com.android.application",              version.ref = "agp" }
kotlin-android       = { id = "org.jetbrains.kotlin.android",         version.ref = "kotlin" }
kotlin-compose       = { id = "org.jetbrains.kotlin.plugin.compose",  version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
hilt                 = { id = "com.google.dagger.hilt.android",        version.ref = "hilt" }
ksp                  = { id = "com.google.devtools.ksp",               version.ref = "ksp" }
```

### 10.1 `app/build.gradle.kts` Required Plugins

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}
```

### 10.2 Annotation Processors

Use **KSP** (not KAPT) for all annotation processing: Room, Hilt.

---

## 11. Non-Functional Requirements

| ID | Requirement |
|----|-------------|
| NF-01 | App must not crash on cold start. Room migrations must be provided for any schema change (export schema = true). |
| NF-02 | All database and IO operations must run off the main thread. UI thread violations in debug builds should throw via `StrictMode`. |
| NF-03 | Compose UI recomposition must be minimized — use `remember`, `derivedStateOf`, and stable data classes. |
| NF-04 | The widget must not perform any direct database access on the main thread; all data loading goes through a coroutine scope in `GlanceAppWidget.provideGlance`. |
| NF-05 | ProGuard/R8 rules must be added for Room, Hilt, and serialization to ensure correct release builds. |
| NF-06 | The domain layer must have zero Android framework imports — pure Kotlin only. Unit tests for all use cases must be possible without Robolectric. |
| NF-07 | Min Kotlin version: 2.0.0. Compose compiler is driven by the Kotlin Compose compiler plugin (no separate `kotlinCompilerExtensionVersion` needed with BOM). |

---

## 12. Out of Scope

The following are explicitly excluded from v1.0:

- Cloud sync or backend integration.
- Multiple lists / categories / tags.
- Recurring todos.
- Shared/collaborative todos.
- Widgets for lock screen.
- Wear OS companion app.
- iOS or any cross-platform target.
- In-app purchases.

---

*End of Requirements Document*
