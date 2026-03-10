package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "LUMINAIRES",
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
            entity = LuminaireTypeEntity::class,
            parentColumns = ["ID"],
            childColumns = ["TYPE_ID"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["CONTROLLER_ID"]),
        Index(value = ["CONTROLLER_ID", "ROOM_ID"]),
        Index(value = ["CONTROLLER_ID", "ROOM_ID", "GRID_POS"]),
        Index(value = ["TYPE_ID"])
    ]
)
data class LuminaireEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Long = 0,

    @ColumnInfo(name = "CONTROLLER_ID")
    val controllerId: Int,

    @ColumnInfo(name = "ROOM_ID")
    val roomId: Int? = null,

    @ColumnInfo(name = "NAME", defaultValue = "''")
    val name: String = "",

    @ColumnInfo(name = "ICO_NUM", defaultValue = "300")
    val icoNum: Int = 300,

    @ColumnInfo(name = "TYPE_ID", defaultValue = "2")
    val typeId: Int = LuminaireTypeEntity.TYPE_DIMMABLE,

    @ColumnInfo(name = "BRIGHT", defaultValue = "0")
    val bright: Int = 0,

    @ColumnInfo(name = "TEMPERATURE", defaultValue = "0")
    val temperature: Int = 0,

    @ColumnInfo(name = "SATURATION", defaultValue = "0")
    val saturation: Int = 0,

    @ColumnInfo(name = "HUE", defaultValue = "0")
    val hue: Int = 0,

    @ColumnInfo(name = "GRID_POS", defaultValue = "0")
    val gridPos: Int = 0
)

