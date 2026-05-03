package com.abdallah.taskvault.data.sync

import android.util.Log
import com.abdallah.taskvault.data.local.BillDao
import com.abdallah.taskvault.data.local.BillEntity
import com.abdallah.taskvault.data.local.HabitDao
import com.abdallah.taskvault.data.local.HabitEntity
import com.abdallah.taskvault.data.local.MemoirDao
import com.abdallah.taskvault.data.local.MemoirEntity
import com.abdallah.taskvault.data.local.NoteDao
import com.abdallah.taskvault.data.local.NoteEntity
import com.abdallah.taskvault.data.local.PasswordDao
import com.abdallah.taskvault.data.local.PasswordEntity
import com.abdallah.taskvault.data.local.SubtaskDao
import com.abdallah.taskvault.data.local.SubtaskEntity
import com.abdallah.taskvault.data.local.TodoDao
import com.abdallah.taskvault.data.local.TodoEntity
import com.abdallah.taskvault.data.local.TodoListDao
import com.abdallah.taskvault.data.local.TodoListEntity
import com.abdallah.taskvault.data.local.WalletBudgetEntity
import com.abdallah.taskvault.data.local.WalletCategoryEntity
import com.abdallah.taskvault.data.local.WalletDao
import com.abdallah.taskvault.data.local.WalletTransactionEntity
import com.abdallah.taskvault.domain.model.Bill
import com.abdallah.taskvault.domain.model.Habit
import com.abdallah.taskvault.domain.model.Memoir
import com.abdallah.taskvault.domain.model.Note
import com.abdallah.taskvault.domain.model.Password
import com.abdallah.taskvault.domain.model.Priority
import com.abdallah.taskvault.domain.model.Subtask
import com.abdallah.taskvault.domain.model.Todo
import com.abdallah.taskvault.domain.model.TodoList
import com.abdallah.taskvault.domain.model.TransactionType
import com.abdallah.taskvault.domain.model.WalletBudget
import com.abdallah.taskvault.domain.model.WalletCategory
import com.abdallah.taskvault.domain.model.WalletTransaction
import com.abdallah.taskvault.domain.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val todoDao: TodoDao,
    private val subtaskDao: SubtaskDao,
    private val todoListDao: TodoListDao,
    private val walletDao: WalletDao,
    private val noteDao: NoteDao,
    private val memoirDao: MemoirDao,
    private val passwordDao: PasswordDao,
    private val habitDao: HabitDao,
    private val billDao: BillDao
) {

    private fun userRoot() = authRepository.getCurrentUserId()
        ?.let { firestore.collection("users").document(it) }

    // ─── Todos ───────────────────────────────────────────────────────────────

    suspend fun syncTodo(todo: Todo) {
        val root = userRoot() ?: return
        val data = mapOf(
            "id"              to todo.id,
            "title"           to todo.title,
            "description"     to todo.description,
            "isCompleted"     to todo.isCompleted,
            "dueDateMillis"   to todo.dueDateMillis,
            "priority"        to todo.priority.name,
            "reminderEnabled" to todo.reminderEnabled,
            "recurrenceRule"  to todo.recurrenceRule.name,
            "listId"          to todo.listId,
            "isDeleted"       to todo.isDeleted,
            "deletedAtMillis" to todo.deletedAtMillis,
            "createdAtMillis" to todo.createdAtMillis,
            "updatedAtMillis" to todo.updatedAtMillis
        )
        runCatching {
            root.collection("todos").document(todo.id.toString())
                .set(data, SetOptions.merge()).await()
        }
    }

    suspend fun deleteTodoSync(todoId: Long) {
        val root = userRoot() ?: return
        runCatching {
            root.collection("todos").document(todoId.toString()).delete().await()
        }
    }

    // ─── Subtasks ────────────────────────────────────────────────────────────

    suspend fun syncSubtask(subtask: Subtask) {
        val root = userRoot() ?: return
        val data = mapOf(
            "id"              to subtask.id,
            "todoId"          to subtask.todoId,
            "title"           to subtask.title,
            "isCompleted"     to subtask.isCompleted,
            "position"        to subtask.position,
            "createdAtMillis" to subtask.createdAtMillis
        )
        runCatching {
            root.collection("subtasks").document(subtask.id.toString())
                .set(data, SetOptions.merge()).await()
        }
    }

    suspend fun deleteSubtaskSync(subtaskId: Long) {
        val root = userRoot() ?: return
        runCatching {
            root.collection("subtasks").document(subtaskId.toString()).delete().await()
        }
    }

    // ─── Todo Lists ──────────────────────────────────────────────────────────

    suspend fun syncTodoList(list: TodoList) {
        val root = userRoot() ?: return
        val data = mapOf(
            "id"              to list.id,
            "name"            to list.name,
            "colorHex"        to list.colorHex,
            "icon"            to list.icon,
            "createdAtMillis" to list.createdAtMillis
        )
        runCatching {
            root.collection("todo_lists").document(list.id.toString())
                .set(data, SetOptions.merge()).await()
        }
    }

    suspend fun deleteTodoListSync(listId: Long) {
        val root = userRoot() ?: return
        runCatching {
            root.collection("todo_lists").document(listId.toString()).delete().await()
        }
    }

    // ─── Wallet ──────────────────────────────────────────────────────────────

    suspend fun syncTransaction(tx: WalletTransaction) {
        val root = userRoot() ?: return
        val data = mapOf(
            "id"           to tx.id,
            "amount"       to tx.amount,
            "type"         to tx.type.name,
            "categoryId"   to tx.categoryId,
            "categoryName" to tx.categoryName,
            "categoryIcon" to tx.categoryIcon,
            "notes"        to tx.notes,
            "dateMillis"   to tx.dateMillis
        )
        runCatching {
            root.collection("wallet_transactions").document(tx.id.toString())
                .set(data, SetOptions.merge()).await()
        }
    }

    suspend fun deleteTransactionSync(txId: Long) {
        val root = userRoot() ?: return
        runCatching {
            root.collection("wallet_transactions").document(txId.toString()).delete().await()
        }
    }

    suspend fun syncCategory(cat: WalletCategory) {
        val root = userRoot() ?: return
        val data = mapOf(
            "id"        to cat.id,
            "name"      to cat.name,
            "icon"      to cat.icon,
            "isDefault" to cat.isDefault
        )
        runCatching {
            root.collection("wallet_categories").document(cat.id.toString())
                .set(data, SetOptions.merge()).await()
        }
    }

    suspend fun deleteCategorySync(catId: Long) {
        val root = userRoot() ?: return
        runCatching {
            root.collection("wallet_categories").document(catId.toString()).delete().await()
        }
    }

    suspend fun syncBudget(budget: WalletBudget) {
        val root = userRoot() ?: return
        runCatching {
            root.collection("wallet_budget").document("budget")
                .set(mapOf("monthlyBudget" to budget.monthlyBudget), SetOptions.merge()).await()
        }
    }

    // ─── Notes ───────────────────────────────────────────────────────────────

    suspend fun syncNote(note: Note) {
        val root = userRoot() ?: return
        val data = mapOf(
            "id"             to note.id,
            "title"          to note.title,
            "content"        to note.content,
            "colorHex"       to note.colorHex,
            "isPinned"       to note.isPinned,
            "createdAt"      to note.createdAt,
            "updatedAt"      to note.updatedAt
        )
        runCatching {
            root.collection("notes").document(note.id.toString())
                .set(data, SetOptions.merge()).await()
        }
    }

    suspend fun deleteNoteSync(noteId: Long) {
        val root = userRoot() ?: return
        runCatching {
            root.collection("notes").document(noteId.toString()).delete().await()
        }
    }

    // ─── Memoirs ─────────────────────────────────────────────────────────────

    suspend fun syncMemoir(memoir: Memoir) {
        val root = userRoot() ?: return
        val data = mapOf(
            "id"          to memoir.id,
            "title"       to memoir.title,
            "content"     to memoir.content,
            "mood"        to memoir.mood,
            "dateMillis"  to memoir.dateMillis,
            "createdAt"   to memoir.createdAt
        )
        runCatching {
            root.collection("memoirs").document(memoir.id.toString())
                .set(data, SetOptions.merge()).await()
        }
    }

    suspend fun deleteMemoirSync(memoirId: Long) {
        val root = userRoot() ?: return
        runCatching {
            root.collection("memoirs").document(memoirId.toString()).delete().await()
        }
    }

    // ─── Passwords ───────────────────────────────────────────────────────────

    suspend fun syncPassword(password: Password) {
        val root = userRoot() ?: return
        val data = mapOf(
            "id"        to password.id,
            "title"     to password.title,
            "username"  to password.username,
            "password"  to password.password,
            "url"       to password.url,
            "notes"     to password.notes,
            "createdAt" to password.createdAt,
            "updatedAt" to password.updatedAt
        )
        runCatching {
            root.collection("passwords").document(password.id.toString())
                .set(data, SetOptions.merge()).await()
        }
    }

    suspend fun deletePasswordSync(passwordId: Long) {
        val root = userRoot() ?: return
        runCatching {
            root.collection("passwords").document(passwordId.toString()).delete().await()
        }
    }

    // ─── Habits ───────────────────────────────────────────────────────────────

    suspend fun syncHabit(habit: Habit) {
        val root = userRoot() ?: return
        val data = mapOf(
            "id"            to habit.id,
            "name"          to habit.name,
            "description"   to habit.description,
            "colorHex"      to habit.colorHex,
            "emoji"         to habit.emoji,
            "streak"        to habit.streak,
            "longestStreak" to habit.longestStreak,
            "lastCompletedDate" to habit.lastCompletedDate,
            "createdAt"     to habit.createdAt
        )
        runCatching {
            root.collection("habits").document(habit.id.toString())
                .set(data, SetOptions.merge()).await()
        }
    }

    suspend fun deleteHabitSync(habitId: Long) {
        val root = userRoot() ?: return
        runCatching {
            root.collection("habits").document(habitId.toString()).delete().await()
        }
    }

    // ─── Bills ─────────────────────────────────────────────────────────────────

    suspend fun syncBill(bill: Bill) {
        val root = userRoot() ?: return
        val data = mapOf(
            "id"                 to bill.id,
            "name"               to bill.name,
            "amount"             to bill.amount,
            "dueDay"             to bill.dueDay,
            "category"           to bill.category,
            "notes"              to bill.notes,
            "isPaid"             to bill.isPaid,
            "reminderEnabled"    to bill.reminderEnabled,
            "reminderDaysBefore" to bill.reminderDaysBefore,
            "nextDueDateMillis"  to bill.nextDueDateMillis,
            "createdAt"          to bill.createdAt
        )
        runCatching {
            root.collection("bills").document(bill.id.toString())
                .set(data, SetOptions.merge()).await()
        }
    }

    suspend fun deleteBillSync(billId: Long) {
        val root = userRoot() ?: return
        runCatching {
            root.collection("bills").document(billId.toString()).delete().await()
        }
    }

    // ─── Bulk push (upload local → Firestore) ────────────────────────────────

    suspend fun syncAll(
        todos: List<Todo>,
        lists: List<TodoList>,
        transactions: List<WalletTransaction>,
        categories: List<WalletCategory>,
        budget: WalletBudget?,
        notes: List<Note> = emptyList(),
        memoirs: List<Memoir> = emptyList(),
        passwords: List<Password> = emptyList(),
        habits: List<Habit> = emptyList(),
        bills: List<Bill> = emptyList()
    ) {
        todos.forEach { syncTodo(it) }
        lists.forEach { syncTodoList(it) }
        transactions.forEach { syncTransaction(it) }
        categories.forEach { syncCategory(it) }
        budget?.let { syncBudget(it) }
        notes.forEach { syncNote(it) }
        memoirs.forEach { syncMemoir(it) }
        passwords.forEach { syncPassword(it) }
        habits.forEach { syncHabit(it) }
        bills.forEach { syncBill(it) }
    }

    // ─── Restore (pull Firestore → Room on sign-in) ───────────────────────────

    suspend fun restoreFromCloud(): Boolean {
        val uid = authRepository.getCurrentUserId()
        Log.d("TaskVault", "[FirebaseSync] restoreFromCloud() uid=$uid")
        val root = userRoot() ?: run {
            Log.e("TaskVault", "[FirebaseSync] restoreFromCloud: userRoot is null")
            return false
        }
        return runCatching {
            // 1. Todo lists (no FK dependencies)
            val listsSnap = root.collection("todo_lists").get().await()
            Log.d("TaskVault", "[FirebaseSync] Fetched ${listsSnap.size()} todo lists")
            listsSnap.documents.forEach { doc ->
                val id = doc.getLong("id") ?: return@forEach
                todoListDao.insertList(
                    TodoListEntity(
                        id             = id,
                        name           = doc.getString("name") ?: "",
                        colorHex       = doc.getString("colorHex") ?: "#6750A4",
                        icon           = doc.getString("icon") ?: "📋",
                        createdAtMillis = doc.getLong("createdAtMillis") ?: System.currentTimeMillis()
                    )
                )
            }

            // 2. Wallet categories (FK parent for transactions)
            val catSnap = root.collection("wallet_categories").get().await()
            Log.d("TaskVault", "[FirebaseSync] Fetched ${catSnap.size()} wallet categories")
            catSnap.documents.forEach { doc ->
                val id = doc.getLong("id") ?: return@forEach
                if (id == 0L) { doc.reference.delete().await(); return@forEach }
                walletDao.insertCategory(
                    WalletCategoryEntity(
                        id        = id,
                        name      = doc.getString("name") ?: "",
                        icon      = doc.getString("icon") ?: "",
                        isDefault = doc.getBoolean("isDefault") ?: false
                    )
                )
            }

            // 3. Todos (FK parent for subtasks)
            val todosSnap = root.collection("todos").get().await()
            Log.d("TaskVault", "[FirebaseSync] Fetched ${todosSnap.size()} todos")
            todosSnap.documents.forEach { doc ->
                val id = doc.getLong("id") ?: return@forEach
                todoDao.insertTodo(
                    TodoEntity(
                        id              = id,
                        title           = doc.getString("title") ?: "",
                        description     = doc.getString("description") ?: "",
                        isCompleted     = doc.getBoolean("isCompleted") ?: false,
                        dueDateMillis   = doc.getLong("dueDateMillis"),
                        priority        = runCatching {
                            Priority.valueOf(doc.getString("priority") ?: "NONE")
                        }.getOrDefault(Priority.NONE),
                        reminderEnabled = doc.getBoolean("reminderEnabled") ?: false,
                        recurrenceRule  = doc.getString("recurrenceRule"),
                        listId          = doc.getLong("listId"),
                        isDeleted       = doc.getBoolean("isDeleted") ?: false,
                        deletedAtMillis = doc.getLong("deletedAtMillis"),
                        createdAtMillis = doc.getLong("createdAtMillis") ?: System.currentTimeMillis(),
                        updatedAtMillis = doc.getLong("updatedAtMillis") ?: System.currentTimeMillis()
                    )
                )
            }

            // 4. Subtasks (FK child of todos)
            val subtasksSnap = root.collection("subtasks").get().await()
            Log.d("TaskVault", "[FirebaseSync] Fetched ${subtasksSnap.size()} subtasks")
            subtasksSnap.documents.forEach { doc ->
                val id = doc.getLong("id") ?: return@forEach
                subtaskDao.insertSubtask(
                    SubtaskEntity(
                        id              = id,
                        todoId          = doc.getLong("todoId") ?: return@forEach,
                        title           = doc.getString("title") ?: "",
                        isCompleted     = doc.getBoolean("isCompleted") ?: false,
                        position        = (doc.getLong("position") ?: 0L).toInt(),
                        createdAtMillis = doc.getLong("createdAtMillis") ?: System.currentTimeMillis()
                    )
                )
            }

            // 5. Wallet transactions (FK child of categories)
            val txSnap = root.collection("wallet_transactions").get().await()
            Log.d("TaskVault", "[FirebaseSync] Fetched ${txSnap.size()} transactions")
            txSnap.documents.forEach { doc ->
                val id = doc.getLong("id") ?: return@forEach
                if (id == 0L) { doc.reference.delete().await(); return@forEach }
                walletDao.insertTransaction(
                    WalletTransactionEntity(
                        id         = id,
                        type       = runCatching {
                            TransactionType.valueOf(doc.getString("type") ?: "EXPENSE")
                        }.getOrDefault(TransactionType.EXPENSE),
                        amount     = doc.getDouble("amount") ?: 0.0,
                        categoryId = doc.getLong("categoryId"),
                        dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
                        notes      = doc.getString("notes") ?: ""
                    )
                )
            }

            // 6. Budget (single document)
            val budgetDoc = root.collection("wallet_budget").document("budget").get().await()
            if (budgetDoc.exists()) {
                walletDao.upsertBudget(
                    WalletBudgetEntity(
                        id            = 1L,
                        monthlyBudget = budgetDoc.getDouble("monthlyBudget") ?: 0.0
                    )
                )
            }

            // 7. Notes
            val notesSnap = root.collection("notes").get().await()
            Log.d("TaskVault", "[FirebaseSync] Fetched ${notesSnap.size()} notes")
            notesSnap.documents.forEach { doc ->
                val id = doc.getLong("id") ?: return@forEach
                noteDao.insert(
                    NoteEntity(
                        id       = id,
                        title    = doc.getString("title") ?: "",
                        content  = doc.getString("content") ?: "",
                        colorHex = doc.getString("colorHex") ?: "#6750A4",
                        isPinned = doc.getBoolean("isPinned") ?: false,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                    )
                )
            }

            // 8. Memoirs
            val memoirsSnap = root.collection("memoirs").get().await()
            Log.d("TaskVault", "[FirebaseSync] Fetched ${memoirsSnap.size()} memoirs")
            memoirsSnap.documents.forEach { doc ->
                val id = doc.getLong("id") ?: return@forEach
                memoirDao.insert(
                    MemoirEntity(
                        id          = id,
                        title       = doc.getString("title") ?: "",
                        content     = doc.getString("content") ?: "",
                        mood        = doc.getString("mood") ?: "😊",
                        dateMillis  = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
                        createdAt   = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                )
            }

            // 9. Passwords
            val passwordsSnap = root.collection("passwords").get().await()
            Log.d("TaskVault", "[FirebaseSync] Fetched ${passwordsSnap.size()} passwords")
            passwordsSnap.documents.forEach { doc ->
                val id = doc.getLong("id") ?: return@forEach
                passwordDao.insert(
                    PasswordEntity(
                        id        = id,
                        title     = doc.getString("title") ?: "",
                        username  = doc.getString("username") ?: "",
                        password  = doc.getString("password") ?: "",
                        url       = doc.getString("url") ?: "",
                        notes     = doc.getString("notes") ?: "",
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                    )
                )
            }

            // 10. Habits
            val habitsSnap = root.collection("habits").get().await()
            Log.d("TaskVault", "[FirebaseSync] Fetched ${habitsSnap.size()} habits")
            habitsSnap.documents.forEach { doc ->
                val id = doc.getLong("id") ?: return@forEach
                habitDao.insert(
                    HabitEntity(
                        id               = id,
                        name             = doc.getString("name") ?: "",
                        description       = doc.getString("description") ?: "",
                        colorHex         = doc.getString("colorHex") ?: "#6750A4",
                        emoji            = doc.getString("emoji") ?: "⭐",
                        streak           = doc.getLong("streak")?.toInt() ?: 0,
                        longestStreak    = doc.getLong("longestStreak")?.toInt() ?: 0,
                        lastCompletedDate = doc.getString("lastCompletedDate"),
                        createdAt        = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                )
            }

            // 11. Bills
            val billsSnap = root.collection("bills").get().await()
            Log.d("TaskVault", "[FirebaseSync] Fetched ${billsSnap.size()} bills")
            billsSnap.documents.forEach { doc ->
                val id = doc.getLong("id") ?: return@forEach
                billDao.insert(
                    BillEntity(
                        id                 = id,
                        name               = doc.getString("name") ?: "",
                        amount             = doc.getDouble("amount") ?: 0.0,
                        dueDay             = doc.getLong("dueDay")?.toInt() ?: 1,
                        category           = doc.getString("category") ?: "Other",
                        notes              = doc.getString("notes") ?: "",
                        isPaid             = doc.getBoolean("isPaid") ?: false,
                        reminderEnabled    = doc.getBoolean("reminderEnabled") ?: false,
                        reminderDaysBefore = doc.getLong("reminderDaysBefore")?.toInt() ?: 1,
                        nextDueDateMillis  = doc.getLong("nextDueDateMillis") ?: System.currentTimeMillis(),
                        createdAt          = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                )
            }

            Log.d("TaskVault", "[FirebaseSync] restoreFromCloud() SUCCESS")
            true
        }.onFailure { e ->
            Log.e("TaskVault", "[FirebaseSync] restoreFromCloud() FAILED: ${e.message}", e)
        }.getOrDefault(false)
    }

    suspend fun hasCloudData(): Boolean {
        val root = userRoot() ?: return false
        return runCatching {
            val todos = root.collection("todos").limit(1).get().await()
            !todos.isEmpty
        }.getOrDefault(false)
    }

    suspend fun deleteAllCloudData() {
        val uid = authRepository.getCurrentUserId() ?: return
        val root = firestore.collection("users").document(uid)
        Log.d("TaskVault", "[FirebaseSync] deleteAllCloudData() uid=$uid")
        val collections = listOf(
            "todos", "subtasks", "todo_lists",
            "wallet_transactions", "wallet_categories", "wallet_budget",
            "notes", "memoirs", "passwords", "habits", "bills"
        )
        for (col in collections) {
            val snap = root.collection(col).get().await()
            for (doc in snap.documents) {
                doc.reference.delete().await()
            }
            Log.d("TaskVault", "[FirebaseSync] Deleted ${snap.size()} docs from $col")
        }
        root.delete().await()
        Log.d("TaskVault", "[FirebaseSync] deleteAllCloudData() DONE")
    }
}
