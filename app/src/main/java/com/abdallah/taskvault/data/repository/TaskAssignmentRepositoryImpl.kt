package com.abdallah.taskvault.data.repository

import com.abdallah.taskvault.domain.model.AssignmentStatus
import com.abdallah.taskvault.domain.model.Priority
import com.abdallah.taskvault.domain.model.TaskAssignment
import com.abdallah.taskvault.domain.repository.AuthRepository
import com.abdallah.taskvault.domain.repository.TaskAssignmentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskAssignmentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : TaskAssignmentRepository {

    private val assignmentsCol get() = firestore.collection("task_assignments")

    override suspend fun assignTask(assignment: TaskAssignment): String {
        val docRef = assignmentsCol.document()
        val data = mapOf(
            "title"          to assignment.title,
            "description"    to assignment.description,
            "priority"       to assignment.priority.name,
            "dueDateMillis"  to assignment.dueDateMillis,
            "assignerId"     to assignment.assignerId,
            "assignerName"   to assignment.assignerName,
            "assigneeIds"    to assignment.assigneeIds,
            "assigneeNames"  to assignment.assigneeNames,
            "status"         to assignment.status.name,
            "createdAtMillis" to assignment.createdAtMillis
        )
        docRef.set(data).await()

        // Create a notification document for each assignee
        assignment.assigneeIds.forEach { assigneeId ->
            firestore.collection("users").document(assigneeId)
                .collection("notifications").add(
                    mapOf(
                        "type"        to "task_assigned",
                        "title"       to assignment.title,
                        "fromName"    to assignment.assignerName,
                        "fromId"      to assignment.assignerId,
                        "assignmentId" to docRef.id,
                        "read"        to false,
                        "timestamp"   to System.currentTimeMillis()
                    )
                ).await()
        }
        return docRef.id
    }

    override suspend fun getAssignmentsByAssigner(userId: String): List<TaskAssignment> {
        val snapshot = assignmentsCol.whereEqualTo("assignerId", userId).get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                TaskAssignment(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    priority = try { Priority.valueOf(doc.getString("priority") ?: "NONE") } catch (_: Exception) { Priority.NONE },
                    dueDateMillis = doc.getLong("dueDateMillis"),
                    assignerId = doc.getString("assignerId") ?: "",
                    assignerName = doc.getString("assignerName") ?: "",
                    assigneeIds = (doc.get("assigneeIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    assigneeNames = (doc.get("assigneeNames") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    status = try { AssignmentStatus.valueOf(doc.getString("status") ?: "PENDING") } catch (_: Exception) { AssignmentStatus.PENDING },
                    createdAtMillis = doc.getLong("createdAtMillis") ?: 0L
                )
            } catch (_: Exception) { null }
        }
    }

    override suspend fun getAssignmentsForAssignee(userId: String): List<TaskAssignment> {
        val snapshot = assignmentsCol.whereArrayContains("assigneeIds", userId).get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                TaskAssignment(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    priority = try { Priority.valueOf(doc.getString("priority") ?: "NONE") } catch (_: Exception) { Priority.NONE },
                    dueDateMillis = doc.getLong("dueDateMillis"),
                    assignerId = doc.getString("assignerId") ?: "",
                    assignerName = doc.getString("assignerName") ?: "",
                    assigneeIds = (doc.get("assigneeIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    assigneeNames = (doc.get("assigneeNames") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    status = try { AssignmentStatus.valueOf(doc.getString("status") ?: "PENDING") } catch (_: Exception) { AssignmentStatus.PENDING },
                    createdAtMillis = doc.getLong("createdAtMillis") ?: 0L
                )
            } catch (_: Exception) { null }
        }
    }

    override suspend fun updateStatus(assignmentId: String, status: AssignmentStatus) {
        val docRef = assignmentsCol.document(assignmentId)
        docRef.update("status", status.name).await()

        // Notify the assigner about the status change
        val snapshot = docRef.get().await()
        val assignerId  = snapshot.getString("assignerId")  ?: return
        val title       = snapshot.getString("title")       ?: return
        val updaterName = authRepository.currentUser.value?.displayName
            ?: authRepository.getCurrentUserId()
            ?: "Someone"

        firestore.collection("users").document(assignerId)
            .collection("notifications").add(
                mapOf(
                    "type"        to "status_updated",
                    "title"       to title,
                    "fromName"    to updaterName,
                    "fromId"      to (authRepository.getCurrentUserId() ?: ""),
                    "assignmentId" to assignmentId,
                    "newStatus"   to status.name,
                    "read"        to false,
                    "timestamp"   to System.currentTimeMillis()
                )
            ).await()
    }
}
