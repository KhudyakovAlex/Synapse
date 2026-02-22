package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AiMessageDao {
    @Query("SELECT * FROM AI_MESSAGES ORDER BY CREATED_AT ASC, ID ASC")
    fun observeAll(): Flow<List<AiMessageEntity>>

    @Query("SELECT COUNT(*) FROM AI_MESSAGES")
    suspend fun count(): Int

    @Query("SELECT * FROM AI_MESSAGES ORDER BY CREATED_AT DESC, ID DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<AiMessageEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(message: AiMessageEntity): Long

    @Query("DELETE FROM AI_MESSAGES")
    suspend fun clear()
}

