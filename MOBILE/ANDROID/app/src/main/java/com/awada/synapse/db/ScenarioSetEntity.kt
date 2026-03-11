package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "SCENARIO_SET",
    foreignKeys = [
        ForeignKey(
            entity = ButtonEntity::class,
            parentColumns = ["ID"],
            childColumns = ["BUTTON_ID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ScenarioEntity::class,
            parentColumns = ["ID"],
            childColumns = ["SCENARIO_ID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["BUTTON_ID"]),
        Index(value = ["SCENARIO_ID"]),
        Index(value = ["BUTTON_ID", "POSITION"], unique = true)
    ]
)
data class ScenarioSetEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Long = 0,

    @ColumnInfo(name = "BUTTON_ID")
    val buttonId: Int,

    @ColumnInfo(name = "POSITION", defaultValue = "0")
    val position: Int = 0,

    @ColumnInfo(name = "SCENARIO_ID")
    val scenarioId: Long,
)
