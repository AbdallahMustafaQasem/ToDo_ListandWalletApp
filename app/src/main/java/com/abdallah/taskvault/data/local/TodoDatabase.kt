package com.abdallah.taskvault.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.abdallah.taskvault.data.local.converter.PriorityConverter
import com.abdallah.taskvault.data.local.converter.TransactionTypeConverter

@Database(
    entities = [
        TodoEntity::class,
        WalletTransactionEntity::class,
        WalletCategoryEntity::class,
        WalletBudgetEntity::class,
        SubtaskEntity::class,
        TodoListEntity::class,
        NoteEntity::class,
        MemoirEntity::class,
        PasswordEntity::class,
        HabitEntity::class,
        BillEntity::class,
        ContactEntity::class,
        TagEntity::class,
        TodoTagCrossRef::class,
        CommentEntity::class
    ],
    version = 11,
    exportSchema = true
)
@TypeConverters(PriorityConverter::class, TransactionTypeConverter::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun walletDao(): WalletDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun todoListDao(): TodoListDao
    abstract fun noteDao(): NoteDao
    abstract fun memoirDao(): MemoirDao
    abstract fun passwordDao(): PasswordDao
    abstract fun habitDao(): HabitDao
    abstract fun billDao(): BillDao
    abstract fun contactDao(): ContactDao
    abstract fun tagDao(): TagDao
    abstract fun commentDao(): CommentDao

    companion object {
        const val DATABASE_NAME = "todo_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE todos ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE todos ADD COLUMN deleted_at_millis INTEGER")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS wallet_categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        is_default INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS wallet_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        amount REAL NOT NULL,
                        category_id INTEGER,
                        date_millis INTEGER NOT NULL,
                        notes TEXT NOT NULL,
                        FOREIGN KEY(category_id) REFERENCES wallet_categories(id) ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_wallet_transactions_category_id ON wallet_transactions(category_id)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS wallet_budget (
                        id INTEGER PRIMARY KEY NOT NULL,
                        monthly_budget REAL NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS comments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        todo_id INTEGER NOT NULL,
                        author_name TEXT NOT NULL DEFAULT '',
                        text TEXT NOT NULL,
                        timestamp_millis INTEGER NOT NULL,
                        FOREIGN KEY(todo_id) REFERENCES todos(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_comments_todo_id ON comments(todo_id)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tags (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        color_hex TEXT NOT NULL DEFAULT '#6750A4'
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS todo_tags (
                        todo_id INTEGER NOT NULL,
                        tag_id INTEGER NOT NULL,
                        PRIMARY KEY(todo_id, tag_id),
                        FOREIGN KEY(todo_id) REFERENCES todos(id) ON DELETE CASCADE,
                        FOREIGN KEY(tag_id) REFERENCES tags(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_todo_tags_todo_id ON todo_tags(todo_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_todo_tags_tag_id ON todo_tags(tag_id)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS contacts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        user_id TEXT NOT NULL,
                        display_name TEXT NOT NULL,
                        role TEXT NOT NULL DEFAULT '',
                        avatar_color TEXT NOT NULL DEFAULT '#6750A4',
                        added_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_contacts_user_id ON contacts(user_id)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bills (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        due_day INTEGER NOT NULL,
                        category TEXT NOT NULL DEFAULT 'Other',
                        notes TEXT NOT NULL DEFAULT '',
                        is_paid INTEGER NOT NULL DEFAULT 0,
                        reminder_enabled INTEGER NOT NULL DEFAULT 0,
                        reminder_days_before INTEGER NOT NULL DEFAULT 1,
                        next_due_date_millis INTEGER NOT NULL,
                        created_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS habits (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        color_hex TEXT NOT NULL DEFAULT '#6750A4',
                        emoji TEXT NOT NULL DEFAULT '⭐',
                        streak INTEGER NOT NULL DEFAULT 0,
                        longest_streak INTEGER NOT NULL DEFAULT 0,
                        last_completed_date TEXT,
                        created_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS passwords (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        username TEXT NOT NULL DEFAULT '',
                        password TEXT NOT NULL,
                        url TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        created_at_millis INTEGER NOT NULL,
                        updated_at_millis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        color_hex TEXT NOT NULL DEFAULT '#6750A4',
                        is_pinned INTEGER NOT NULL DEFAULT 0,
                        created_at_millis INTEGER NOT NULL,
                        updated_at_millis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS memoirs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        mood TEXT NOT NULL DEFAULT '😊',
                        date_millis INTEGER NOT NULL,
                        created_at_millis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE todos ADD COLUMN recurrence_rule TEXT")
                db.execSQL("ALTER TABLE todos ADD COLUMN list_id INTEGER")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS todo_lists (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        color_hex TEXT NOT NULL DEFAULT '#6750A4',
                        icon TEXT NOT NULL DEFAULT '📋',
                        created_at_millis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS subtasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        todo_id INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        is_completed INTEGER NOT NULL DEFAULT 0,
                        position INTEGER NOT NULL DEFAULT 0,
                        created_at_millis INTEGER NOT NULL,
                        FOREIGN KEY(todo_id) REFERENCES todos(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_todo_id ON subtasks(todo_id)")
            }
        }
    }
}
