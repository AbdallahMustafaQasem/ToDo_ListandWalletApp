package com.abdallah.taskvault.util

import java.util.Calendar

data class ParsedTaskInput(
    val title: String,
    val dueDateMillis: Long? = null,
    val hasDateHint: Boolean = false
)

object NaturalLanguageParser {

    fun parse(input: String): ParsedTaskInput {
        var text = input.trim()
        var dueDateMillis: Long? = null
        var hasDateHint = false

        val now = Calendar.getInstance()

        // ── Time: "at 3pm", "at 14:30", "at 9am" ──────────────────────────
        val timeRegex = Regex("""(?i)\bat\s+(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""")
        val timeMatch = timeRegex.find(text)
        var hour = -1
        var minute = 0
        if (timeMatch != null) {
            hour = timeMatch.groupValues[1].toInt()
            minute = timeMatch.groupValues[2].takeIf { it.isNotBlank() }?.toInt() ?: 0
            val ampm = timeMatch.groupValues[3].lowercase()
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0
            text = text.removeRange(timeMatch.range).trim()
            hasDateHint = true
        }

        val cal = Calendar.getInstance()

        // ── Relative days ──────────────────────────────────────────────────
        val todayRegex   = Regex("""(?i)\btoday\b""")
        val tomorrowRegex= Regex("""(?i)\btomorrow\b""")
        val inNDaysRegex = Regex("""(?i)\bin\s+(\d+)\s+days?\b""")
        val nextWeekRegex= Regex("""(?i)\bnext\s+week\b""")
        val weekdayRegex = Regex("""(?i)\b(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\b""")

        when {
            todayRegex.containsMatchIn(text) -> {
                text = text.replace(todayRegex, "").trim()
                hasDateHint = true
            }
            tomorrowRegex.containsMatchIn(text) -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                text = text.replace(tomorrowRegex, "").trim()
                hasDateHint = true
            }
            inNDaysRegex.containsMatchIn(text) -> {
                val m = inNDaysRegex.find(text)!!
                cal.add(Calendar.DAY_OF_YEAR, m.groupValues[1].toInt())
                text = text.removeRange(m.range).trim()
                hasDateHint = true
            }
            nextWeekRegex.containsMatchIn(text) -> {
                cal.add(Calendar.WEEK_OF_YEAR, 1)
                text = text.replace(nextWeekRegex, "").trim()
                hasDateHint = true
            }
            weekdayRegex.containsMatchIn(text) -> {
                val m = weekdayRegex.find(text)!!
                val targetDay = when (m.groupValues[1].lowercase()) {
                    "monday"    -> Calendar.MONDAY
                    "tuesday"   -> Calendar.TUESDAY
                    "wednesday" -> Calendar.WEDNESDAY
                    "thursday"  -> Calendar.THURSDAY
                    "friday"    -> Calendar.FRIDAY
                    "saturday"  -> Calendar.SATURDAY
                    "sunday"    -> Calendar.SUNDAY
                    else        -> -1
                }
                if (targetDay != -1) {
                    val today = now.get(Calendar.DAY_OF_WEEK)
                    var diff = targetDay - today
                    if (diff <= 0) diff += 7
                    cal.add(Calendar.DAY_OF_YEAR, diff)
                    text = text.removeRange(m.range).trim()
                    hasDateHint = true
                }
            }
        }

        if (hasDateHint) {
            if (hour >= 0) {
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 9)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            dueDateMillis = cal.timeInMillis
        }

        // Clean up leftover punctuation / double spaces
        val cleanedTitle = text.replace(Regex("""\s{2,}"""), " ").trim().trimEnd(',', '-', '–')

        return ParsedTaskInput(
            title = cleanedTitle.ifBlank { input.trim() },
            dueDateMillis = dueDateMillis,
            hasDateHint = hasDateHint
        )
    }
}
