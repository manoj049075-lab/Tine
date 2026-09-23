package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val currentCount: Int,
    val isUnlocked: Boolean = false,
    val unlockedDate: Long? = null,
    val category: String = "STREAK" // STREAK, FRAMES, CAPSULE, NOTE
)
