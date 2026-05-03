package com.abdallah.taskvault.domain.model

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
