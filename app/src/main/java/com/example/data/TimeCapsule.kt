package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_capsules")
data class TimeCapsule(
    @PrimaryKey
    val id: String,
    val title: String,
    val durationMonths: Int, // 3, 6, 12, 24, etc.
    val startDate: Long,
    val unlockDate: Long,
    val isStrictLock: Boolean = true,
    val isUnlocked: Boolean = false,
    val customNote: String = ""
)
