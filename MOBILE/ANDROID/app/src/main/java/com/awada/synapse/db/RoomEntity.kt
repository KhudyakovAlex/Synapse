package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "ROOMS",
    primaryKeys = ["CONTROLLER_ID", "ID"],
    foreignKeys = [
        ForeignKey(
            entity = ControllerEntity::class,
            parentColumns = ["ID"],
            childColumns = ["CONTROLLER_ID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["CONTROLLER_ID"]),
        Index(value = ["CONTROLLER_ID", "GRID_POS"])
    ]
)
data class RoomEntity(
    @ColumnInfo(name = "CONTROLLER_ID")
    val controllerId: Int,

    /**
     * 0..15: индекс в массиве прошивки.
     */
    @ColumnInfo(name = "ID")
    val id: Int,

    @ColumnInfo(name = "NAME", defaultValue = "''")
    val name: String = "",

    @ColumnInfo(name = "ICO_NUM", defaultValue = "200")
    val icoNum: Int = 200,

    @ColumnInfo(name = "IS_AUTO", defaultValue = "0")
    val isAuto: Boolean = false,

    /**
     * 0..4 или -1 (нет сцены).
     */
    @ColumnInfo(name = "SCENE_NUM", defaultValue = "-1")
    val sceneNum: Int = -1,

    @ColumnInfo(name = "GRID_POS", defaultValue = "0")
    val gridPos: Int = 0
)

