package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationRecord>>

    @Query("SELECT * FROM calculation_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteHistory(): Flow<List<CalculationRecord>>

    @Query("SELECT * FROM calculation_history WHERE expression LIKE '%' || :query || '%' OR result LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<CalculationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: CalculationRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<CalculationRecord>)

    @Update
    suspend fun update(record: CalculationRecord)

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()

    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    suspend fun getAllHistoryList(): List<CalculationRecord>
}
