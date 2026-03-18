package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BrightSensorDao {
    @Query(
        """
        SELECT * FROM BRIGHT_SENSORS
        WHERE CONTROLLER_ID = :controllerId
          AND ((:roomId IS NULL AND ROOM_ID IS NULL) OR ROOM_ID = :roomId)
        ORDER BY GRID_POS ASC, ID ASC
        """
    )
    fun observeAll(controllerId: Int, roomId: Int?): Flow<List<BrightSensorEntity>>

    @Query("SELECT COUNT(*) FROM BRIGHT_SENSORS WHERE CONTROLLER_ID = :controllerId")
    fun observeCountForController(controllerId: Int): Flow<Int>

    @Query(
        """
        SELECT * FROM BRIGHT_SENSORS
        WHERE CONTROLLER_ID = :controllerId
          AND GROUP_ID = :groupId
        ORDER BY GRID_POS ASC, ID ASC
        """
    )
    fun observeAllForGroup(controllerId: Int, groupId: Int): Flow<List<BrightSensorEntity>>

    @Query(
        """
        SELECT * FROM BRIGHT_SENSORS
        WHERE CONTROLLER_ID = :controllerId
          AND GROUP_ID = :groupId
        ORDER BY GRID_POS ASC, ID ASC
        """
    )
    suspend fun getAllForGroup(controllerId: Int, groupId: Int): List<BrightSensorEntity>

    @Query(
        """
        SELECT * FROM BRIGHT_SENSORS
        WHERE CONTROLLER_ID = :controllerId
          AND ((:roomId IS NULL AND ROOM_ID IS NULL) OR ROOM_ID = :roomId)
        ORDER BY GRID_POS ASC, ID ASC
        """
    )
    suspend fun getAllOrdered(controllerId: Int, roomId: Int?): List<BrightSensorEntity>

    @Query(
        """
        SELECT * FROM BRIGHT_SENSORS
        WHERE CONTROLLER_ID = :controllerId
        ORDER BY GRID_POS ASC, ID ASC
        """
    )
    suspend fun getAllForController(controllerId: Int): List<BrightSensorEntity>

    @Query("SELECT * FROM BRIGHT_SENSORS WHERE ID = :id LIMIT 1")
    suspend fun getById(id: Long): BrightSensorEntity?

    @Query("UPDATE BRIGHT_SENSORS SET NAME = :name WHERE ID = :id")
    suspend fun setName(id: Long, name: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: BrightSensorEntity): Long

    @Update
    suspend fun update(entity: BrightSensorEntity)

    @Query("UPDATE BRIGHT_SENSORS SET GRID_POS = :gridPos WHERE ID = :id")
    suspend fun setGridPos(id: Long, gridPos: Int)

    @Query("UPDATE BRIGHT_SENSORS SET ROOM_ID = :roomId WHERE ID = :id")
    suspend fun moveToRoom(id: Long, roomId: Int?)

    @Query("UPDATE BRIGHT_SENSORS SET GROUP_ID = :groupId WHERE ID = :id")
    suspend fun moveToGroup(id: Long, groupId: Int?)

    @Query("DELETE FROM BRIGHT_SENSORS WHERE ID = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM BRIGHT_SENSORS WHERE CONTROLLER_ID = :controllerId")
    suspend fun deleteAllForController(controllerId: Int)
}

