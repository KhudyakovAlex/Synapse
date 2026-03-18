package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GraphDao {
    @Query(
        """
        SELECT * FROM GRAPHS
        WHERE CONTROLLER_ID = :controllerId
        ORDER BY ID ASC
        """
    )
    fun observeAllForController(controllerId: Int): Flow<List<GraphEntity>>

    @Query(
        """
        SELECT * FROM GRAPHS
        WHERE ID = :id
        LIMIT 1
        """
    )
    suspend fun getById(id: Long): GraphEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: GraphEntity): Long

    @Update
    suspend fun update(entity: GraphEntity)

    @Query("DELETE FROM GRAPHS WHERE ID = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM GRAPHS WHERE CONTROLLER_ID = :controllerId")
    suspend fun deleteAllForController(controllerId: Int)
}
