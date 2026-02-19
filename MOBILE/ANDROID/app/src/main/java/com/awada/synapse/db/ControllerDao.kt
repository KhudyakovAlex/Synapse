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
    @Query("SELECT * FROM CONTROLLERS ORDER BY GRID_POS ASC, ID ASC")
    fun observeAll(): Flow<List<ControllerEntity>>

    @Query("SELECT * FROM CONTROLLERS WHERE ID = :id LIMIT 1")
    suspend fun getById(id: Int): ControllerEntity?

    @Query("SELECT * FROM CONTROLLERS WHERE NAME = :name LIMIT 1")
    suspend fun getByName(name: String): ControllerEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(controller: ControllerEntity): Long

    @Query("SELECT MAX(GRID_POS) FROM CONTROLLERS")
    suspend fun getMaxGridPos(): Int?

    @Query("UPDATE CONTROLLERS SET GRID_POS = :gridPos WHERE ID = :id")
    suspend fun setGridPos(id: Int, gridPos: Int)

    @Query("SELECT * FROM CONTROLLERS ORDER BY GRID_POS ASC, ID ASC")
    suspend fun getAllOrdered(): List<ControllerEntity>

    @Query("DELETE FROM CONTROLLERS WHERE ID = :id")
    suspend fun deleteById(id: Int)

    @Update
    suspend fun update(controller: ControllerEntity)

    @Delete
    suspend fun delete(controller: ControllerEntity)
}

