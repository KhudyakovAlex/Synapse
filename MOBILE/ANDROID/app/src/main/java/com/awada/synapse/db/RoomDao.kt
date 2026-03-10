package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM ROOMS WHERE CONTROLLER_ID = :controllerId ORDER BY GRID_POS ASC, ID ASC")
    fun observeAll(controllerId: Int): Flow<List<RoomEntity>>

    @Query("SELECT * FROM ROOMS WHERE CONTROLLER_ID = :controllerId ORDER BY GRID_POS ASC, ID ASC")
    suspend fun getAllOrdered(controllerId: Int): List<RoomEntity>

    @Query("SELECT * FROM ROOMS WHERE CONTROLLER_ID = :controllerId AND ID = :id LIMIT 1")
    suspend fun getById(controllerId: Int, id: Int): RoomEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(room: RoomEntity)

    @Update
    suspend fun update(room: RoomEntity)

    @Query("UPDATE ROOMS SET GRID_POS = :gridPos WHERE CONTROLLER_ID = :controllerId AND ID = :id")
    suspend fun setGridPos(controllerId: Int, id: Int, gridPos: Int)

    @Query("UPDATE ROOMS SET SCENE_NUM = :sceneNum WHERE CONTROLLER_ID = :controllerId AND ID = :id")
    suspend fun setSceneNum(controllerId: Int, id: Int, sceneNum: Int)

    @Query("DELETE FROM ROOMS WHERE CONTROLLER_ID = :controllerId AND ID = :id")
    suspend fun deleteById(controllerId: Int, id: Int)

    @Query("DELETE FROM ROOMS WHERE CONTROLLER_ID = :controllerId")
    suspend fun deleteAllForController(controllerId: Int)
}

