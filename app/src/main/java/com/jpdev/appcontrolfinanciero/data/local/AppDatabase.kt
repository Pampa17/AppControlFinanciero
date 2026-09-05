package com.jpdev.appcontrolfinanciero.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jpdev.appcontrolfinanciero.domain.CategorySeeds
import com.jpdev.appcontrolfinanciero.domain.EntryType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [IncomeEntity::class, ExpenseEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: run {
                    // ponytail: still pre-release (exam project, no shipped users) — destructive
                    // migration is fine here instead of writing a real Migration for the schema
                    // change (category: String -> categoryId: Long FK).
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_control_financiero.db"
                    )
                        .fallbackToDestructiveMigration(true)
                        .build()
                    instance = db
                    seedDefaultCategories(db)
                    db
                }
            }

        private fun seedDefaultCategories(db: AppDatabase) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = db.categoryDao()
                seedType(dao, EntryType.EGRESO, CategorySeeds.EXPENSE)
                seedType(dao, EntryType.INGRESO, CategorySeeds.INCOME)
            }
        }

        private suspend fun seedType(dao: CategoryDao, type: EntryType, seeds: List<CategorySeeds.Seed>) {
            if (dao.getByType(type.name).isNotEmpty()) return
            seeds.forEach { seed -> dao.insert(CategoryEntity(name = seed.name, type = type.name)) }
            dao.insert(CategoryEntity(name = CategorySeeds.RESERVED_NAME, type = type.name, isReserved = true))
        }
    }
}
