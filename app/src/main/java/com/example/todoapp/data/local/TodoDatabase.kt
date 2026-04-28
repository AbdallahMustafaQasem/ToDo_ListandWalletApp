package com.example.todoapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.todoapp.data.local.converter.PriorityConverter
import com.example.todoapp.data.local.converter.TransactionTypeConverter

@Database(
    entities = [
        TodoEntity::class,
        WalletTransactionEntity::class,
        WalletCategoryEntity::class,
        WalletBudgetEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(PriorityConverter::class, TransactionTypeConverter::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun walletDao(): WalletDao

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
    }
}
