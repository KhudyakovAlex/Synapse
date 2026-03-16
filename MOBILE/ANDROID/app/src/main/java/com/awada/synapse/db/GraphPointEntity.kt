package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "GRAPH_POINTS",
    foreignKeys = [
        ForeignKey(
            entity = GraphEntity::class,
            parentColumns = ["ID"],
            childColumns = ["GRAPH_ID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["GRAPH_ID"]),
        Index(value = ["GRAPH_ID", "TIME"], unique = true)
    ]
)
data class GraphPointEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Long = 0,

    @ColumnInfo(name = "GRAPH_ID")
    val graphId: Long,

    @ColumnInfo(name = "TIME", defaultValue = "''")
    val time: String = "",

    @ColumnInfo(name = "VALUE", defaultValue = "0")
    val value: Int = 0,
)
