package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScenarioDao {
    @Query("SELECT * FROM SCENARIOS WHERE ID = :id LIMIT 1")
    fun observeById(id: Long): Flow<ScenarioEntity?>

    @Query("SELECT * FROM SCENARIOS WHERE ID = :id LIMIT 1")
    suspend fun getById(id: Long): ScenarioEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ScenarioEntity): Long

    @Query("DELETE FROM SCENARIOS WHERE ID = :id")
    suspend fun deleteById(id: Long)
}
