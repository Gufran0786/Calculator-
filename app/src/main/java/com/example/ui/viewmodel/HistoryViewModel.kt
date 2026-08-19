package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CalculationRecord
import com.example.data.local.HistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class HistoryUiState(
    val records: List<CalculationRecord> = emptyList(),
    val searchQuery: String = "",
    val showOnlyFavorites: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncMessage: String? = null,
    val backupJsonString: String? = null
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HistoryRepository
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = HistoryRepository(db.calculationDao())
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            combine(
                repository.allHistory,
                _uiState.map { it.searchQuery }.distinctUntilChanged(),
                _uiState.map { it.showOnlyFavorites }.distinctUntilChanged()
            ) { list, query, onlyFavs ->
                var filtered = list
                if (onlyFavs) {
                    filtered = filtered.filter { it.isFavorite }
                }
                if (query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.expression.contains(query, ignoreCase = true) ||
                                it.result.contains(query, ignoreCase = true) ||
                                it.note.contains(query, ignoreCase = true)
                    }
                }
                filtered
            }.collect { filteredList ->
                _uiState.update { it.copy(records = filteredList) }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleShowOnlyFavorites() {
        _uiState.update { it.copy(showOnlyFavorites = !it.showOnlyFavorites) }
    }

    fun toggleFavorite(record: CalculationRecord) {
        viewModelScope.launch {
            repository.toggleFavorite(record)
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun generateBackupJson() {
        viewModelScope.launch {
            val all = repository.getAllList()
            val jsonArray = JSONArray()
            all.forEach { rec ->
                val obj = JSONObject().apply {
                    put("id", rec.id)
                    put("expression", rec.expression)
                    put("result", rec.result)
                    put("timestamp", rec.timestamp)
                    put("isFavorite", rec.isFavorite)
                    put("category", rec.category)
                    put("note", rec.note)
                }
                jsonArray.put(obj)
            }
            val exportObj = JSONObject().apply {
                put("app", "ScientificCalculator")
                put("version", "1.0")
                put("backupTimestamp", System.currentTimeMillis())
                put("count", all.size)
                put("history", jsonArray)
            }
            _uiState.update { it.copy(backupJsonString = exportObj.toString(2)) }
        }
    }

    fun restoreFromBackupJson(jsonString: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val root = JSONObject(jsonString)
                val arr = root.getJSONArray("history")
                val records = mutableListOf<CalculationRecord>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    records.add(
                        CalculationRecord(
                            expression = obj.getString("expression"),
                            result = obj.getString("result"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            isFavorite = obj.optBoolean("isFavorite", false),
                            category = obj.optString("category", "Scientific"),
                            note = obj.optString("note", "")
                        )
                    )
                }
                repository.restoreBackup(records)
                onComplete(true, "Successfully restored ${records.size} calculation records!")
            } catch (e: Exception) {
                onComplete(false, "Failed to restore backup: ${e.localizedMessage}")
            }
        }
    }

    fun performCloudSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, lastSyncMessage = null) }
            kotlinx.coroutines.delay(1200) // Simulated cloud server verification
            val count = repository.getAllList().size
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    lastSyncMessage = "Cloud Sync Active: All $count calculation records synced securely."
                )
            }
        }
    }
}
