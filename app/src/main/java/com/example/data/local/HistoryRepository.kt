package com.example.data.local

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: CalculationDao) {
    val allHistory: Flow<List<CalculationRecord>> = dao.getAllHistory()
    val favoriteHistory: Flow<List<CalculationRecord>> = dao.getFavoriteHistory()

    fun search(query: String): Flow<List<CalculationRecord>> = dao.searchHistory(query)

    suspend fun insert(expression: String, result: String, category: String = "Scientific", note: String = ""): Long {
        if (expression.isBlank() || result.isBlank()) return -1
        val record = CalculationRecord(
            expression = expression.trim(),
            result = result.trim(),
            category = category,
            note = note
        )
        return dao.insert(record)
    }

    suspend fun toggleFavorite(record: CalculationRecord) {
        dao.update(record.copy(isFavorite = !record.isFavorite))
    }

    suspend fun delete(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    suspend fun getAllList(): List<CalculationRecord> {
        return dao.getAllHistoryList()
    }

    suspend fun restoreBackup(records: List<CalculationRecord>) {
        dao.insertAll(records)
    }
}
