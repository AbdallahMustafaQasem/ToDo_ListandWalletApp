# TODo App — Requirements V2

**Version:** 2.0  
**Date:** 2026-04-19  
**Author:** Project Manager  
**Audience:** Senior Android Developer  
**Scope:** Two focused change sets — Statistics Screen Redesign and Widget Real-time Update Fix

---

## Table of Contents

1. [Statistics Screen Redesign](#1-statistics-screen-redesign)
   - 1.1 Overview & Goals
   - 1.2 Data Requirements (ViewModel / UiState)
   - 1.3 Screen Layout & Section Specifications
   - 1.4 Animation Specifications
   - 1.5 Color Palette Usage
   - 1.6 Composable Structure
2. [Widget Real-time Update Fix](#2-widget-real-time-update-fix)
   - 2.1 Problem Statement
   - 2.2 Target Behaviour
   - 2.3 Implementation Requirements
   - 2.4 What Must NOT Change
   - 2.5 Files Affected

---

## 1. Statistics Screen Redesign

### 1.1 Overview & Goals

The existing `StatisticsScreen` already renders a completion ring, streak card, overdue card, and a priority bar chart. This redesign enriches it with four specific additions:

1. A **four-card summary header** that surfaces Total, Completed, Remaining (active), and Overdue counts as distinct animated cards — replacing the current three-chip `Row`.
2. The existing **completion ring** is retained and enhanced with a secondary teal accent ring and a gradient colour transition.
3. A new **Weekly Activity Bar Chart** — Canvas-drawn, animated — showing how many tasks were completed on each of the last 7 calendar days.
4. The existing **Priority Breakdown** chart is converted from a vertical bar chart to a **horizontal** bar chart that also shows the percentage alongside the count.
5. The existing **Streak** and **Most Productive Day** sections are rendered as a side-by-side pair of insight cards at the bottom of the screen.

The screen must remain a single scrollable `Column` within a `Scaffold` with the existing `TopAppBar`. No tabs or paging are introduced.

---

### 1.2 Data Requirements (ViewModel / UiState)

#### 1.2.1 `StatisticsUiState` — New & Changed Fields

The existing `StatisticsUiState` data class must be extended. Fields marked **NEW** do not exist yet; all others already exist and must be preserved.

```kotlin
data class StatisticsUiState(
    // ── Existing (unchanged) ─────────────────────────────────────────────
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val activeCount: Int = 0,
    val completionPercent: Float = 0f,
    val countByPriority: Map<Priority, Int> = emptyMap(),
    val streakDays: Int = 0,
    val overdueTodos: Int = 0,
    val isLoading: Boolean = true,

    // ── NEW ──────────────────────────────────────────────────────────────
    // Weekly activity: ordered list of (dayLabel, completedCount) for the
    // last 7 days, index 0 = oldest, index 6 = today.
    // dayLabel is a 3-char abbreviation e.g. "Mon", "Tue".
    val weeklyActivity: List<DayActivity> = emptyList(),

    // Day-of-week name (e.g. "Monday") that has the most completed tasks
    // across all time. Empty string if there is no data.
    val mostProductiveDay: String = ""
)

// NEW supporting data class (plain Kotlin, lives in the same file or
// in domain/model if reused elsewhere — preferably in the statistics
// package to avoid polluting the domain layer with UI-specific types).
data class DayActivity(
    val dayLabel: String,      // 3-char abbreviation, e.g. "Mon"
    val completedCount: Int    // number of todos completed on that calendar day
)
```

#### 1.2.2 `StatisticsViewModel` — Computation Logic

All new fields are derived inside the existing `getAllTodosUseCase().collect { todos -> … }` block. No new use cases or DAO queries are required; all computation is in-memory on the already-loaded list.

**Weekly Activity computation:**

```
For each of the 7 calendar days ending with today (today inclusive):
    dayStart = midnight of that day (epoch ms, local timezone)
    dayEnd   = dayStart + 86_400_000 - 1
    count    = todos.count { t ->
                   t.isCompleted
                   && t.updatedAtMillis >= dayStart
                   && t.updatedAtMillis <= dayEnd
               }
    dayLabel = 3-char weekday abbreviation from Calendar/DateTimeFormatter
```

Store as `List<DayActivity>` with index 0 = 7 days ago, index 6 = today.

**Most Productive Day computation:**

```
Group completed todos by day-of-week (1=Sunday … 7=Saturday, or use
java.util.Calendar.DAY_OF_WEEK).
Find the day-of-week with the highest count.
Convert to full English day name ("Monday", "Tuesday", …).
If no completed todos exist, return "".
```

---

### 1.3 Screen Layout & Section Specifications

The screen is a vertically scrollable `Column` with `verticalArrangement = Arrangement.spacedBy(16.dp)` and `padding(16.dp)`. Sections appear in this top-to-bottom order:

```
1.  Summary Header Cards   (4-card grid, 2×2)
2.  Completion Ring        (Card, existing — enhanced)
3.  Weekly Activity Chart  (Card, NEW)
4.  Priority Breakdown     (Card, horizontal bars — redesigned)
5.  Insight Row            (Streak card + Most Productive Day card, side by side)
```

---

#### Section 1 — Summary Header Cards

**Layout:** A 2×2 grid using two `Row` composables each with two equally weighted `weight(1f)` cards, wrapped in a `Column` with `verticalArrangement = Arrangement.spacedBy(8.dp)`.

**Four cards (label → color mapping):**

| Label | Value field | Container color | Content color |
|-------|------------|-----------------|---------------|
| Total | `totalCount` | `Purple30` (`0xFF4F378B`) | `Color.White` |
| Completed | `completedCount` | `Teal40` (`0xFF00897B`) | `Color.White` |
| Remaining | `activeCount` | `Purple80` (`0xFFD0BCFF`) | `Purple10` (`0xFF21005D`) |
| Overdue | `overdueTodos` | `MaterialTheme.colorScheme.errorContainer` | `MaterialTheme.colorScheme.onErrorContainer` |

**Each card structure:**
- `Card` with `RoundedCornerShape(16.dp)`, `CardDefaults.cardElevation(defaultElevation = 4.dp)`.
- Inner `Column` centered horizontally, `padding(vertical = 20.dp, horizontal = 8.dp)`.
- Top element: icon from `Icons.Default.*` (see below), size 28.dp, tinted with `contentColor`.
- Middle element: the animated count value in `MaterialTheme.typography.headlineSmall`, `fontWeight = FontWeight.Bold`.
- Bottom element: label string in `MaterialTheme.typography.labelMedium`, alpha `0.75f`.

**Card icons:**
- Total → `Icons.Default.FormatListBulleted`
- Completed → `Icons.Default.CheckCircle`
- Remaining → `Icons.Default.RadioButtonUnchecked`
- Overdue → `Icons.Default.Warning`

**Animation:** Each card's count animates from 0 to its target value using `animateIntAsState` with `tween(durationMillis = 800, easing = FastOutSlowInEasing)`. This already exists in the current `StatChip` — reuse the same pattern.

**Overdue card special rule:** When `overdueTodos == 0`, the Overdue card uses `MaterialTheme.colorScheme.surfaceVariant` as container and `MaterialTheme.colorScheme.onSurfaceVariant` as content (muted, non-alarming). When `overdueTodos > 0`, it uses `errorContainer` / `onErrorContainer`.

---

#### Section 2 — Completion Ring (Enhanced)

Retain the existing `AnimatedCompletionRing` composable and `Card` wrapper. Apply these enhancements:

1. **Dual-ring visual:** Draw a second, thinner arc behind the primary arc using `Teal40` (`0xFF00897B`) at 30% alpha to hint at the teal secondary palette. This teal ring spans the full 360° and acts as the track instead of the current `surfaceVariant` track.
2. **Primary arc color:** Transition the arc color between `Purple40` (`0xFF6750A4`) (0%) and `Teal40` (`0xFF00897B`) (100%) using `lerp(Purple40, Teal40, animatedPercent / 100f)`. This requires importing `androidx.compose.ui.graphics.lerp`.
3. **Percentage text:** Keep the existing `headlineMedium` / `labelMedium` text pair. No changes to text.
4. **Ring dimensions:** Increase from `Modifier.size(160.dp)` to `Modifier.size(180.dp)`. Stroke width stays at `18.dp`.
5. No other changes to this section.

---

#### Section 3 — Weekly Activity Bar Chart (NEW)

**Card structure:**
```
Card (RoundedCornerShape(16.dp), elevation = 4.dp)
└── Column (padding = 20.dp)
    ├── Text("Weekly Activity", titleMedium, Bold)
    ├── Spacer(8.dp)
    ├── WeeklyBarChart(
    │       weeklyActivity = uiState.weeklyActivity,
    │       modifier = Modifier.fillMaxWidth().height(140.dp)
    │   )
    └── (no footer text needed)
```

**`WeeklyBarChart` composable — Canvas implementation:**

```
Parameters:
  weeklyActivity: List<DayActivity>   // 7 entries, index 0 = oldest
  modifier: Modifier

Canvas drawing logic:
  barCount   = 7
  gap        = 8.dp
  barWidth   = (totalWidth - gap * 8) / 7        // equal gaps on both sides + between
  chartHeight = totalHeight - 32.dp              // bottom 32dp reserved for day labels
  maxCount   = weeklyActivity.maxOf { it.completedCount }.coerceAtLeast(1)
  cornerRad  = 6.dp

For each entry at index i:
  left      = gap + i * (barWidth + gap)
  barHeight = (count / maxCount) * chartHeight * animProgress
  top       = chartHeight - barHeight

  // Background track (full height)
  drawRoundRect(
      color        = surfaceVariant (MaterialTheme.colorScheme.surfaceVariant),
      topLeft      = Offset(left, 0f),
      size         = Size(barWidth, chartHeight),
      cornerRadius = CornerRadius(cornerRad)
  )

  // Filled bar
  // Color: today's bar (index 6) uses Purple40; all others use Teal40.
  barColor = if (i == 6) Purple40 else Teal40
  if (barHeight > 0f):
      drawRoundRect(color = barColor, topLeft = Offset(left, top), ...)

  // Count label (above bar, only if count > 0)
  drawText(count.toString(), centered above bar, 12sp, bold, onSurface color)

  // Day label (below chart area)
  drawText(dayLabel, centered, 11sp, onSurface at 65% alpha)
```

**Animation:** Single `animateFloatAsState` from `0f` to `1f` with `tween(durationMillis = 1000, easing = FastOutSlowInEasing)` drives all bar heights. The animation triggers on initial composition (the `targetValue = 1f` approach that already exists in `PriorityBarChart` — reuse the same pattern).

**Empty state:** If all 7 entries have `completedCount == 0`, draw only the background tracks and render "No completions this week" as a centered `Text` composable overlaid via a `Box` wrapping the `Canvas`.

---

#### Section 4 — Priority Breakdown (Horizontal Bars — Redesigned)

Replace the existing vertical `PriorityBarChart` with a horizontal bar chart. The section's `Card` wrapper and title "By Priority" are unchanged.

**`HorizontalPriorityBarChart` composable — Canvas implementation:**

```
Parameters:
  countByPriority: Map<Priority, Int>
  totalCount: Int   // used for percentage calculation
  modifier: Modifier

Layout:
  rowHeight  = 36.dp         // height per priority row
  totalHeight = rowHeight * 4 + gap * 3   // 4 priorities + 3 gaps
  labelWidth = 48.dp         // left-side label column
  valueWidth = 52.dp         // right-side count+pct label column
  barAreaWidth = totalWidth - labelWidth - valueWidth - 16.dp

Priority order (top → bottom): HIGH, MEDIUM, LOW, NONE

For each priority at index i:
  top = i * (rowHeight + gap)
  barFraction = count / totalCount.coerceAtLeast(1)
  barWidth = barAreaWidth * barFraction * animProgress

  // Priority name label (left-aligned, left column)
  drawText(priorityName, left=0, vertically centered in row, 12sp, bold, priorityColor)

  // Background track (full bar area width)
  drawRoundRect(surfaceVariant, x=labelWidth, y=top+4.dp, w=barAreaWidth, h=rowHeight-8.dp, r=6.dp)

  // Filled bar
  drawRoundRect(priorityColor, x=labelWidth, y=top+4.dp, w=barWidth, h=rowHeight-8.dp, r=6.dp)

  // Count + percentage label (right-aligned, right column)
  label = "$count  (${pct}%)"   where pct = (barFraction * 100).roundToInt()
  drawText(label, right-aligned in valueWidth column, 11sp, onSurface at 80% alpha)
```

**Priority colors** (already defined in `Color.kt`, reuse):
- `Priority.NONE` → `PriorityNoneColor` (`0xFF9E9E9E`)
- `Priority.LOW` → `PriorityLowColor` (`0xFF43A047`)
- `Priority.MEDIUM` → `PriorityMediumColor` (`0xFFFFA000`)
- `Priority.HIGH` → `PriorityHighColor` (`0xFFE53935`)

**Card height:** Remove the fixed `Modifier.height(180.dp)`. Let the canvas compute its own height as `(rowHeight * 4 + gap * 3)` expressed as a fixed `Modifier.height(...)` on the composable.

**Animation:** Same `animateFloatAsState` pattern as all other charts — `tween(1000ms, FastOutSlowInEasing)`.

---

#### Section 5 — Insight Row (Streak + Most Productive Day)

Replace the separate streak card and remove the current standalone overdue card (overdue is now surfaced in the summary header). Show streak and most productive day as two equal-width cards side by side in a `Row`.

**Layout:**
```
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
) {
    StreakCard(modifier = Modifier.weight(1f), streakDays = uiState.streakDays)
    MostProductiveDayCard(modifier = Modifier.weight(1f), dayName = uiState.mostProductiveDay)
}
```

**`StreakCard`:**
- `Card`, `RoundedCornerShape(16.dp)`, `containerColor = MaterialTheme.colorScheme.tertiaryContainer`, elevation 4dp.
- Icon: `Icons.Default.LocalFireDepartment`, size 32dp, tint `Color(0xFFFF6F00)`.
- Value: `"${streakDays}"` in `titleLarge`, bold, `onTertiaryContainer`.
- Sub-label: `"day streak"` in `bodySmall`, `onTertiaryContainer` at 70% alpha.
- Footer text: if `streakDays == 0` → `"Start today!"` else `"Keep it up!"` in `labelSmall` at 60% alpha.

**`MostProductiveDayCard`:**
- `Card`, `RoundedCornerShape(16.dp)`, `containerColor = Purple90` (`0xFFEADDFF`), `contentColor = Purple10` (`0xFF21005D`), elevation 4dp.
- Icon: `Icons.Default.EmojiEvents` (trophy), size 32dp, tint `Purple40`.
- Value: if `dayName.isEmpty()` → `"—"` else the day name (e.g. `"Monday"`) in `titleMedium`, bold, `Purple10`.
- Sub-label: `"most productive"` in `bodySmall`, `Purple10` at 70% alpha.
- Footer text: if `dayName.isEmpty()` → `"No data yet"` else `"day of week"` in `labelSmall` at 60% alpha.

Both cards share the same internal composable structure:
```
Card {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(...)
        Text(value)
        Text(subLabel)
        Text(footerText)
    }
}
```

---

### 1.4 Animation Specifications

| Component | Mechanism | Duration | Easing |
|-----------|-----------|----------|--------|
| Summary card counts | `animateIntAsState` per card | 800ms | `FastOutSlowInEasing` |
| Completion ring arc sweep | `animateFloatAsState` (existing) | 1200ms | `FastOutSlowInEasing` |
| Completion ring color lerp | Derived from the same animated float above | 1200ms | Same float |
| Weekly bar heights | Single `animateFloatAsState` (`0f → 1f`) | 1000ms | `FastOutSlowInEasing` |
| Horizontal priority bar widths | Single `animateFloatAsState` (`0f → 1f`) | 1000ms | `FastOutSlowInEasing` |

All animations run once on initial composition. There is no looping or repeat. Re-navigation to the screen re-triggers all animations naturally because the composable is recreated.

---

### 1.5 Color Palette Usage

All colors reference either the existing `Color.kt` constants or `MaterialTheme.colorScheme.*` roles. No new hardcoded hex values are introduced except where explicitly listed below.

| UI element | Color reference |
|-----------|----------------|
| "Total" card background | `Purple30` (`Color(0xFF4F378B)`) |
| "Completed" card background | `Teal40` (`Color(0xFF00897B)`) |
| "Remaining" card background | `Purple80` (`Color(0xFFD0BCFF)`) |
| "Remaining" card text | `Purple10` (`Color(0xFF21005D)`) |
| "Overdue" card (overdue > 0) | `MaterialTheme.colorScheme.errorContainer` / `onErrorContainer` |
| "Overdue" card (overdue = 0) | `MaterialTheme.colorScheme.surfaceVariant` / `onSurfaceVariant` |
| Completion ring track | `Teal40` at 30% alpha |
| Completion ring arc (0%) | `Purple40` (`Color(0xFF6750A4)`) |
| Completion ring arc (100%) | `Teal40` (`Color(0xFF00897B)`) |
| Weekly chart — today's bar | `Purple40` |
| Weekly chart — other bars | `Teal40` |
| Weekly chart background track | `MaterialTheme.colorScheme.surfaceVariant` |
| Priority bars | `PriorityNoneColor`, `PriorityLowColor`, `PriorityMediumColor`, `PriorityHighColor` (existing) |
| Most Productive Day card background | `Purple90` (`Color(0xFFEADDFF)`) |
| Most Productive Day card text | `Purple10` (`Color(0xFF21005D)`) |
| Streak card background | `MaterialTheme.colorScheme.tertiaryContainer` |
| Flame icon tint | `Color(0xFFFF6F00)` (existing — unchanged) |

---

### 1.6 Composable Structure

The following composables need to be created or modified in `StatisticsScreen.kt`:

**New private composables:**
- `SummaryHeaderGrid(uiState: StatisticsUiState, modifier: Modifier)` — 2×2 card grid
- `SummaryCard(label, value, icon, containerColor, contentColor, modifier)` — single summary card with animated count
- `WeeklyBarChart(weeklyActivity: List<DayActivity>, modifier: Modifier)` — Canvas-drawn weekly chart
- `HorizontalPriorityBarChart(countByPriority: Map<Priority, Int>, totalCount: Int, modifier: Modifier)` — replaces `PriorityBarChart`
- `StreakCard(streakDays: Int, modifier: Modifier)`
- `MostProductiveDayCard(dayName: String, modifier: Modifier)`

**Modified existing composables:**
- `AnimatedCompletionRing` — enhanced dual-ring and lerp color (no signature change)
- `StatisticsScreen` — updated body to use the new section structure; remove old `StatChip` row, remove standalone streak/overdue cards, add `WeeklyBarChart` and `HorizontalPriorityBarChart` sections

**Removed composables:**
- `StatChip` — replaced by `SummaryCard`
- `PriorityBarChart` — replaced by `HorizontalPriorityBarChart`

**`DayActivity` data class** must be added to `StatisticsViewModel.kt` (or a new `StatisticsModels.kt` file in the same `ui.statistics` package).

---

## 2. Widget Real-time Update Fix

### 2.1 Problem Statement

The current `TodoWidget.provideGlance` implementation calls `getUpcomingTodos().first()` — it collects only the **first** emission from the Flow and then stops. This means the widget displays a static snapshot of the database at the moment it was last rendered. Any subsequent database changes (new todo created, todo completed, todo deleted) are not reflected until something externally calls `TodoWidget().updateAll(context)`.

The current architecture relies on `notifyWidget()` call-sites scattered across the app and `ActionCallback` implementations each ending with `updateAll(context)`. This is fragile: any code path that mutates the database without calling `notifyWidget()` produces a stale widget.

### 2.2 Target Behaviour

The widget must:

1. **React to every database change automatically**, without any external trigger. When a todo is added, updated, completed, or deleted via the main app, the widget must refresh within the same UI frame cycle that the database emits the new value.
2. **Eliminate all manual `notifyWidget()` / `updateAll()` call-sites** from the repository and use case layers. The widget is self-updating.
3. Continue to show: the top 3 upcoming non-completed todos, an "Add Todo" button, and a refresh button.
4. The refresh button remains as a user-facing manual refresh escape hatch, but it is no longer needed for correctness.

### 2.3 Implementation Requirements

#### 2.3.1 Use `collectAsState()` inside `provideContent`

Jetpack Glance provides `collectAsState()` as a Glance-aware state holder that bridges a `Flow` into the composable tree. Replace the current `.first()` snapshot approach with a live `collectAsState()`.

**Required change to `provideGlance`:**

```kotlin
override suspend fun provideGlance(context: Context, id: GlanceId) {
    val dao = EntryPointAccessors
        .fromApplication(context.applicationContext, TodoWidgetEntryPoint::class.java)
        .todoDao()

    provideContent {
        // collectAsState() subscribes to the Flow and recomposes the
        // Glance UI on every new emission — no updateAll() needed.
        val entities by dao.getUpcomingTodos().collectAsState(initial = emptyList())

        val todos = entities.map { entity ->
            WidgetTodoItem(
                id            = entity.id,
                title         = entity.title,
                dueDateMillis = entity.dueDateMillis,
                isCompleted   = entity.isCompleted
            )
        }

        GlanceTheme(colors = widgetColors) {
            TodoWidgetContent(todos = todos)
        }
    }
}
```

**Import required:**
```kotlin
import androidx.glance.appwidget.state.collectAsState
// or, depending on Glance version:
import androidx.glance.appwidget.lazy.collectAsLazyListState  // NOT this one
// Correct import (Glance 1.1.0+):
import androidx.glance.appwidget.collectAsState
```

> **Note to developer:** As of `androidx.glance:glance-appwidget:1.1.0` (the version in `libs.versions.toml`), `Flow.collectAsState()` is available as an extension on `Flow` inside a `@GlanceComposable` context via `androidx.glance.appwidget.GlanceRemoteViews` coroutine infrastructure. Verify the exact import path against the installed Glance version. If the extension is not available at `glance-appwidget:1.1.0`, upgrade to `1.1.1` or later where it is confirmed stable.

#### 2.3.2 DAO Query Adjustment — Limit to 3

The existing `getUpcomingTodos()` DAO query uses `LIMIT 5`. The widget now shows only the top 3, per the redesigned spec. Change the limit:

```kotlin
// In TodoDao.kt — modify the existing query
@Query("SELECT * FROM todos WHERE is_completed = 0 ORDER BY due_date_millis ASC LIMIT 3")
fun getUpcomingTodos(): Flow<List<TodoEntity>>
```

> **Warning:** `getUpcomingTodos()` is also referenced in `REQUIREMENTS.md` Section 8 where F-20 specifies "up to 5 upcoming todos". This requirements document supersedes that for the widget display — the widget now shows **top 3**. Update `REQUIREMENTS.md` Section 8.3 and F-20 accordingly if doing a full document pass.

#### 2.3.3 Remove `updateAll()` from `ToggleCompletionCallback`

Because the widget now self-updates via `collectAsState()`, the explicit `updateAll(context)` call at the end of `ToggleCompletionCallback.onAction` is redundant. It may be kept as a safety net for widget framework edge cases where the Flow emission does not propagate in time, but it must NOT be relied upon for correctness.

Recommended: keep it in `ToggleCompletionCallback` as a belt-and-suspenders call. Remove it from the repository layer entirely.

#### 2.3.4 Remove `notifyWidget()` from Repository / Use Cases

Identify and remove any call-sites of the form:
```kotlin
TodoWidget().updateAll(context)
// or
context.sendBroadcast(...)  // widget refresh intents
```
from `TodoRepositoryImpl`, all use cases, and any ViewModel that currently triggers widget updates after mutations. The widget is self-healing via the live Flow.

#### 2.3.5 `RefreshWidgetCallback` — Retain but Simplify

The refresh button remains. Its `ActionCallback` no longer needs to do anything beyond nudging the widget to re-render, which `collectAsState()` already handles. Simplify to a no-op or keep the `updateAll` as a user-visible "force refresh" that handles edge cases (e.g., widget was off-screen and missed emissions):

```kotlin
class RefreshWidgetCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // collectAsState() keeps the widget live; this is a manual fallback only.
        TodoWidget().updateAll(context)
    }
}
```

### 2.4 What Must NOT Change

The following widget behaviours and UI elements are out of scope for this fix and must remain exactly as they are:

| Element | Requirement |
|---------|-------------|
| Widget title bar | "My Todos" text + app icon + refresh button — unchanged |
| Todo row layout | Checkbox icon, title, optional due-date label — unchanged |
| Overdue date color | Red for overdue due dates — unchanged |
| "Add Todo" footer button | Opens `MainActivity` with `EXTRA_OPEN_ADD_SCREEN = true` — unchanged |
| Todo row tap | Opens `MainActivity` with `EXTRA_OPEN_DETAIL_TODO_ID` — unchanged |
| `ToggleCompletionCallback` logic | Reads current state, flips `isCompleted`, calls `dao.updateCompletion` — unchanged |
| Light / dark theme support | `ColorProviders(light = LightColorScheme, dark = DarkColorScheme)` — unchanged |
| Widget metadata XML | `todo_widget_info.xml` — unchanged |
| `TodoWidgetEntryPoint` Hilt entry point | Unchanged |

### 2.5 Files Affected

| File | Change type | Description |
|------|-------------|-------------|
| `widget/TodoWidget.kt` | Modify | Replace `.first()` with `collectAsState(initial = emptyList())` inside `provideContent`; remove `.map {}` from outside `provideContent`; simplify `RefreshWidgetCallback` |
| `data/local/TodoDao.kt` | Modify | Change `LIMIT 5` to `LIMIT 3` in `getUpcomingTodos()` |
| `data/repository/TodoRepositoryImpl.kt` | Modify (if applicable) | Remove any `TodoWidget().updateAll(context)` calls |
| Use case files (`AddTodoUseCase`, `UpdateTodoUseCase`, `DeleteTodoUseCase`, `ToggleTodoCompletionUseCase`) | Modify (if applicable) | Remove any widget update trigger calls |

---

## Appendix A — Summary of New/Changed Symbols

### New Kotlin symbols introduced

| Symbol | Kind | Location |
|--------|------|----------|
| `DayActivity` | `data class` | `ui/statistics/StatisticsViewModel.kt` |
| `StatisticsUiState.weeklyActivity` | Property | `ui/statistics/StatisticsViewModel.kt` |
| `StatisticsUiState.mostProductiveDay` | Property | `ui/statistics/StatisticsViewModel.kt` |
| `SummaryHeaderGrid` | `@Composable` | `ui/statistics/StatisticsScreen.kt` |
| `SummaryCard` | `@Composable` | `ui/statistics/StatisticsScreen.kt` |
| `WeeklyBarChart` | `@Composable` | `ui/statistics/StatisticsScreen.kt` |
| `HorizontalPriorityBarChart` | `@Composable` | `ui/statistics/StatisticsScreen.kt` |
| `StreakCard` | `@Composable` | `ui/statistics/StatisticsScreen.kt` |
| `MostProductiveDayCard` | `@Composable` | `ui/statistics/StatisticsScreen.kt` |

### Removed Kotlin symbols

| Symbol | Location | Replacement |
|--------|----------|-------------|
| `StatChip` | `ui/statistics/StatisticsScreen.kt` | `SummaryCard` |
| `PriorityBarChart` | `ui/statistics/StatisticsScreen.kt` | `HorizontalPriorityBarChart` |

### Changed DAO queries

| Method | Change |
|--------|--------|
| `TodoDao.getUpcomingTodos()` | `LIMIT 5` → `LIMIT 3` |

---

*End of Requirements V2*
