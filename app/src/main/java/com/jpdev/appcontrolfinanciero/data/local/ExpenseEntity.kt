package com.jpdev.appcontrolfinanciero.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val categoryId: Long,
    val amount: Long,
    val date: Long // epoch day, see java.time.LocalDate.toEpochDay()
)
