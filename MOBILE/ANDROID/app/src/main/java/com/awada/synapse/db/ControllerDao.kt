package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ControllerDao {
    @Query("SELECT * FROM CONTROLLERS ORDER BY GRID_POS ASC, ID ASC")
    fun observeAll(): Flow<List<ControllerEntity>>

    @Query("SELECT * FROM CONTROLLERS WHERE ID = :id LIMIT 1")
    suspend fun getById(id: Int): ControllerEntity?

    @Query("SELECT * FROM CONTROLLERS WHERE ID = :id LIMIT 1")
    fun observeById(id: Int): Flow<ControllerEntity?>

    @Query("SELECT * FROM CONTROLLERS WHERE NAME = :name LIMIT 1")
    suspend fun getByName(name: String): ControllerEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRaw(controller: ControllerEntity): Long

    @Transaction
    suspend fun insertAtEnd(controller: ControllerEntity): Long {
        val controllerId = insertRaw(controller.copy(gridPos = getAllOrdered().size))
        normalizeGridPositions()
        return controllerId
    }

    @Query("SELECT MAX(GRID_POS) FROM CONTROLLERS")
    suspend fun getMaxGridPos(): Int?

    @Query("UPDATE CONTROLLERS SET GRID_POS = :gridPos WHERE ID = :id")
    suspend fun setGridPosRaw(id: Int, gridPos: Int)

    @Transaction
    suspend fun replaceOrder(controllerIds: List<Int>) {
        val existingIds = getAllOrdered().map { it.id }
        check(controllerIds.size == existingIds.size) {
            "Controller order size mismatch: expected ${existingIds.size}, got ${controllerIds.size}"
        }
        check(controllerIds.toSet() == existingIds.toSet()) {
            "Controller order must include each existing controller exactly once."
        }
        applyOrderedIds(controllerIds)
    }

    @Transaction
    suspend fun moveToPosition(id: Int, gridPos: Int) {
        val orderedIds = getAllOrdered().map { it.id }.toMutableList()
        val fromIndex = orderedIds.indexOf(id)
        if (fromIndex == -1) return
        val controllerId = orderedIds.removeAt(fromIndex)
        orderedIds.add(gridPos.coerceIn(0, orderedIds.size), controllerId)
        applyOrderedIds(orderedIds)
    }

    @Transaction
    suspend fun normalizeGridPositions() {
        applyOrderedIds(getAllOrdered().map { it.id })
    }

    @Transaction
    suspend fun applyOrderedIds(controllerIds: List<Int>) {
        controllerIds.forEachIndexed { index, controllerId ->
            setGridPosRaw(controllerId, -(index + 1))
        }
        controllerIds.forEachIndexed { index, controllerId ->
            setGridPosRaw(controllerId, index)
        }
    }

    @Query("UPDATE CONTROLLERS SET SCENE_NUM = :sceneNum WHERE ID = :id")
    suspend fun setSceneNum(id: Int, sceneNum: Int)

    @Query("UPDATE CONTROLLERS SET IS_CONNECTED = 0")
    suspend fun clearConnections()

    @Query("UPDATE CONTROLLERS SET IS_CONNECTED = :isConnected WHERE ID = :id")
    suspend fun setIsConnected(id: Int, isConnected: Boolean)

    @Query("SELECT * FROM CONTROLLERS ORDER BY GRID_POS ASC, ID ASC")
    suspend fun getAllOrdered(): List<ControllerEntity>

    @Query("DELETE FROM CONTROLLERS WHERE ID = :id")
    suspend fun deleteByIdRaw(id: Int)

    @Transaction
    suspend fun deleteById(id: Int) {
        deleteByIdRaw(id)
        normalizeGridPositions()
    }

    @Query("UPDATE CONTROLLERS SET NAME = :name, ICO_NUM = :icoNum WHERE ID = :id")
    suspend fun updateNameAndIcon(id: Int, name: String, icoNum: Int)

    @Update
    suspend fun update(controller: ControllerEntity)

    @Delete
    suspend fun delete(controller: ControllerEntity)
}

