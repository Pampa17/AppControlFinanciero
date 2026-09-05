package com.jpdev.appcontrolfinanciero.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val category: String,
    val amount: Long,
    val date: Long // epoch day, see java.time.LocalDate.toEpochDay()
)
