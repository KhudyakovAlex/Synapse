package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GraphPointDao {
    @Query(
        """
        SELECT * FROM GRAPH_POINTS
        WHERE GRAPH_ID = :graphId
        ORDER BY TIME ASC, ID ASC
        """
    )
    fun observeAllForGraph(graphId: Long): Flow<List<GraphPointEntity>>

    @Query(
        """
        SELECT * FROM GRAPH_POINTS
        WHERE GRAPH_ID IN (:graphIds)
        ORDER BY GRAPH_ID ASC, TIME ASC, ID ASC
        """
    )
    fun observeAllForGraphs(graphIds: List<Long>): Flow<List<GraphPointEntity>>

    @Query(
        """
        SELECT * FROM GRAPH_POINTS
        WHERE GRAPH_ID = :graphId
        ORDER BY TIME ASC, ID ASC
        """
    )
    suspend fun getAllForGraph(graphId: Long): List<GraphPointEntity>

    @Query("SELECT * FROM GRAPH_POINTS WHERE ID = :id LIMIT 1")
    suspend fun getById(id: Long): GraphPointEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: GraphPointEntity): Long

    @Update
    suspend fun update(entity: GraphPointEntity)

    @Query("DELETE FROM GRAPH_POINTS WHERE ID = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM GRAPH_POINTS WHERE GRAPH_ID = :graphId")
    suspend fun deleteAllForGraph(graphId: Long)
}
