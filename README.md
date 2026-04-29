# 📝 Todo & Wallet App

A native Android productivity application combining a full-featured **Todo manager** with a personal **Wallet / expense tracker** — built entirely in Kotlin with Jetpack Compose and Clean Architecture.

---

## Table of Contents

1. [Features](#features)
2. [Screenshots / Screens](#screens)
3. [Architecture](#architecture)
4. [Project Structure](#project-structure)
5. [Tech Stack](#tech-stack)
6. [Data Models](#data-models)
7. [Navigation Graph](#navigation-graph)
8. [Notifications & Reminders](#notifications--reminders)
9. [Home Screen Widget](#home-screen-widget)
10. [Getting Started](#getting-started)
11. [Build & Run](#build--run)
12. [Permissions](#permissions)
13. [Non-Functional Notes](#non-functional-notes)
14. [Out of Scope (v1.0)](#out-of-scope-v10)

---

## Features

### ✅ Todo Management

| ID | Feature |
|----|---------|
| F-01 | Create todos with **title** (required), **description** (optional), **due date/time**, **priority** (None / Low / Medium / High), and **reminder** toggle |
| F-02 | Scrollable list ordered by due date ascending; no-due-date items sort to the bottom |
| F-03 | Tap a todo to open a pre-populated detail/edit screen |
| F-04 | Update any field and save changes |
| F-05 | Delete via swipe-to-dismiss or the Delete button; **Snackbar with Undo** appears for 4 seconds |
| F-06 | Mark complete/incomplete via checkbox; completed items show strikethrough + reduced opacity |
| F-07 | Filter by **All / Active / Completed** (state reset on relaunch) |
| F-08 | Search todos by title (in-memory, on the current filtered list) |
| F-09 | Due date stores date **and** time as epoch milliseconds |
| F-10 | Overdue, incomplete todos are flagged with a **red due-date chip** |
| F-11 | Date/time pickers use Material Design 3 `DatePickerDialog` / `TimePickerDialog` |
| F-12 | **Sort Options** — sort by Creation Date / Due Date / Priority / Alphabetical (persisted in DataStore) |
| F-13 | **Subtasks** — add, check-off, and delete sub-items for any todo; progress counter shown (e.g. `2/3`) |
| F-14 | **Recurring Todos** — set a repeat rule: None / Daily / Weekly / Monthly |
| F-15 | **Todo Lists / Projects** — organise todos into named, colour-coded lists; assign a list per todo |

### 🗑️ Trash / Recycle Bin

- Deleted todos are soft-deleted and moved to the Trash screen.
- Items in Trash can be **permanently deleted** or **restored** to the active list.
- A background `TrashPurgeWorker` (WorkManager) auto-purges trash items older than 30 days.

### 📊 Statistics Screen

- **Completion rate** — animated dual-ring chart.
- **Weekly activity** — bar chart of todos completed over the last 7 days.
- **Priority breakdown** — count by None / Low / Medium / High.
- **Current streak** — consecutive days with at least one completion.
- **Most productive day** — day-of-week with the highest completion count.
- **Overdue count** — number of active past-due todos.

### 💰 Wallet / Expense Tracker

- Log **income** and **expense** transactions with amount, category, date, and optional notes.
- Manage **custom categories** (icon + name).
- Set a **monthly budget** and track spending vs. budget in real time.
- Transaction list with filter by type (All / Income / Expense).
- **Wallet Analytics** — pie chart of expenses by category and spending-over-time bar chart.
- **Budget Alerts** — daily `BudgetAlertWorker` sends a push notification when monthly spending crosses 75 %, 90 %, and 100 % of the set budget (each threshold fires only once per calendar month).

### 🔒 App Lock

- Optional biometric / device-credential lock screen on every app launch and resume.
- Enabled / disabled via **App Lock** toggle in the main menu.
- Uses `BiometricPrompt` with fallback to PIN / pattern / password.
- Closing the lock prompt (back / cancel) exits the app.

### � Calendar View

- Month grid showing all months with coloured dot indicators on days that have todos.
- Navigate forward / backward between months.
- Tap any day to see that day's todos beneath the grid, sorted by priority.
- Tap a todo to open its edit screen.

### �� Notifications & Reminders

- Exact alarm fires at the todo's due date/time when reminder is enabled.
- Notification shows two action buttons: **Mark Done** and **Open App**.
- Tapping **Mark Done** marks the todo complete without opening the app.
- Alarms survive **device reboot** via `BootReceiver`.
- Graceful degradation to inexact alarms on API 31+ if exact-alarm permission is not granted.

### 📱 Home Screen Widget

- 4×2 Glance widget showing up to 5 upcoming non-completed todos.
- Checkbox on each row marks the todo complete from the home screen.
- **+ Add** button opens the Add Todo screen.
- Refresh button reloads data from Room.
- Supports light **and** dark mode.

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| **Todo List** | `home` | Main screen with search, filters, FAB, and todo list |
| **Add Todo** | `add` | Form to create a new todo |
| **Edit Todo** | `detail/{todoId}` | Pre-populated form to edit or delete an existing todo |
| **Statistics** | `statistics` | Charts and productivity stats |
| **Trash** | `trash` | Soft-deleted todos; restore or permanently delete |
| **Wallet** | `wallet` | Transaction list, balance summary, budget tracker |
| **Wallet Categories** | `wallet/categories` | Manage income/expense categories |
| **About** | `about` | App name, version, and description |
| **Calendar** | `calendar` | Month grid with todo-day indicators and daily todo list |
| **Lists & Projects** | `lists` | Create, edit, and delete named todo lists; assign colour and emoji icon |

---

## Architecture

The app follows strict **Clean Architecture** with three isolated layers:

```
┌─────────────────────────────────────────┐
│          Presentation Layer             │
│  Composables  ──►  ViewModel  ──►  UI State │
│  (com.example.todoapp.ui)               │
└────────────────────┬────────────────────┘
                     │ uses UseCases
┌────────────────────▼────────────────────┐
│            Domain Layer                 │
│  UseCases  ──►  Repository (interface)  │
│  Todo / Wallet models, Priority enum    │
│  (com.example.todoapp.domain)           │
└────────────────────┬────────────────────┘
                     │ implements
┌────────────────────▼────────────────────┐
│             Data Layer                  │
│  RepositoryImpl ──► DAO ──► Room DB     │
│  AlarmScheduler, Workers, Preferences   │
│  (com.example.todoapp.data)             │
└─────────────────────────────────────────┘
```

- **Domain layer** has **zero Android framework imports** — pure Kotlin only.
- All database I/O runs on `Dispatchers.IO`.
- Repository exposes `Flow<List<T>>` for reactive UI updates.
- ViewModels use `viewModelScope` and `stateIn` to convert flows to `StateFlow`.
- No `LiveData` — `StateFlow` and `SharedFlow` only.

---

## Project Structure

```
com.example.todoapp
│
├── TodoApplication.kt          ─ Hilt app, notification channel init
├── MainActivity.kt             ─ Single-activity host
│
├── di/
│   ├── AppModule.kt            ─ DB, DAO, AlarmManager, NotificationManager
│   ├── RepositoryModule.kt     ─ Binds Impl → Interface
│   └── PreferencesModule.kt    ─ DataStore / UserPreferences
│
├── data/
│   ├── local/
│   │   ├── TodoDatabase.kt
│   │   ├── TodoDao.kt
│   │   ├── WalletDao.kt
│   │   ├── TodoEntity.kt
│   │   ├── WalletTransactionEntity.kt
│   │   ├── WalletCategoryEntity.kt
│   │   ├── WalletBudgetEntity.kt
│   │   └── converter/
│   │       ├── PriorityConverter.kt
│   │       └── TransactionTypeConverter.kt
│   ├── mapper/
│   │   ├── TodoMapper.kt
│   │   └── WalletMapper.kt
│   ├── repository/
│   │   ├── TodoRepositoryImpl.kt
│   │   └── WalletRepositoryImpl.kt
│   ├── alarm/
│   │   ├── AlarmScheduler.kt       ─ Interface
│   │   └── AlarmSchedulerImpl.kt   ─ AlarmManager-backed impl
│   ├── worker/
│   │   ├── ReminderWorker.kt       ─ Fallback / boot reschedule
│   │   └── TrashPurgeWorker.kt     ─ Auto-purge old trash items
│   └── preferences/
│       └── UserPreferencesRepository.kt
│
├── domain/
│   ├── model/
│   │   ├── Todo.kt
│   │   ├── Priority.kt
│   │   ├── WalletTransaction.kt
│   │   ├── WalletCategory.kt
│   │   ├── WalletBudget.kt
│   │   └── TransactionType.kt
│   ├── repository/
│   │   ├── TodoRepository.kt
│   │   └── WalletRepository.kt
│   ├── usecase/
│   │   ├── GetAllTodosUseCase.kt
│   │   ├── GetTodoByIdUseCase.kt
│   │   ├── AddTodoUseCase.kt
│   │   ├── UpdateTodoUseCase.kt
│   │   ├── DeleteTodoUseCase.kt
│   │   ├── ToggleTodoCompletionUseCase.kt
│   │   ├── GetDeletedTodosUseCase.kt
│   │   ├── RestoreTodoUseCase.kt
│   │   └── PermanentlyDeleteTodoUseCase.kt
│   └── alarm/
│       └── AlarmScheduler.kt   ─ Domain-facing interface
│
├── ui/
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Screen.kt
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
│   ├── addedit/
│   │   ├── AddEditTodoScreen.kt
│   │   ├── AddEditTodoViewModel.kt
│   │   └── AddEditTodoUiState.kt
│   ├── statistics/
│   │   ├── StatisticsScreen.kt
│   │   └── StatisticsViewModel.kt
│   ├── trash/
│   │   └── TrashScreen.kt
│   └── wallet/
│       ├── WalletScreen.kt
│       ├── WalletCategoriesScreen.kt
│       └── WalletViewModel.kt
│
├── widget/
│   ├── TodoWidget.kt               ─ GlanceAppWidget subclass
│   ├── TodoWidgetReceiver.kt       ─ GlanceAppWidgetReceiver
│   └── TodoWidgetStateDefinition.kt
│
└── receiver/
    ├── ReminderBroadcastReceiver.kt    ─ Fires notification on alarm
    ├── BootReceiver.kt                 ─ BOOT_COMPLETED → reschedule alarms
    └── NotificationActionReceiver.kt   ─ Handles "Mark Done" from notification
```

---

## Tech Stack

| Category | Library / Tool | Version |
|----------|---------------|---------|
| Language | Kotlin | 2.0.0 |
| UI | Jetpack Compose + Material Design 3 | BOM 2024.06.00 |
| DI | Hilt (Dagger) | 2.51.1 |
| Database | Room | 2.6.1 |
| Navigation | Jetpack Navigation Compose | 2.7.7 |
| Async | Kotlin Coroutines + Flow | 1.8.1 |
| Widget | Jetpack Glance | 1.1.0 |
| Background work | WorkManager | 2.9.0 |
| Preferences | DataStore Preferences | 1.1.1 |
| Serialization | kotlinx.serialization JSON | 1.7.1 |
| Annotation processing | KSP | 2.0.0-1.0.22 |
| Build system | Gradle Kotlin DSL | AGP 8.5.0 |
| Min SDK | Android 8.0 (API 26) | — |
| Target / Compile SDK | Android 36 | — |
| JVM target | Java 11 | — |

---

## Data Models

### `TodoEntity` (Room)

```kotlin
@Entity(tableName = "todos")
data class TodoEntity(
    val id: Long = 0,               // Auto-generated primary key
    val title: String,              // Required, max 100 chars
    val description: String = "",   // Optional, max 500 chars
    val isCompleted: Boolean = false,
    val isDeleted: Boolean = false,  // Soft-delete flag (Trash)
    val dueDateMillis: Long? = null, // Epoch ms; null = no due date
    val priority: Priority = Priority.NONE,
    val reminderEnabled: Boolean = false,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
```

### `Priority` Enum (Domain)

```kotlin
enum class Priority { NONE, LOW, MEDIUM, HIGH }
```

Stored in Room via `PriorityConverter` (String ↔ enum).

### `WalletTransaction` (Domain)

```kotlin
data class WalletTransaction(
    val id: Long = 0,
    val type: TransactionType,  // INCOME | EXPENSE
    val amount: Double,
    val categoryId: Long?,
    val categoryName: String,
    val categoryIcon: String,
    val dateMillis: Long,
    val notes: String = ""
)
```

### `WalletBudget` (Domain)

```kotlin
data class WalletBudget(
    val id: Long = 1,
    val monthlyBudget: Double
)
```

---

## Navigation Graph

```
NavGraph (startDestination = home)
├── home                    ─ TodoListScreen
├── add                     ─ AddEditTodoScreen (new)
├── detail/{todoId}         ─ AddEditTodoScreen (edit) + deep link
├── statistics              ─ StatisticsScreen
├── about                   ─ AboutScreen
├── trash                   ─ TrashScreen
├── wallet                  ─ WalletScreen
└── wallet/categories       ─ WalletCategoriesScreen
```

Deep links into `detail/{todoId}` are handled from notification `PendingIntent` using the URI pattern `todoapp://detail/{todoId}`.

Screen transitions use slide + fade animations (300 ms).

---

## Notifications & Reminders

### Alarm Strategy

- Uses `AlarmManager.setExactAndAllowWhileIdle()` for scheduling.
- Alarm ID = `todo.id.toInt()` (unique per todo).
- On API 31+: checks `canScheduleExactAlarms()`; falls back to `setAndAllowWhileIdle()` (inexact) with an in-app Snackbar warning.
- `PendingIntent` flags: `FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE`.
- Rescheduled or cancelled whenever due date / reminder toggle changes.

### Notification Channel

| Property | Value |
|----------|-------|
| Channel ID | `"todo_reminders"` |
| Name | `"Todo Reminders"` |
| Importance | `IMPORTANCE_HIGH` |
| Sound | Default |
| Vibration | Enabled |

### Notification Structure

```
Title:   <todo title>
Body:    <todo description> or "No description added."
Actions: [Mark Done]  →  marks complete (no app open)
         [Open App]   →  deep-links to detail/{todoId}
```

### Boot Rescheduling

`BootReceiver` listens for `BOOT_COMPLETED` and `LOCKED_BOOT_COMPLETED`, queries all active reminders (`reminderEnabled = true`, `isCompleted = false`, `dueDateMillis > now`), and reschedules each alarm using `AlarmSchedulerImpl`.

---

## Home Screen Widget

Built with **Jetpack Glance** (no manual `RemoteViews`).

| Property | Value |
|----------|-------|
| Size | 4×2 cells (min 250×110 dp), resizable |
| Data source | Room DB via DataStore-backed `GlanceStateDefinition` |
| Update trigger | Programmatic (`TodoWidget.updateAll(context)`) |
| Theme | Respects system light/dark mode |

### Widget Layout

```
┌──────────────────────────────────────┐
│  [App Icon] My Todos       [Refresh] │
├──────────────────────────────────────┤
│  [ ] Buy groceries    — Tomorrow     │
│  [ ] Call dentist     — Overdue      │
│  [ ] Read book        — No date      │  ← up to 5 rows
├──────────────────────────────────────┤
│              [+ Add Todo]            │
└──────────────────────────────────────┘
```

### Widget Interactions

| Element | Action |
|---------|--------|
| Row checkbox | `ActionCallback` → `ToggleTodoCompletionUseCase` → widget refresh |
| Todo row tap | Opens app to `detail/{todoId}` |
| + Add button | Opens app to Add Todo screen |
| Refresh icon | Reloads state from DB and re-renders |

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11+
- Android SDK with API 36 installed

### Clone

```bash
git clone https://github.com/<your-username>/TODoApp.git
cd TODoApp
```

### Open in Android Studio

`File → Open` → select the `TODoApp` folder.

Android Studio will sync Gradle automatically.

---

## Build & Run

**Debug build:**

```bash
./gradlew assembleDebug
```

**Release build** (requires signing config):

```bash
./gradlew assembleRelease
```

**Install on connected device:**

```bash
./gradlew installDebug
```

**Run unit tests:**

```bash
./gradlew test
```

**Run instrumented tests:**

```bash
./gradlew connectedAndroidTest
```

> **Note:** The Room schema is exported to `app/schemas/`. Commit this directory to track database migrations.

---

## Permissions

| Permission | Purpose |
|------------|---------|
| `RECEIVE_BOOT_COMPLETED` | Reschedule alarms after device reboot |
| `SCHEDULE_EXACT_ALARM` | Exact alarm for reminders (API 31+) |
| `USE_EXACT_ALARM` | Exact alarm without user approval (API 33+) |
| `POST_NOTIFICATIONS` | Show reminder notifications (API 33+, runtime) |
| `VIBRATE` | Notification vibration |

---

## Non-Functional Notes

| ID | Note |
|----|------|
| NF-01 | App does not crash on cold start. Room migration support is in place (`exportSchema = true`). |
| NF-02 | All database and IO operations run off the main thread. `StrictMode` is enabled in debug builds. |
| NF-03 | Compose recompositions are minimized via `remember`, `derivedStateOf`, and stable data classes. |
| NF-04 | The widget never performs direct DB access on the main thread; all loading goes through a coroutine scope in `GlanceAppWidget.provideGlance`. |
| NF-05 | ProGuard/R8 rules are configured for Room, Hilt, and kotlinx.serialization. |
| NF-06 | Domain layer has zero Android imports — all use cases are unit-testable without Robolectric. |
| NF-07 | Kotlin 2.0+ with the Compose compiler plugin; no separate `kotlinCompilerExtensionVersion` needed. |

---

## Out of Scope (v1.0)

The following are **not** included in version 1.0:

- Cloud sync or backend integration
- Multiple lists / categories / tags for todos
- Recurring todos
- Shared / collaborative todos
- Lock-screen widgets
- Wear OS companion app
- iOS or any cross-platform target
- In-app purchases

---

## License

```
Copyright 2026 Abdallah Mustafa Qasem

Licensed under the MIT License.
See LICENSE file for details.
```
