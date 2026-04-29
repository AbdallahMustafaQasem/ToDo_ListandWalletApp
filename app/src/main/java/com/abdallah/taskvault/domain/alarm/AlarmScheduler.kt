package com.abdallah.taskvault.domain.alarm

import com.abdallah.taskvault.domain.model.Todo

interface AlarmScheduler {
    fun schedule(todo: Todo)
    fun cancel(todo: Todo)
}
