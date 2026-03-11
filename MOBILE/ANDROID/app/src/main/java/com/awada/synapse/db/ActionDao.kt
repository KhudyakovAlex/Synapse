package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionDao {
    @Query(
        """
        SELECT * FROM ACTIONS
        WHERE SCENARIO_ID = :scenarioId
        ORDER BY POSITION ASC, ID ASC
        """
    )
    fun observeAllForScenario(scenarioId: Long): Flow<List<ActionEntity>>

    @Query(
        """
        SELECT * FROM ACTIONS
        WHERE SCENARIO_ID = :scenarioId
        ORDER BY POSITION ASC, ID ASC
        """
    )
    suspend fun getAllForScenario(scenarioId: Long): List<ActionEntity>

    @Query("SELECT COALESCE(MAX(POSITION), -1) + 1 FROM ACTIONS WHERE SCENARIO_ID = :scenarioId")
    suspend fun getNextPositionForScenario(scenarioId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ActionEntity): Long

    @Update
    suspend fun update(entity: ActionEntity)

    @Query("DELETE FROM ACTIONS WHERE ID = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ACTIONS WHERE SCENARIO_ID = :scenarioId")
    suspend fun deleteAllForScenario(scenarioId: Long)
}
