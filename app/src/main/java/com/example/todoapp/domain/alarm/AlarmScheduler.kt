package com.example.todoapp.domain.alarm

import com.example.todoapp.domain.model.Todo

interface AlarmScheduler {
    fun schedule(todo: Todo)
    fun cancel(todo: Todo)
}
