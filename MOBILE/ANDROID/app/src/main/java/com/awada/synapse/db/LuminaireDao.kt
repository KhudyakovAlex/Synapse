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
          AND GROUP_ID = :groupId
        ORDER BY GRID_POS ASC, ID ASC
        """
    )
    fun observeAllForGroup(controllerId: Int, groupId: Int): Flow<List<LuminaireEntity>>

    @Query(
        """
        SELECT * FROM LUMINAIRES
        WHERE CONTROLLER_ID = :controllerId
          AND GROUP_ID = :groupId
        ORDER BY GRID_POS ASC, ID ASC
        """
    )
    suspend fun getAllForGroup(controllerId: Int, groupId: Int): List<LuminaireEntity>

    @Query(
        """
        SELECT * FROM LUMINAIRES
        WHERE CONTROLLER_ID = :controllerId
        ORDER BY GRID_POS ASC, ID ASC
        """
    )
    fun observeAllForController(controllerId: Int): Flow<List<LuminaireEntity>>

    @Query(
        """
        SELECT * FROM LUMINAIRES
        WHERE CONTROLLER_ID = :controllerId
        ORDER BY GRID_POS ASC, ID ASC
        """
    )
    suspend fun getAllForController(controllerId: Int): List<LuminaireEntity>

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
    fun observeById(id: Long): Flow<LuminaireEntity?>

    @Query("SELECT * FROM LUMINAIRES WHERE ID = :id LIMIT 1")
    suspend fun getById(id: Long): LuminaireEntity?

    @Query("UPDATE LUMINAIRES SET NAME = :name WHERE ID = :id")
    suspend fun setName(id: Long, name: String)

    @Query("UPDATE LUMINAIRES SET ICO_NUM = :icoNum WHERE ID = :id")
    suspend fun setIcoNum(id: Long, icoNum: Int)

    @Query("UPDATE LUMINAIRES SET NAME = :name, ICO_NUM = :icoNum, TYPE_ID = :typeId WHERE ID = :id")
    suspend fun setNameIconAndType(id: Long, name: String, icoNum: Int, typeId: Int)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: LuminaireEntity): Long

    @Update
    suspend fun update(entity: LuminaireEntity)

    @Query("UPDATE LUMINAIRES SET GRID_POS = :gridPos WHERE ID = :id")
    suspend fun setGridPos(id: Long, gridPos: Int)

    @Query("UPDATE LUMINAIRES SET ROOM_ID = :roomId WHERE ID = :id")
    suspend fun moveToRoom(id: Long, roomId: Int?)

    @Query("UPDATE LUMINAIRES SET GROUP_ID = :groupId WHERE ID = :id")
    suspend fun moveToGroup(id: Long, groupId: Int?)

    @Query("UPDATE LUMINAIRES SET BRIGHT = :bright WHERE ID = :id")
    suspend fun setBright(id: Long, bright: Int)

    @Query("UPDATE LUMINAIRES SET BRIGHT = :bright WHERE CONTROLLER_ID = :controllerId")
    suspend fun setBrightForController(controllerId: Int, bright: Int)

    @Query("UPDATE LUMINAIRES SET BRIGHT = :bright WHERE CONTROLLER_ID = :controllerId AND ROOM_ID = :roomId")
    suspend fun setBrightForRoom(controllerId: Int, roomId: Int, bright: Int)

    @Query("UPDATE LUMINAIRES SET BRIGHT = :bright WHERE CONTROLLER_ID = :controllerId AND GROUP_ID = :groupId")
    suspend fun setBrightForGroup(controllerId: Int, groupId: Int, bright: Int)

    @Query("UPDATE LUMINAIRES SET HUE = :hue WHERE ID = :id")
    suspend fun setHue(id: Long, hue: Int)

    @Query("UPDATE LUMINAIRES SET HUE = :hue WHERE CONTROLLER_ID = :controllerId")
    suspend fun setHueForController(controllerId: Int, hue: Int)

    @Query("UPDATE LUMINAIRES SET HUE = :hue WHERE CONTROLLER_ID = :controllerId AND ROOM_ID = :roomId")
    suspend fun setHueForRoom(controllerId: Int, roomId: Int, hue: Int)

    @Query("UPDATE LUMINAIRES SET HUE = :hue WHERE CONTROLLER_ID = :controllerId AND GROUP_ID = :groupId")
    suspend fun setHueForGroup(controllerId: Int, groupId: Int, hue: Int)

    @Query("UPDATE LUMINAIRES SET SATURATION = :saturation WHERE ID = :id")
    suspend fun setSaturation(id: Long, saturation: Int)

    @Query("UPDATE LUMINAIRES SET SATURATION = :saturation WHERE CONTROLLER_ID = :controllerId")
    suspend fun setSaturationForController(controllerId: Int, saturation: Int)

    @Query("UPDATE LUMINAIRES SET SATURATION = :saturation WHERE CONTROLLER_ID = :controllerId AND ROOM_ID = :roomId")
    suspend fun setSaturationForRoom(controllerId: Int, roomId: Int, saturation: Int)

    @Query("UPDATE LUMINAIRES SET SATURATION = :saturation WHERE CONTROLLER_ID = :controllerId AND GROUP_ID = :groupId")
    suspend fun setSaturationForGroup(controllerId: Int, groupId: Int, saturation: Int)

    @Query("UPDATE LUMINAIRES SET TEMPERATURE = :temperature WHERE ID = :id")
    suspend fun setTemperature(id: Long, temperature: Int)

    @Query("UPDATE LUMINAIRES SET TEMPERATURE = :temperature WHERE CONTROLLER_ID = :controllerId")
    suspend fun setTemperatureForController(controllerId: Int, temperature: Int)

    @Query("UPDATE LUMINAIRES SET TEMPERATURE = :temperature WHERE CONTROLLER_ID = :controllerId AND ROOM_ID = :roomId")
    suspend fun setTemperatureForRoom(controllerId: Int, roomId: Int, temperature: Int)

    @Query("UPDATE LUMINAIRES SET TEMPERATURE = :temperature WHERE CONTROLLER_ID = :controllerId AND GROUP_ID = :groupId")
    suspend fun setTemperatureForGroup(controllerId: Int, groupId: Int, temperature: Int)

    @Query("DELETE FROM LUMINAIRES WHERE ID = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM LUMINAIRES WHERE CONTROLLER_ID = :controllerId")
    suspend fun deleteAllForController(controllerId: Int)
}

