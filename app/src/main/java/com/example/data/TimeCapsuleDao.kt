package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeCapsuleDao {
    @Query("SELECT * FROM time_capsules ORDER BY unlockDate ASC")
    fun getAllCapsules(): Flow<List<TimeCapsule>>

    @Query("SELECT * FROM time_capsules WHERE id = :id LIMIT 1")
    suspend fun getCapsuleById(id: String): TimeCapsule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapsule(capsule: TimeCapsule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapsules(capsules: List<TimeCapsule>)

    @Update
    suspend fun updateCapsule(capsule: TimeCapsule)
}
