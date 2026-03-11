package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "EVENTS",
    foreignKeys = [
        ForeignKey(
            entity = ControllerEntity::class,
            parentColumns = ["ID"],
            childColumns = ["CONTROLLER_ID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ScenarioEntity::class,
            parentColumns = ["ID"],
            childColumns = ["SCENARIO_ID"],
            onDelete = ForeignKey.SET_DEFAULT
        )
    ],
    indices = [
        Index(value = ["CONTROLLER_ID"]),
        Index(value = ["SCENARIO_ID"]),
        Index(value = ["CONTROLLER_ID", "TIME"])
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Long = 0,

    @ColumnInfo(name = "CONTROLLER_ID")
    val controllerId: Int,

    @ColumnInfo(name = "DAYS", defaultValue = "''")
    val days: String = "",

    @ColumnInfo(name = "TIME", defaultValue = "''")
    val time: String = "",

    @ColumnInfo(name = "SCENARIO_ID", defaultValue = "-1")
    val scenarioId: Long = NO_SCENARIO_ID,
) {
    companion object {
        const val NO_SCENARIO_ID = -1L
    }
}
