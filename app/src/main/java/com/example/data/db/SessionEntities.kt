package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val modelId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "turn_records")
data class TurnRecordEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val userPrompt: String,
    val attachmentsJson: String = "[]",
    val stepGroupsJson: String = "[]",
    val finalTitle: String = "",
    val finalResponseText: String = "",
    val deliverablesJson: String = "[]",
    val turnSummaryJson: String = "{}",
    val status: String = "COMPLETED",
    val timestamp: Long = System.currentTimeMillis()
)
