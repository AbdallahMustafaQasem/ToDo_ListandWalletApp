package com.abdallah.taskvault.domain.repository

import com.abdallah.taskvault.domain.model.TaskAssignment

interface TaskAssignmentRepository {
    suspend fun assignTask(assignment: TaskAssignment): String
    suspend fun getAssignmentsByAssigner(userId: String): List<TaskAssignment>
    suspend fun getAssignmentsForAssignee(userId: String): List<TaskAssignment>
    suspend fun updateStatus(assignmentId: String, status: com.abdallah.taskvault.domain.model.AssignmentStatus)
}
