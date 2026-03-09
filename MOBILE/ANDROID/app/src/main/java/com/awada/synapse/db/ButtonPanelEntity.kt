package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "BUTTON_PANELS",
    foreignKeys = [
        ForeignKey(
            entity = ControllerEntity::class,
            parentColumns = ["ID"],
            childColumns = ["CONTROLLER_ID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["CONTROLLER_ID", "ID"],
            childColumns = ["CONTROLLER_ID", "ROOM_ID"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["CONTROLLER_ID"]),
        Index(value = ["CONTROLLER_ID", "ROOM_ID"]),
        Index(value = ["CONTROLLER_ID", "ROOM_ID", "GRID_POS"])
    ]
)
data class ButtonPanelEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Long = 0,

    @ColumnInfo(name = "CONTROLLER_ID")
    val controllerId: Int,

    @ColumnInfo(name = "ROOM_ID")
    val roomId: Int? = null,

    @ColumnInfo(name = "NAME", defaultValue = "''")
    val name: String = "",

    @ColumnInfo(name = "GRID_POS", defaultValue = "0")
    val gridPos: Int = 0
)

