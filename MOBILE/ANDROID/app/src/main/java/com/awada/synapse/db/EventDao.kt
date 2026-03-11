package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query(
        """
        SELECT * FROM EVENTS
        WHERE CONTROLLER_ID = :controllerId
        ORDER BY TIME ASC, ID ASC
        """
    )
    fun observeAllForController(controllerId: Int): Flow<List<EventEntity>>

    @Query("SELECT * FROM EVENTS WHERE ID = :id LIMIT 1")
    suspend fun getById(id: Long): EventEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: EventEntity): Long

    @Update
    suspend fun update(entity: EventEntity)

    @Query("DELETE FROM EVENTS WHERE ID = :id")
    suspend fun deleteById(id: Long)
}
