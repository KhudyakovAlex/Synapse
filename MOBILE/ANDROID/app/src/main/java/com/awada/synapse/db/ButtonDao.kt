package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ButtonDao {
    @Query(
        """
        SELECT * FROM BUTTONS
        WHERE BUTTON_PANEL_ID = :buttonPanelId
        ORDER BY NUM ASC, ID ASC
        """
    )
    abstract fun observeAllForPanel(buttonPanelId: Long): Flow<List<ButtonEntity>>

    @Query(
        """
        SELECT * FROM BUTTONS
        WHERE BUTTON_PANEL_ID = :buttonPanelId
        ORDER BY NUM ASC, ID ASC
        """
    )
    abstract suspend fun getAllForPanel(buttonPanelId: Long): List<ButtonEntity>

    @Query("SELECT COALESCE(MAX(ID), -1) + 1 FROM BUTTONS")
    abstract suspend fun getNextId(): Int

    @Query(
        """
        UPDATE BUTTONS
        SET MATRIX_ROW = :matrixRow,
            MATRIX_COL = :matrixCol
        WHERE ID = :id
        """
    )
    abstract suspend fun setMatrixPosition(id: Int, matrixRow: Int, matrixCol: Int)

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
    abstract suspend fun swapMatrixPositions(
        firstId: Int,
        firstRow: Int,
        firstCol: Int,
        secondId: Int,
        secondRow: Int,
        secondCol: Int,
    )

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insert(entity: ButtonEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertAll(entities: List<ButtonEntity>)

    @Query("DELETE FROM BUTTONS WHERE BUTTON_PANEL_ID = :buttonPanelId")
    abstract suspend fun deleteAllForPanel(buttonPanelId: Long)

    @Query(
        """
        DELETE FROM BUTTONS
        WHERE BUTTON_PANEL_ID IN (
            SELECT ID FROM BUTTON_PANELS WHERE CONTROLLER_ID = :controllerId
        )
        """
    )
    abstract suspend fun deleteAllForController(controllerId: Int)
}
