package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CONTROLLERS")
data class ControllerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Int = 0,

    @ColumnInfo(name = "NAME", defaultValue = "''")
    val name: String = "",

    @ColumnInfo(name = "PASSWORD", defaultValue = "''")
    val password: String = "",

    @ColumnInfo(name = "IS_SCHEDULE", defaultValue = "0")
    val isSchedule: Int = 0,

    @ColumnInfo(name = "IS_AUTO", defaultValue = "0")
    val isAuto: Int = 0,

    @ColumnInfo(name = "ICO_NUM", defaultValue = "100")
    val icoNum: Int = 100,

    @ColumnInfo(name = "STATUS", defaultValue = "'A'")
    val status: String = "A",

    @ColumnInfo(name = "SCENE_NUM", defaultValue = "-1")
    val sceneNum: Int = -1,

    @ColumnInfo(name = "TIMESTAMP", defaultValue = "0")
    val timestamp: Long = 0L,
    @ColumnInfo(name = "GRID_POS", defaultValue = "0")
    val gridPos: Int = 0
)

