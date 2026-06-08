package com.deepseek.balance.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_records")
data class UsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val date: String,
    val month: String,
    val model: String,
    val inputTokens: Long = 0,
    val outputTokens: Long = 0,
    val totalTokens: Long = 0,
    val costAmount: Double = 0.0
)
