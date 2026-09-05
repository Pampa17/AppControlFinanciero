package com.jpdev.appcontrolfinanciero.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert
    suspend fun insert(income: IncomeEntity): Long

    @Query("SELECT * FROM incomes WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun observeByDateRange(start: Long, end: Long): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE date BETWEEN :start AND :end")
    suspend fun getByDateRange(start: Long, end: Long): List<IncomeEntity>

    @Query("SELECT COUNT(*) FROM incomes WHERE categoryId = :categoryId")
    suspend fun countByCategory(categoryId: Long): Int

    @Query("UPDATE incomes SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long)
}
