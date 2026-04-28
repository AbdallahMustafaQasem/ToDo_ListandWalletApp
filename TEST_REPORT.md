# TEST REPORT — Post-Change QA Review
**Date:** 2026-04-19
**Reviewer:** Senior Android QA (automated)
**Scope:** TodoWidget.kt, StatisticsScreen.kt, StatisticsViewModel.kt, TodoRepositoryImpl.kt, AppModule.kt, RepositoryModule.kt

---

## 1. TodoWidget.kt — Widget Live-Update

### Bug Found: CRITICAL — Invalid `collectAsState` in Glance scope

**Symptom:**
The original code imported `androidx.compose.runtime.collectAsState` and called
`.collectAsState(initial = emptyList())` inside `provideContent { }`. Glance uses its own
composition runtime; the standard Compose `collectAsState` extension is unavailable in a
`@GlanceComposable` scope. At runtime this either fails to compile or compiles but produces a
dangling observer that never triggers recomposition — leaving the widget perpetually empty.

**Root cause:**
Glance's composition is not backed by the standard Compose `Recomposer`. Extensions such as
`collectAsState` that rely on `rememberCoroutineScope` / `DisposableEffect` do not function
inside Glance composables.

**Fix applied:**
- Added `override val stateDefinition = TodoWidgetStateDefinition` so Glance persists state
  via DataStore (the `WidgetState` / `TodoWidgetStateDefinition` infrastructure already existed).
- Inside `provideGlance`, replaced the inline `collectAsState` call with a `coroutineScope { }`
  block that:
  1. Launches a child coroutine that collects `dao.getUpcomingTodos()` (a Room Flow) indefinitely.
  2. On each emission, writes updated `WidgetState` via `updateAppWidgetState(...)` and calls
     `update(context, id)` to trigger a re-render.
  3. Concurrently calls `provideContent { }`, which reads state via `currentState<WidgetState>()`.
- Removed the invalid `import androidx.compose.runtime.collectAsState` and
  `import androidx.compose.runtime.getValue`.
- Added `import kotlinx.coroutines.coroutineScope`, `import kotlinx.coroutines.launch`,
  `import androidx.glance.appwidget.state.updateAppWidgetState`, and
  `import androidx.glance.currentState`.

**Files changed:** `app/src/main/java/com/example/todoapp/widget/TodoWidget.kt`

---

## 2. StatisticsScreen.kt — Statistics Screen Redesign

### Check: `drawIntoCanvas` usage — PASS

`drawIntoCanvas { canvas -> canvas.nativeCanvas.drawText(...) }` is used correctly in
`WeeklyBarChart`. The old bug (`drawContext.canvas.nativeCanvas`) is NOT present.

### Check: `animateIntAsState` usage — PASS

```kotlin
val animatedCount by animateIntAsState(targetValue = count, ...)
```
`animateIntAsState` returns `State<Int>`; the `by` delegate unwraps it to `Int` automatically.
No `.value` call needed. Correct.

### Check: Imports — PASS

All required imports are present:
- `androidx.compose.ui.graphics.drawscope.drawIntoCanvas` ✓
- `androidx.compose.ui.graphics.nativeCanvas` ✓
- Priority color symbols (`PriorityHighColor`, `PriorityMediumColor`, `PriorityLowColor`,
  `PriorityNoneColor`) from `com.example.todoapp.ui.theme.*` ✓ (all four defined in Color.kt)
- `Purple40` used in `DualRing` ✓ (defined in Color.kt)
- `DayActivity` is in the same package (`ui.statistics`) — no cross-package import needed ✓

### Check: All UI state fields match ViewModel — PASS

| Screen reference          | StatisticsUiState field            | Present? |
|---------------------------|------------------------------------|----------|
| `uiState.isLoading`       | `isLoading: Boolean`               | ✓        |
| `uiState.totalCount`      | `totalCount: Int`                  | ✓        |
| `uiState.completedCount`  | `completedCount: Int`              | ✓        |
| `uiState.activeCount`     | `activeCount: Int`                 | ✓        |
| `uiState.overdueTodos`    | `overdueTodos: Int`                | ✓        |
| `uiState.completionPercent` | `completionPercent: Float`       | ✓        |
| `uiState.weeklyActivity`  | `weeklyActivity: List<DayActivity>`| ✓        |
| `uiState.countByPriority` | `countByPriority: Map<Priority,Int>`| ✓       |
| `uiState.streakDays`      | `streakDays: Int`                  | ✓        |
| `uiState.mostProductiveDay` | `mostProductiveDay: String`      | ✓        |

No unresolved field references.

---

## 3. StatisticsViewModel.kt — Business Logic

### Check: `buildWeeklyActivity` 7-day window — PASS

Iterates `daysBack` from 6 down to 0, sets each `Calendar` to midnight, computes `dayStart` /
`dayEnd`, counts completed todos whose `updatedAtMillis` falls in that window, and appends a
`DayActivity`. Result: 7 entries, index 0 = 6 days ago, index 6 = today. Correct.

### Check: `mostProductiveDay` empty case — PASS

`if (completedTodos.isEmpty()) return ""` guards against empty input. `IntArray(8)` uses
Calendar day-of-week indices 1–7 (index 0 stays 0). `maxByOrNull` on indices 0..7 will never
pick index 0 when any real day has at least one completion. `dayNames[bestDow]` maps correctly
to the full day name string. Correct.

### Check: Streak computation — PASS

Streak walks backward from today's midnight checking `completedDates` (a `Set<Long>` of
midnight timestamps) and increments until a day is missing from the set. Correct.

---

## 4. TodoRepositoryImpl.kt — Context / notifyWidget Removal

### Check: `@ApplicationContext` context removed — PASS

`TodoRepositoryImpl` only injects `TodoDao`. No `Context` parameter, no `@ApplicationContext`,
no `notifyWidget()` call. Clean.

### Check: AppModule.kt — PASS

`AppModule` does NOT pass `Context` to the repository. It uses `@ApplicationContext` only to
construct `TodoDatabase`, `AlarmManager`, and `NotificationManager` — all correct and
independent of the repository change.

### Check: RepositoryModule.kt — PASS

`RepositoryModule.bindTodoRepository(impl: TodoRepositoryImpl)` compiles because
`TodoRepositoryImpl`'s only constructor parameter is `TodoDao`, which Hilt already provides via
`AppModule.provideTodoDao`.

---

## Summary

| Area                                              | Issue                                 | Severity   | Status              |
|---------------------------------------------------|---------------------------------------|------------|---------------------|
| `TodoWidget.kt` — `collectAsState` in Glance scope| Invalid API; widget never updates     | CRITICAL   | Fixed               |
| `StatisticsScreen.kt` — all imports               | All present                           | —          | Pass (no change)    |
| `StatisticsScreen.kt` — `drawIntoCanvas` pattern  | Correct                               | —          | Pass                |
| `StatisticsScreen.kt` — `animateIntAsState` usage | Correct (`by` delegate)               | —          | Pass                |
| `StatisticsViewModel.kt` — `buildWeeklyActivity`  | Correct 7-day window                  | —          | Pass                |
| `StatisticsViewModel.kt` — `mostProductiveDay`    | Empty case guarded                    | —          | Pass                |
| `StatisticsUiState` fields vs. screen             | All fields present                    | —          | Pass                |
| `TodoRepositoryImpl.kt` — context removed         | Already removed                       | —          | Pass                |
| `AppModule.kt` — no Context passed to repo        | Correct                               | —          | Pass                |
| `RepositoryModule.kt` — binding still valid       | Correct                               | —          | Pass                |

**Verdict: ONE critical bug found and fixed.**
The widget live-update used `collectAsState` from the standard Compose runtime inside a Glance
composable — this is unsupported in Glance's composition model. Replaced with the correct
Glance pattern: Flow collection in a sibling coroutine writing to DataStore-backed Glance state,
with `provideContent` reading that state via `currentState<WidgetState>()`. All other changed
code reviewed clean with no further issues.
