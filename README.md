# TaskVault — All-in-One Productivity App

A native Android productivity application built entirely in **Kotlin** with **Jetpack Compose** and **Clean Architecture**.

TaskVault brings together todo management, a personal wallet, notes, memoirs, a password vault, habit tracking, bill reminders, contact management, and a **collaborative task assignment system** — all backed by Firebase and available in **12 languages**.

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
9. [Push Notifications](#push-notifications)
10. [Language Support](#language-support)
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
| F-01 | Create todos with **title**, **description**, **due date/time**, **priority** (None / Low / Medium / High), and **reminder** toggle |
| F-02 | Scrollable list ordered by due date; no-date items sort to bottom |
| F-03 | Tap to open a pre-populated edit screen |
| F-04 | Update any field and save changes |
| F-05 | Delete via swipe-to-dismiss or button; **Snackbar Undo** for 4 seconds |
| F-06 | Mark complete/incomplete via checkbox; strikethrough + reduced opacity |
| F-07 | Filter by **All / Active / Completed** |
| F-08 | In-memory search by title |
| F-09 | Overdue incomplete todos show a **red due-date chip** |
| F-10 | **Sort** by Creation Date / Due Date / Priority / Alphabetical (persisted) |
| F-11 | **Subtasks** — add, check-off, delete sub-items; progress counter shown (e.g. `2/3`) |
| F-12 | **Recurring Todos** — None / Daily / Weekly / Monthly |
| F-13 | **Todo Lists / Projects** — colour-coded named lists; assign a list per todo |
| F-14 | **Cloud Sync** — all data mirrored to Firestore in real time |

### 🗑️ Trash / Recycle Bin

- Deleted todos are soft-deleted and moved to the Trash screen.
- Items can be **permanently deleted** or **restored**.
- `TrashPurgeWorker` (WorkManager) auto-purges items older than 30 days.

### 📊 Statistics Screen

- Completion rate — animated dual-ring chart.
- Weekly activity — bar chart of completions over the last 7 days.
- Priority breakdown — count by None / Low / Medium / High.
- Current streak — consecutive days with at least one completion.
- Most productive day and overdue count.

### 💰 Wallet / Expense Tracker

- Log **income** and **expense** transactions with amount, category, date, and notes.
- Manage **custom categories** (icon + name).
- Set a **monthly budget** and track spending vs. budget in real time.
- Filter transactions by All / Income / Expense.
- **Wallet Analytics** — pie chart by category and spending-over-time bar chart.
- **Currency symbol** — choose from common symbols; persisted per user.
- **Budget Alerts** — `BudgetAlertWorker` sends a notification when monthly spending crosses 75 %, 90 %, and 100 % of budget (each threshold fires once per calendar month).

### 📝 Notes

- Create rich text notes with a title and body.
- Full-screen detail editor with auto-save.
- List with search and date ordering.

### 📖 Memoirs / Journal

- Personal journal entries with title, body, and timestamp.
- Chronological list with detail editor.

### 🔐 Password Vault

- Store service name, username, and encrypted password.
- Copy credentials to clipboard.
- Biometric-protected access.

### 🏃 Habit Tracker

- Define habits with a name, icon, and target frequency.
- Check off completions per day.
- Streak counting and progress indicator.

### 🧾 Bill Reminders

- Track upcoming bills with name, amount, and due date.
- Overdue and due-soon indicators.
- `BillReminderReceiver` fires a notification when a bill is due.

### 👥 Contacts

- Manage a contact list (name, user ID, avatar colour) used as quick-pick assignees in the Task Assignment flow.

### 🤝 Task Assignment System

A full collaborative workflow for delegating tasks to other app users.

#### Assign Task (`assign`)
- **Title**, **description**, **priority**, and **due date** fields.
- Pick assignees from your **Contacts** list (chip selection with avatar initials).
- Or enter any user's **UID** manually for users not in your contacts.
- Submits to Firestore `task_assignments` collection and writes a notification document to each assignee.

#### Assigned to Me (`assigned_to_me`)
- Shows all tasks assigned to the **current user** from Firestore.
- Filter chips: All / Pending / Accepted / In Progress / Completed / Declined.
- Each card shows title, description, priority, due date, assigner name, and a status badge.
- Action buttons advance the workflow:
  - **Pending** → Accept or Decline
  - **Accepted** → Start
  - **In Progress** → Complete
- Every status change writes a notification document back to the **assigner**.

#### Assignment Stats (`assign_stats`)
- Fetches all tasks the current user has **assigned to others**.
- **3 summary cards** — Total assigned, Acceptance %, Completion %.
- **Status breakdown** — progress bar per status showing count and proportion.
- **Recent 5 assignments** — task name, assignees, due date, priority colour, status badge.

### 🔔 Push Notifications (FCM)

- **New assignment** — when a task is assigned to you, a system notification appears: *"X assigned you 'Task Name'"*.
- **Status update** — when an assignee updates the status of your task, you receive: *"X updated 'Task Name' to IN_PROGRESS"*.
- Powered by `MyFirebaseMessagingService` (FCM) + `AssignmentNotificationManager` (Firestore real-time listener).
- Device FCM token saved to `users/{uid}/fcmToken` and refreshed automatically.
- Works while the app is **open or backgrounded**. For delivery when the app is fully killed, deploy the included Firebase Cloud Functions trigger (see [Push Notifications](#push-notifications)).

### 🔒 App Lock

- Optional biometric / device-credential lock on every launch and resume.
- Enabled via **Settings → Security**.
- `BiometricPrompt` with PIN / pattern / password fallback.
- Closing the lock prompt exits the app.

### 👤 Profile & Account

- View Google account name and email.
- **Sign out** with confirmation dialog.
- **Delete account** — erases all Firestore data and removes Firebase Auth user.

### 📅 Calendar View

- Month grid with coloured dot indicators on days that have todos.
- Tap a day to see that day's todos; tap a todo to edit it.

### 📊 Analytics

- Firebase Analytics screen-view tracking on every navigation event.

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| **Login** | *(gate)* | Google Sign-In; shown when not authenticated |
| **Sync** | *(gate)* | Cloud sync progress / error after sign-in |
| **Dashboard** | `dashboard` | Feature-grid home with summary chips |
| **Todo List** | `home` | Search, filters, FAB, and todo list |
| **Add Todo** | `add` | Form to create a new todo |
| **Edit Todo** | `detail/{todoId}` | Pre-populated form; deep-link target |
| **Statistics** | `statistics` | Charts and productivity stats |
| **Trash** | `trash` | Restore or permanently delete soft-deleted todos |
| **Wallet** | `wallet` | Transaction list, balance, budget tracker |
| **Wallet Categories** | `wallet/categories` | Manage income/expense categories |
| **Calendar** | `calendar` | Month grid with daily todo list |
| **Lists & Projects** | `lists` | Create and manage named todo lists |
| **Notes** | `notes` | Note list |
| **Note Detail** | `notes/detail?noteId={id}` | Full-screen note editor |
| **Memoirs** | `memoirs` | Journal entry list |
| **Memoir Detail** | `memoirs/detail?memoirId={id}` | Journal editor |
| **Passwords** | `passwords` | Password vault list |
| **Password Detail** | `passwords/detail?passwordId={id}` | Add / edit password entry |
| **Habits** | `habits` | Habit list with streak and progress |
| **Habit Detail** | `habits/detail?habitId={id}` | Create / edit habit |
| **Bills** | `bills` | Bill list with due-date indicators |
| **Bill Detail** | `bills/detail?billId={id}` | Add / edit bill |
| **Contacts** | `contacts` | Manage contacts used for task assignment |
| **Assign Task** | `assign` | Assign a task to other users |
| **Assigned to Me** | `assigned_to_me` | View and action tasks assigned to you |
| **Assignment Stats** | `assign_stats` | Stats for tasks you have assigned |
| **Search** | `search` | Global search across todos, notes, memoirs, passwords |
| **Settings** | `settings` | Theme, language, currency, app lock |
| **Profile** | `profile` | Account info, sign-out, delete account |
| **About** | `about` | App version and description |

---

## Architecture

The app follows strict **Clean Architecture** with three isolated layers:

```
┌────────────────────────────────────────────┐
│           Presentation Layer               │
│  Composables  ──►  ViewModel  ──►  UiState │
│  (com.abdallah.taskvault.ui)               │
└─────────────────────┬──────────────────────┘
                      │ uses
┌─────────────────────▼──────────────────────┐
│              Domain Layer                  │
│  Repository interfaces + Domain models     │
│  (com.abdallah.taskvault.domain)           │
└─────────────────────┬──────────────────────┘
                      │ implements
┌─────────────────────▼──────────────────────┐
│               Data Layer                   │
│  RepositoryImpl ──► DAO ──► Room DB        │
│  Firebase Firestore, FCM, WorkManager      │
│  (com.abdallah.taskvault.data)             │
└────────────────────────────────────────────┘
```

- **Domain layer** has zero Android framework imports — pure Kotlin only.
- All database I/O runs on `Dispatchers.IO`.
- Repositories expose `Flow<List<T>>` for reactive UI updates.
- ViewModels use `viewModelScope` and `stateIn` to convert flows to `StateFlow`.
- No `LiveData` — `StateFlow` and `SharedFlow` only.

---

## Project Structure

```
com.abdallah.taskvault
│
├── MainActivity.kt              ─ Single-activity host; language, theme, app lock, FCM listener
├── TodoApplication.kt           ─ Hilt app entry point
│
├── di/
│   ├── AppModule.kt             ─ DB, DAOs, AlarmManager, NotificationManager (2 channels)
│   ├── FirebaseModule.kt        ─ FirebaseAuth, FirebaseFirestore
│   ├── RepositoryModule.kt      ─ Binds Impl → Interface for all repositories
│   └── PreferencesModule.kt     ─ DataStore / UserPreferences
│
├── data/
│   ├── local/
│   │   ├── TodoDatabase.kt
│   │   ├── TodoDao / SubtaskDao / TodoListDao / WalletDao
│   │   ├── NoteDao / MemoirDao / PasswordDao
│   │   ├── HabitDao / BillDao / ContactDao
│   │   └── Entities + converters
│   ├── repository/
│   │   ├── TodoRepositoryImpl.kt
│   │   ├── WalletRepositoryImpl.kt
│   │   ├── NoteRepositoryImpl.kt
│   │   ├── MemoirRepositoryImpl.kt
│   │   ├── PasswordRepositoryImpl.kt
│   │   ├── HabitRepositoryImpl.kt
│   │   ├── BillRepositoryImpl.kt
│   │   ├── ContactRepositoryImpl.kt
│   │   ├── TaskAssignmentRepositoryImpl.kt  ─ Firestore-backed; writes notifications on assign + status change
│   │   └── FirebaseAuthRepositoryImpl.kt
│   ├── sync/
│   │   └── FirebaseSyncRepository.kt        ─ Mirrors Room data to Firestore on sign-in
│   ├── alarm/
│   │   ├── AlarmScheduler.kt
│   │   └── AlarmSchedulerImpl.kt
│   ├── worker/
│   │   ├── TrashPurgeWorker.kt
│   │   └── BudgetAlertWorker.kt
│   └── preferences/
│       └── UserPreferencesRepository.kt
│
├── domain/
│   ├── model/
│   │   ├── Todo, Priority, SubTask, TodoList
│   │   ├── WalletTransaction, WalletCategory, WalletBudget, TransactionType
│   │   ├── Note, Memoir, Password, Habit, Bill, Contact
│   │   └── TaskAssignment, AssignmentStatus
│   ├── repository/   (interface per domain)
│   └── usecase/      (GetAll, Add, Update, Delete, Toggle, Restore…)
│
├── service/
│   ├── MyFirebaseMessagingService.kt        ─ FCM token refresh + displays push notifications
│   └── AssignmentNotificationManager.kt     ─ Firestore real-time listener → local notifications
│
├── ui/
│   ├── AppViewModel.kt          ─ Auth state, sync orchestration, account deletion
│   ├── navigation/  (NavGraph.kt, Screen.kt)
│   ├── theme/       (Color, Theme, Type)
│   ├── dashboard/   (DashboardScreen, DashboardViewModel)
│   ├── auth/        (LoginScreen, SyncScreen, ProfileScreen)
│   ├── settings/    (SettingsScreen, SettingsViewModel)
│   ├── todolist/    (TodoListScreen, TodoListViewModel, components/)
│   ├── addedit/     (AddEditTodoScreen, AddEditTodoViewModel)
│   ├── statistics/  (StatisticsScreen, StatisticsViewModel)
│   ├── trash/       (TrashScreen)
│   ├── calendar/    (CalendarScreen, CalendarViewModel)
│   ├── lists/       (TodoListsScreen, TodoListsViewModel)
│   ├── wallet/      (WalletScreen, WalletViewModel, WalletCategoriesScreen)
│   ├── notes/       (NoteListScreen, NoteDetailScreen, NoteViewModel)
│   ├── memoirs/     (MemoirListScreen, MemoirDetailScreen, MemoirViewModel)
│   ├── passwords/   (PasswordListScreen, PasswordDetailScreen, PasswordViewModel)
│   ├── habits/      (HabitListScreen, HabitDetailScreen, HabitViewModel)
│   ├── bills/       (BillListScreen, BillDetailScreen, BillViewModel)
│   ├── contacts/    (ContactListScreen, ContactViewModel)
│   ├── assign/
│   │   ├── TaskAssignmentScreen.kt + ViewModel   ─ Assign task to others
│   │   ├── AssignedToMeScreen.kt + ViewModel     ─ View & action tasks assigned to you
│   │   └── AssignmentStatsScreen.kt + ViewModel  ─ Stats for tasks you assigned
│   ├── search/      (SearchScreen, SearchViewModel)
│   ├── about/       (AboutScreen)
│   └── applock/     (AppLockScreen)
│
├── analytics/
│   └── AnalyticsHelper + AnalyticsEntryPoint    ─ Firebase Analytics screen tracking
│
├── widget/
│   ├── TodoWidget.kt / TodoWidgetReceiver.kt
│   └── TodoWidgetStateDefinition.kt
│
└── receiver/
    ├── ReminderBroadcastReceiver.kt
    ├── BillReminderReceiver.kt
    ├── BootReceiver.kt
    └── NotificationActionReceiver.kt
```

---

## Tech Stack

| Category | Library / Tool |
|----------|---------------|
| Language | Kotlin 2.0+ |
| UI | Jetpack Compose + Material Design 3 |
| DI | Hilt (Dagger) |
| Database | Room |
| Navigation | Jetpack Navigation Compose |
| Async | Kotlin Coroutines + Flow |
| Auth | Firebase Authentication (Google Sign-In) |
| Cloud DB | Firebase Firestore |
| Push | Firebase Cloud Messaging (FCM) |
| Widget | Jetpack Glance |
| Background | WorkManager |
| Preferences | DataStore Preferences |
| Serialization | kotlinx.serialization JSON |
| Annotation processing | KSP |
| Build system | Gradle Kotlin DSL |
| Min SDK | Android 8.0 (API 26) |
| Target / Compile SDK | Android 36 |
| JVM target | Java 11 |

---

## Data Models

### `TaskAssignment` (Domain / Firestore)

```kotlin
data class TaskAssignment(
    val id: String = "",
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.NONE,
    val dueDateMillis: Long? = null,
    val assignerId: String,
    val assignerName: String,
    val assigneeIds: List<String>,
    val assigneeNames: List<String>,
    val status: AssignmentStatus = AssignmentStatus.PENDING,
    val createdAtMillis: Long = System.currentTimeMillis()
)

enum class AssignmentStatus {
    PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, DECLINED
}
```

### `TodoEntity` (Room)

```kotlin
@Entity(tableName = "todos")
data class TodoEntity(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val isDeleted: Boolean = false,
    val dueDateMillis: Long? = null,
    val priority: Priority = Priority.NONE,
    val reminderEnabled: Boolean = false,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
```

---

## Navigation Graph

```
MainActivity (gates)
├── LoginScreen             ─ shown when currentUser == null
├── SyncScreen              ─ shown during first sign-in sync
└── NavGraph (startDestination = dashboard)
    ├── dashboard               ─ DashboardScreen
    ├── home                    ─ TodoListScreen
    ├── add                     ─ AddEditTodoScreen (new)
    ├── detail/{todoId}         ─ AddEditTodoScreen (edit) + deep link
    ├── statistics              ─ StatisticsScreen
    ├── trash                   ─ TrashScreen
    ├── wallet                  ─ WalletScreen
    ├── wallet/categories       ─ WalletCategoriesScreen
    ├── calendar                ─ CalendarScreen
    ├── lists                   ─ TodoListsScreen
    ├── notes                   ─ NoteListScreen
    ├── notes/detail?noteId={}  ─ NoteDetailScreen
    ├── memoirs                 ─ MemoirListScreen
    ├── memoirs/detail?…        ─ MemoirDetailScreen
    ├── passwords               ─ PasswordListScreen
    ├── passwords/detail?…      ─ PasswordDetailScreen
    ├── habits                  ─ HabitListScreen
    ├── habits/detail?…         ─ HabitDetailScreen
    ├── bills                   ─ BillListScreen
    ├── bills/detail?…          ─ BillDetailScreen
    ├── contacts                ─ ContactListScreen
    ├── assign                  ─ TaskAssignmentScreen
    ├── assigned_to_me          ─ AssignedToMeScreen
    ├── assign_stats            ─ AssignmentStatsScreen
    ├── search                  ─ SearchScreen
    ├── settings                ─ SettingsScreen
    ├── profile                 ─ ProfileScreen
    └── about                   ─ AboutScreen
```

Screen transitions use slide + fade animations (300 ms). Deep links into `detail/{todoId}` use the URI pattern `todoapp://detail/{todoId}`.

---

## Firebase Setup

> The app requires a Firebase project to build and run.

1. Create a project at [console.firebase.google.com](https://console.firebase.google.com).
2. Add an **Android app** with package name `com.abdallah.taskvault`.
3. Enable **Google Sign-In** under *Authentication → Sign-in method*.
4. Enable **Cloud Firestore** (start in test mode during development).
5. Enable **Firebase Cloud Messaging** under *Project Settings → Cloud Messaging*.
6. Add your debug **SHA-1** fingerprint under *Project Settings → Your apps*:
   ```bash
   ./gradlew signingReport
   ```
7. Download `google-services.json` and place it in the `app/` directory.

### Firestore Data Structure

```
task_assignments/{assignmentId}/
  ├── title, description, priority, dueDateMillis
  ├── assignerId, assignerName
  ├── assigneeIds[], assigneeNames[]
  ├── status, createdAtMillis

users/{uid}/
  ├── fcmToken                   ─ device push token (saved on login / token refresh)
  ├── todos/                     ─ TodoEntity documents
  ├── subtasks/                  ─ SubtaskEntity documents
  ├── todoLists/                 ─ TodoListEntity documents
  ├── transactions/              ─ WalletTransactionEntity documents
  ├── categories/                ─ WalletCategoryEntity documents
  ├── budget/                    ─ WalletBudgetEntity document
  └── notifications/{id}/
        ├── type                 ─ "task_assigned" | "status_updated"
        ├── title                ─ task title
        ├── fromName, fromId     ─ sender
        ├── assignmentId
        ├── newStatus            ─ (status_updated only)
        ├── read                 ─ false → shown, then marked true
        └── timestamp
```

---

## Push Notifications

The app uses two mechanisms together:

### 1 — Firestore Real-Time Listener (foreground + backgrounded app)

`AssignmentNotificationManager` starts listening on `users/{uid}/notifications` the moment the user logs in. Any new unread document triggers a local system notification immediately.

| Trigger | Who receives | Notification text |
|---------|-------------|-------------------|
| Task assigned via `assignTask()` | Each **assignee** | *"X assigned you 'Task Name'"* |
| Status updated via `updateStatus()` | The **assigner** | *"X updated 'Task Name' to IN_PROGRESS"* |

### 2 — Firebase Cloud Messaging (killed app)

`MyFirebaseMessagingService` handles incoming FCM data payloads and shows a system notification.

To deliver push when the app is fully killed, deploy a **Firebase Cloud Function** that triggers on new documents in `users/{uid}/notifications` and sends an FCM message to the stored `fcmToken`. Example trigger (Node.js):

```js
exports.onNewNotification = functions.firestore
  .document("users/{uid}/notifications/{notifId}")
  .onCreate(async (snap, context) => {
    const uid = context.params.uid;
    const data = snap.data();
    const userDoc = await admin.firestore().doc(`users/${uid}`).get();
    const token = userDoc.data()?.fcmToken;
    if (!token) return;
    await admin.messaging().send({
      token,
      data: {
        type: data.type,
        title: data.title,
        body: `${data.fromName} — ${data.title}`,
      },
    });
  });
```

### Notification Channels

| Channel ID | Name | Purpose |
|------------|------|---------|
| `todo_reminders` | Todo Reminders | Due-date alarm notifications |
| `task_assignments` | Task Assignments | Assignment and status-update notifications |

---

## Language Support

Every user-facing string is fully localised across **12 languages**, including UI labels, empty states, dialogs, notifications, error strings, and all assignment/notification copy.

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

## Home Screen Widget

Built with **Jetpack Glance** (no manual `RemoteViews`).

| Property | Value |
|----------|-------|
| Size | 4×2 cells (min 250×110 dp), resizable |
| Data source | Room DB via `GlanceStateDefinition` |
| Theme | Respects system light / dark mode |

```
┌──────────────────────────────────────┐
│  [App Icon] My Todos       [Refresh] │
├──────────────────────────────────────┤
│  [ ] Buy groceries    — Tomorrow     │
│  [ ] Call dentist     — Overdue      │
│  [ ] Read book        — No date      │
├──────────────────────────────────────┤
│              [+ Add Todo]            │
└──────────────────────────────────────┘
```

| Element | Action |
|---------|--------|
| Row checkbox | Marks todo complete; widget refreshes |
| Todo row tap | Opens `detail/{todoId}` |
| + Add button | Opens Add Todo screen |
| Refresh icon | Reloads from DB |

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11+
- Android SDK API 36
- A Firebase project with `google-services.json` in `app/` (see [Firebase Setup](#firebase-setup))

### Clone

```bash
git clone https://github.com/<your-username>/TODoApp.git
cd TODoApp
```

### Open in Android Studio

`File → Open` → select the `TODoApp` folder. Gradle syncs automatically.

---

## Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

> Room schema files are exported to `app/schemas/`. Commit this directory to track migrations.

---

## Permissions

| Permission | Purpose |
|------------|---------|
| `RECEIVE_BOOT_COMPLETED` | Reschedule alarms after device reboot |
| `SCHEDULE_EXACT_ALARM` | Exact alarm for reminders (API 31+) |
| `USE_EXACT_ALARM` | Exact alarm without user approval (API 33+) |
| `POST_NOTIFICATIONS` | Show notifications (API 33+, runtime prompt) |
| `VIBRATE` | Notification vibration |

---

## Non-Functional Notes

| ID | Note |
|----|------|
| NF-01 | Room migration support in place (`exportSchema = true`); no crash on cold start. |
| NF-02 | All DB and IO operations run off the main thread. |
| NF-03 | Compose recompositions minimised via `remember`, `derivedStateOf`, and stable data classes. |
| NF-04 | Widget never performs DB access on the main thread; all loading goes through a coroutine scope in `GlanceAppWidget.provideGlance`. |
| NF-05 | ProGuard/R8 rules configured for Room, Hilt, and kotlinx.serialization. |
| NF-06 | Domain layer has zero Android imports — all use cases are unit-testable without Robolectric. |
| NF-07 | FCM token is refreshed automatically via `MyFirebaseMessagingService.onNewToken()` and saved to Firestore. |
| NF-08 | Firestore notification documents are marked `read = true` immediately after display to prevent duplicate notifications. |
| NF-09 | `AssignmentNotificationManager` skips the first Firestore snapshot to avoid re-showing already-seen notifications on app start. |

---

## License

```
Copyright 2026 Abdallah Mustafa Qasem

Licensed under the MIT License.
See LICENSE file for details.
```
