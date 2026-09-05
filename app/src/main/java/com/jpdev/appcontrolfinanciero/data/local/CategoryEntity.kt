package com.jpdev.appcontrolfinanciero.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // domain.EntryType name: "INGRESO" | "EGRESO"
    val isReserved: Boolean = false // the "Sin categoría" fallback per type — never edited/deleted
)
