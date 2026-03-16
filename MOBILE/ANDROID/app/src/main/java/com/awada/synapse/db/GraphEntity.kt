package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "GRAPHS",
    foreignKeys = [
        ForeignKey(
            entity = ControllerEntity::class,
            parentColumns = ["ID"],
            childColumns = ["CONTROLLER_ID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["CONTROLLER_ID"])
    ]
)
data class GraphEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Long = 0,

    @ColumnInfo(name = "CONTROLLER_ID")
    val controllerId: Int? = null,

    @ColumnInfo(name = "OBJECT_TYPE_ID")
    val objectTypeId: Int? = null,

    @ColumnInfo(name = "OBJECT_ID")
    val objectId: Long? = null,

    @ColumnInfo(name = "CHANGE_TYPE_ID")
    val changeTypeId: Int? = null,
)
