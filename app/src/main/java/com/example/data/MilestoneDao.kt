package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones ORDER BY isUnlocked DESC, targetCount ASC")
    fun getAllMilestones(): Flow<List<Milestone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<Milestone>)

    @Update
    suspend fun updateMilestone(milestone: Milestone)

    @Query("UPDATE milestones SET isUnlocked = :unlocked, unlockedDate = :date, currentCount = :count WHERE id = :id")
    suspend fun setMilestoneStatus(id: String, unlocked: Boolean, date: Long?, count: Int)
}
