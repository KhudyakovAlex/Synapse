package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LuminaireDao {
    @Query(
        """
        SELECT * FROM LUMINAIRES
        WHERE CONTROLLER_ID = :controllerId
          AND ((:roomId IS NULL AND ROOM_ID IS NULL) OR ROOM_ID = :roomId)
        ORDER BY GRID_POS ASC, ID ASC
        """
    )
    fun observeAll(controllerId: Int, roomId: Int?): Flow<List<LuminaireEntity>>

    @Query(
        """
        SELECT * FROM LUMINAIRES
        WHERE CONTROLLER_ID = :controllerId
          AND ((:roomId IS NULL AND ROOM_ID IS NULL) OR ROOM_ID = :roomId)
        ORDER BY GRID_POS ASC, ID ASC
        """
    )
    suspend fun getAllOrdered(controllerId: Int, roomId: Int?): List<LuminaireEntity>

    @Query("SELECT * FROM LUMINAIRES WHERE ID = :id LIMIT 1")
    suspend fun getById(id: Long): LuminaireEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: LuminaireEntity): Long

    @Update
    suspend fun update(entity: LuminaireEntity)

    @Query("UPDATE LUMINAIRES SET GRID_POS = :gridPos WHERE ID = :id")
    suspend fun setGridPos(id: Long, gridPos: Int)

    @Query("UPDATE LUMINAIRES SET ROOM_ID = :roomId WHERE ID = :id")
    suspend fun moveToRoom(id: Long, roomId: Int?)

    @Query("DELETE FROM LUMINAIRES WHERE ID = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM LUMINAIRES WHERE CONTROLLER_ID = :controllerId")
    suspend fun deleteAllForController(controllerId: Int)
}

