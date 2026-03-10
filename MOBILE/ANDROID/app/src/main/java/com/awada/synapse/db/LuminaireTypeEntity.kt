package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LUMINAIRE_TYPES")
data class LuminaireTypeEntity(
    @PrimaryKey
    @ColumnInfo(name = "ID")
    val id: Int,

    @ColumnInfo(name = "NAME", defaultValue = "''")
    val name: String = ""
) {
    companion object {
        const val TYPE_ON_OFF = 1
        const val TYPE_DIMMABLE = 2
        const val TYPE_RGB = 3
        const val TYPE_TW = 4
    }
}
