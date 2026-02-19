package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ControllerDao {
    @Query("SELECT * FROM CONTROLLERS ORDER BY ID ASC")
    fun observeAll(): Flow<List<ControllerEntity>>

    @Query("SELECT * FROM CONTROLLERS WHERE ID = :id LIMIT 1")
    suspend fun getById(id: Int): ControllerEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(controller: ControllerEntity): Long

    @Update
    suspend fun update(controller: ControllerEntity)

    @Delete
    suspend fun delete(controller: ControllerEntity)
}

