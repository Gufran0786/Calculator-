package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val category: String = "Scientific", // "Scientific", "Basic", "Voice", "Constant", "Graph"
    val note: String = ""
)
