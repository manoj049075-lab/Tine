package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEntryDao {
    @Query("SELECT * FROM daily_entries WHERE isOfficial = 1 ORDER BY dateString ASC")
    fun getAllOfficialEntries(): Flow<List<DailyEntry>>

    @Query("SELECT * FROM daily_entries WHERE dateString = :dateString LIMIT 1")
    suspend fun getEntryByDate(dateString: String): DailyEntry?

    @Query("SELECT * FROM daily_entries WHERE dateString = :dateString LIMIT 1")
    fun getEntryFlowByDate(dateString: String): Flow<DailyEntry?>

    @Query("SELECT * FROM daily_entries WHERE isOfficial = 1 ORDER BY timestamp DESC LIMIT 1")
    fun getLatestEntry(): Flow<DailyEntry?>

    @Query("SELECT * FROM daily_entries WHERE isOfficial = 1 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEntrySync(): DailyEntry?

    @Query("SELECT COUNT(*) FROM daily_entries WHERE isOfficial = 1")
    fun getEntryCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DailyEntry)

    @Update
    suspend fun updateEntry(entry: DailyEntry)

    @Query("DELETE FROM daily_entries WHERE id = :id")
    suspend fun deleteEntry(id: String)
}
