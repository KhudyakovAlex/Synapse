package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ButtonDao {
    @Query(
        """
        SELECT * FROM BUTTONS
        WHERE BUTTON_PANEL_ID = :buttonPanelId
        ORDER BY NUM ASC, ID ASC
        """
    )
    fun observeAllForPanel(buttonPanelId: Long): Flow<List<ButtonEntity>>

    @Query(
        """
        SELECT * FROM BUTTONS
        WHERE BUTTON_PANEL_ID = :buttonPanelId
        ORDER BY NUM ASC, ID ASC
        """
    )
    suspend fun getAllForPanel(buttonPanelId: Long): List<ButtonEntity>

    @Query("SELECT COALESCE(MAX(ID), -1) + 1 FROM BUTTONS")
    suspend fun getNextId(): Int

    @Query(
        """
        UPDATE BUTTONS
        SET MATRIX_ROW = :matrixRow,
            MATRIX_COL = :matrixCol
        WHERE ID = :id
        """
    )
    suspend fun setMatrixPosition(id: Int, matrixRow: Int, matrixCol: Int)

    @Query(
        """
        UPDATE BUTTONS
        SET MATRIX_ROW = CASE
                WHEN ID = :firstId THEN :secondRow
                WHEN ID = :secondId THEN :firstRow
                ELSE MATRIX_ROW
            END,
            MATRIX_COL = CASE
                WHEN ID = :firstId THEN :secondCol
                WHEN ID = :secondId THEN :firstCol
                ELSE MATRIX_COL
            END
        WHERE ID IN (:firstId, :secondId)
        """
    )
    suspend fun swapMatrixPositions(
        firstId: Int,
        firstRow: Int,
        firstCol: Int,
        secondId: Int,
        secondRow: Int,
        secondCol: Int,
    )

    @Transaction
    suspend fun moveToOccupiedMatrixPosition(
        draggedId: Int,
        targetId: Int,
        targetRow: Int,
        targetCol: Int,
        emptyRow: Int,
        emptyCol: Int,
    ) {
        // Use a temporary off-grid slot to avoid violating the unique matrix-position index
        // when the nearest empty slot is the dragged button's original position.
        setMatrixPosition(
            id = draggedId,
            matrixRow = -1,
            matrixCol = -1,
        )
        setMatrixPosition(
            id = targetId,
            matrixRow = emptyRow,
            matrixCol = emptyCol,
        )
        setMatrixPosition(
            id = draggedId,
            matrixRow = targetRow,
            matrixCol = targetCol,
        )
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ButtonEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<ButtonEntity>)

    @Query("DELETE FROM BUTTONS WHERE BUTTON_PANEL_ID = :buttonPanelId")
    suspend fun deleteAllForPanel(buttonPanelId: Long)

    @Query(
        """
        DELETE FROM BUTTONS
        WHERE BUTTON_PANEL_ID IN (
            SELECT ID FROM BUTTON_PANELS WHERE CONTROLLER_ID = :controllerId
        )
        """
    )
    suspend fun deleteAllForController(controllerId: Int)
}
