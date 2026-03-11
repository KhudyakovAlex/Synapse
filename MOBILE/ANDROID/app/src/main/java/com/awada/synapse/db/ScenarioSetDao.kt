package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScenarioSetDao {
    @Query(
        """
        SELECT * FROM SCENARIO_SET
        WHERE BUTTON_ID = :buttonId
        ORDER BY POSITION ASC, ID ASC
        """
    )
    fun observeAllForButton(buttonId: Int): Flow<List<ScenarioSetEntity>>

    @Query(
        """
        SELECT * FROM SCENARIO_SET
        WHERE BUTTON_ID = :buttonId
        ORDER BY POSITION ASC, ID ASC
        """
    )
    suspend fun getAllForButton(buttonId: Int): List<ScenarioSetEntity>

    @Query("SELECT COALESCE(MAX(POSITION), -1) + 1 FROM SCENARIO_SET WHERE BUTTON_ID = :buttonId")
    suspend fun getNextPositionForButton(buttonId: Int): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ScenarioSetEntity): Long

    @Update
    suspend fun update(entity: ScenarioSetEntity)

    @Query("DELETE FROM SCENARIO_SET WHERE ID = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM SCENARIO_SET WHERE BUTTON_ID = :buttonId")
    suspend fun deleteAllForButton(buttonId: Int)
}
