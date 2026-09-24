package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM chat_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM turn_records WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getTurnsForSession(sessionId: String): Flow<List<TurnRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurn(turn: TurnRecordEntity)

    @Query("DELETE FROM turn_records WHERE sessionId = :sessionId")
    suspend fun deleteTurnsForSession(sessionId: String)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM chat_sessions")
    suspend fun clearAll()
}
