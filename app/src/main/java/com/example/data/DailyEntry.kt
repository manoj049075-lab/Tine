package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_entries",
    indices = [
        Index(value = ["dateString"], unique = true),
        Index(value = ["timestamp"])
    ]
)
data class DailyEntry(
    @PrimaryKey
    val id: String,
    val dateString: String, // e.g. "2026-09-23"
    val timestamp: Long, // Epoch millis
    val imagePath: String, // Path in app internal storage or asset
    val thumbnailPath: String? = null,
    val alignmentScore: Int = 95, // 0 - 100 percentage
    val note: String = "", // Max 200 chars
    val isOfficial: Boolean = true,
    val isImported: Boolean = false,
    val syncState: String = "LOCAL", // LOCAL, SYNCED, PENDING
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
