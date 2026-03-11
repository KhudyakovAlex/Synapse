package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ACTIONS",
    foreignKeys = [
        ForeignKey(
            entity = ScenarioEntity::class,
            parentColumns = ["ID"],
            childColumns = ["SCENARIO_ID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["SCENARIO_ID"]),
        Index(value = ["SCENARIO_ID", "POSITION"], unique = true)
    ]
)
data class ActionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Long = 0,

    @ColumnInfo(name = "SCENARIO_ID")
    val scenarioId: Long,

    @ColumnInfo(name = "POSITION", defaultValue = "0")
    val position: Int = 0,

    @ColumnInfo(name = "WHERE_ID")
    val whereId: Int? = null,

    @ColumnInfo(name = "WHAT_ID")
    val whatId: Int? = null,

    @ColumnInfo(name = "VALUE_ID")
    val valueId: Int? = null,
)
