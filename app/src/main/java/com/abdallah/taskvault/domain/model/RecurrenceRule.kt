package com.abdallah.taskvault.domain.model

enum class RecurrenceRule {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY;

    companion object {
        fun fromString(value: String?): RecurrenceRule =
            value?.let { runCatching { valueOf(it) }.getOrNull() } ?: NONE
    }
}
