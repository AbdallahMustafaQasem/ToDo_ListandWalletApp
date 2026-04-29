# TaskVault — Todo & Wallet App

A native Android productivity application combining a full-featured **Todo manager** with a personal **Wallet / expense tracker** — built entirely in Kotlin with Jetpack Compose and Clean Architecture.

Cloud sync, Google Sign-In, biometric app lock, home screen widget, reminders, and full support for **12 languages** are all included out of the box.

---

## Table of Contents

1. [Features](#features)
2. [Screens](#screens)
3. [Architecture](#architecture)
4. [Project Structure](#project-structure)
5. [Tech Stack](#tech-stack)
6. [Data Models](#data-models)
7. [Navigation Graph](#navigation-graph)
8. [Firebase Setup](#firebase-setup)
9. [Language Support](#language-support)
10. [Notifications & Reminders](#notifications--reminders)
11. [Home Screen Widget](#home-screen-widget)
12. [Getting Started](#getting-started)
13. [Build & Run](#build--run)
14. [Permissions](#permissions)
15. [Non-Functional Notes](#non-functional-notes)

---

## Features

### ✅ Todo Management

| ID | Feature |
|----|---------|
| F-00 | **Google Sign-In** — one-tap authentication via Firebase Auth; all data tied to the user's account |
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
| F-16 | **Cloud Sync** — all todos, subtasks, lists, wallet data, and categories mirrored to Firestore in real time |

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
- **Floating Action Button** for adding transactions — always accessible.
- Manage **custom categories** (icon + name).
- Set a **monthly budget** and track spending vs. budget in real time.
- Transaction list with filter by type (All / Income / Expense).
- **Wallet Analytics** — pie chart of expenses by category and spending-over-time bar chart.
- **Currency symbol** — choose from a list of common currency symbols; persisted per user.
- **Budget Alerts** — daily `BudgetAlertWorker` sends a push notification when monthly spending crosses 75 %, 90 %, and 100 % of the set budget (each threshold fires only once per calendar month).

### 🔒 App Lock

- Optional biometric / device-credential lock screen on every app launch and resume.
- Enabled / disabled via the **Settings → Security** toggle.
- Uses `BiometricPrompt` with fallback to PIN / pattern / password.
- Closing the lock prompt (back / cancel) exits the app.

### 👤 Profile & Account

- View signed-in Google account name and email.
- **Sign out** with confirmation dialog.
- **Delete account** — permanently erases all Firestore data (todos, wallet, lists) and removes the Firebase Auth user; guarded by a confirmation dialog.

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
| **Login** | *(gate)* | Google Sign-In screen shown when not authenticated |
| **Sync** | *(gate)* | Cloud sync progress / error screen shown after sign-in |
| **Todo List** | `home` | Main screen with search, filters, FAB, and todo list |
| **Add Todo** | `add` | Form to create a new todo |
| **Edit Todo** | `detail/{todoId}` | Pre-populated form to edit or delete an existing todo |
| **Statistics** | `statistics` | Charts and productivity stats |
| **Trash** | `trash` | Soft-deleted todos; restore or permanently delete |
| **Wallet** | `wallet` | Transaction list, balance summary, budget tracker with FAB |
| **Wallet Categories** | `wallet/categories` | Manage income/expense categories |
| **Calendar** | `calendar` | Month grid with todo-day indicators and daily todo list |
| **Lists & Projects** | `lists` | Create, edit, and delete named todo lists; assign colour and emoji icon |
| **Settings** | `settings` | Theme, language, currency, app lock, about |
| **Profile** | `profile` | Account info, sign-out, delete account |
| **About** | `about` | App name, version, and description |

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
com.abdallah.taskvault
│
├── TaskVaultApplication.kt      ─ Hilt app, notification channel init
├── MainActivity.kt              ─ Single-activity host; handles language, theme, app lock
│
├── di/
│   ├── AppModule.kt             ─ DB, DAO, AlarmManager, NotificationManager
│   ├── FirebaseModule.kt        ─ FirebaseAuth, FirebaseFirestore
│   ├── RepositoryModule.kt      ─ Binds Impl → Interface
│   └── PreferencesModule.kt     ─ DataStore / UserPreferences
│
├── data/
│   ├── local/
│   │   ├── TodoDatabase.kt
│   │   ├── TodoDao.kt / SubtaskDao.kt / TodoListDao.kt
│   │   ├── WalletDao.kt
│   │   ├── TodoEntity.kt / SubtaskEntity.kt / TodoListEntity.kt
│   │   ├── WalletTransactionEntity.kt
│   │   ├── WalletCategoryEntity.kt
│   │   ├── WalletBudgetEntity.kt
│   │   └── converter/  (PriorityConverter, TransactionTypeConverter)
│   ├── mapper/
│   │   ├── TodoMapper.kt
│   │   └── WalletMapper.kt
│   ├── repository/
│   │   ├── TodoRepositoryImpl.kt
│   │   ├── WalletRepositoryImpl.kt
│   │   └── FirebaseAuthRepositoryImpl.kt
│   ├── sync/
│   │   └── FirebaseSyncRepository.kt  ─ Mirrors all CRUD to Firestore under users/{uid}/
│   ├── alarm/
│   │   ├── AlarmScheduler.kt          ─ Interface
│   │   └── AlarmSchedulerImpl.kt      ─ AlarmManager-backed impl
│   ├── worker/
│   │   ├── ReminderWorker.kt          ─ Fallback / boot reschedule
│   │   ├── TrashPurgeWorker.kt        ─ Auto-purge trash items > 30 days
│   │   └── BudgetAlertWorker.kt       ─ Daily budget threshold notifications
│   └── preferences/
│       └── UserPreferencesRepository.kt
│
├── domain/
│   ├── model/  (Todo, Priority, WalletTransaction, WalletCategory, WalletBudget, TransactionType)
│   ├── repository/  (TodoRepository, WalletRepository, AuthRepository, TodoListRepository)
│   └── usecase/  (GetAll, GetById, Add, Update, Delete, Toggle, Restore, PermanentlyDelete)
│
├── ui/
│   ├── AppViewModel.kt          ─ Auth state, sync orchestration, account deletion
│   ├── navigation/  (NavGraph.kt, Screen.kt)
│   ├── theme/  (Color.kt, Theme.kt, Type.kt)
│   ├── auth/
│   │   ├── LoginScreen.kt / AuthViewModel.kt
│   │   ├── SyncScreen.kt
│   │   └── ProfileScreen.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   ├── todolist/
│   │   ├── TodoListScreen.kt / TodoListViewModel.kt / TodoListUiState.kt
│   │   └── components/  (TodoItem.kt, FilterChipsRow.kt, EmptyStateView.kt)
│   ├── addedit/  (AddEditTodoScreen.kt, AddEditTodoViewModel.kt)
│   ├── statistics/  (StatisticsScreen.kt, StatisticsViewModel.kt)
│   ├── trash/  (TrashScreen.kt)
│   ├── calendar/  (CalendarScreen.kt, CalendarViewModel.kt)
│   ├── lists/  (TodoListsScreen.kt, TodoListsViewModel.kt)
│   ├── about/  (AboutScreen.kt)
│   ├── applock/  (AppLockScreen.kt)
│   └── wallet/
│       ├── WalletScreen.kt / WalletViewModel.kt
│       └── WalletCategoriesScreen.kt
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
| Auth | Firebase Authentication (Google Sign-In) | Latest |
| Cloud DB | Firebase Firestore | Latest |
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
MainActivity (gates)
├── LoginScreen             ─ shown when currentUser == null
├── SyncScreen              ─ shown during / after first sign-in sync
└── NavGraph (startDestination = home)
    ├── home                    ─ TodoListScreen
    ├── add                     ─ AddEditTodoScreen (new)
    ├── detail/{todoId}         ─ AddEditTodoScreen (edit) + deep link
    ├── statistics              ─ StatisticsScreen
    ├── trash                   ─ TrashScreen
    ├── wallet                  ─ WalletScreen
    ├── wallet/categories       ─ WalletCategoriesScreen
    ├── calendar                ─ CalendarScreen
    ├── lists                   ─ TodoListsScreen
    ├── settings                ─ SettingsScreen
    ├── profile                 ─ ProfileScreen
    └── about                   ─ AboutScreen
```

Deep links into `detail/{todoId}` are handled from notification `PendingIntent` using the URI pattern `todoapp://detail/{todoId}`.

Screen transitions use slide + fade animations (300 ms).

---

## Firebase Setup

> The app requires a Firebase project to build and run cloud features.

1. Create a project at [console.firebase.google.com](https://console.firebase.google.com).
2. Add an **Android app** with package name `com.abdallah.taskvault`.
3. Enable **Google Sign-In** under *Authentication → Sign-in method*.
4. Enable **Cloud Firestore** (start in test mode during development).
5. Add your debug **SHA-1** fingerprint under *Project Settings → Your apps*:
   ```bash
   ./gradlew signingReport
   ```
6. Download `google-services.json` and place it in the `app/` directory.

### Firestore Data Structure

```
users/{uid}/
  ├── todos/           ─ TodoEntity documents
  ├── subtasks/        ─ SubtaskEntity documents
  ├── todoLists/       ─ TodoListEntity documents
  ├── transactions/    ─ WalletTransactionEntity documents
  ├── categories/      ─ WalletCategoryEntity documents
  └── budget/          ─ WalletBudgetEntity document
```

---

## Language Support

The app ships with **12 fully localised languages**. Every user-facing string is translated — including UI labels, empty states, dialogs, notifications, sync messages, and error strings.

| Code | Language |
|------|----------|
| `default` | English |
| `ar` | العربية (Arabic) |
| `bn` | বাংলা (Bengali) |
| `de` | Deutsch (German) |
| `es` | Español (Spanish) |
| `fr` | Français (French) |
| `hi` | हिन्दी (Hindi) |
| `ja` | 日本語 (Japanese) |
| `pt` | Português (Portuguese) |
| `ru` | Русский (Russian) |
| `ur` | اردو (Urdu) |
| `zh` | 中文 (Chinese) |

Language is selectable at runtime from **Settings → Language** without restarting.

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
- A Firebase project with `google-services.json` placed in `app/` (see [Firebase Setup](#firebase-setup))

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

## License

```
Copyright 2026 Abdallah Mustafa Qasem

Licensed under the MIT License.
See LICENSE file for details.
```
