package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "BUTTONS",
    indices = [
        Index(value = ["BUTTON_PANEL_ID"]),
        Index(value = ["BUTTON_PANEL_ID", "NUM"], unique = true),
        Index(value = ["DALI_INST"]),
        Index(value = ["BUTTON_PANEL_ID", "MATRIX_ROW", "MATRIX_COL"], unique = true),
        Index(value = ["LONG_PRESS_SCENARIO_ID"])
    ]
)
data class ButtonEntity(
    @PrimaryKey
    @ColumnInfo(name = "ID")
    val id: Int,

    @ColumnInfo(name = "NUM")
    val num: Int,

    @ColumnInfo(name = "BUTTON_PANEL_ID", defaultValue = "-1")
    val buttonPanelId: Long = UNASSIGNED_BUTTON_PANEL_ID,

    @ColumnInfo(name = "DALI_INST", defaultValue = "-1")
    val daliInst: Int = UNASSIGNED_DALI_INST,

    @ColumnInfo(name = "MATRIX_ROW", defaultValue = "0")
    val matrixRow: Int = 0,

    @ColumnInfo(name = "MATRIX_COL", defaultValue = "0")
    val matrixCol: Int = 0,

    @ColumnInfo(name = "LONG_PRESS_SCENARIO_ID")
    val longPressScenarioId: Long? = null,
) {
    companion object {
        const val MAX_ID = 511
        const val MATRIX_SIZE = 4
        const val UNASSIGNED_BUTTON_PANEL_ID = -1L
        const val UNASSIGNED_DALI_INST = -1
    }
}
