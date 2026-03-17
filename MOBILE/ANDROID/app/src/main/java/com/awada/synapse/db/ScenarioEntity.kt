package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "SCENARIOS",
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
data class ScenarioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Long = 0,

    @ColumnInfo(name = "CONTROLLER_ID")
    val controllerId: Int? = null,
) {
    companion object {
        const val PLACEHOLDER_ID = -1L
    }
}
