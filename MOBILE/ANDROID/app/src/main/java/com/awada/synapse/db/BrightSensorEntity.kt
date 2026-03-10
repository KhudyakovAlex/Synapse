package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "BRIGHT_SENSORS",
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
        ),
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["ID"],
            childColumns = ["GROUP_ID"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["CONTROLLER_ID"]),
        Index(value = ["CONTROLLER_ID", "ROOM_ID"]),
        Index(value = ["CONTROLLER_ID", "ROOM_ID", "GRID_POS"]),
        Index(value = ["GROUP_ID"])
    ]
)
data class BrightSensorEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Long = 0,

    @ColumnInfo(name = "CONTROLLER_ID")
    val controllerId: Int,

    @ColumnInfo(name = "ROOM_ID")
    val roomId: Int? = null,

    @ColumnInfo(name = "GROUP_ID")
    val groupId: Int? = null,

    @ColumnInfo(name = "NAME", defaultValue = "''")
    val name: String = "",

    @ColumnInfo(name = "GRID_POS", defaultValue = "0")
    val gridPos: Int = 0
)

