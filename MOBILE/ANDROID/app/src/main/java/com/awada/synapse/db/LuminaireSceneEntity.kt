package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "LUMINAIRE_SCENES",
    primaryKeys = ["SCENE_NUM", "LUMINAIRE_ID"],
    foreignKeys = [
        ForeignKey(
            entity = LuminaireEntity::class,
            parentColumns = ["ID"],
            childColumns = ["LUMINAIRE_ID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["LUMINAIRE_ID"])
    ]
)
data class LuminaireSceneEntity(
    /**
     * DALI scene number: 0..4.
     */
    @ColumnInfo(name = "SCENE_NUM")
    val sceneNum: Int,

    @ColumnInfo(name = "LUMINAIRE_ID")
    val luminaireId: Long,

    @ColumnInfo(name = "BRIGHT", defaultValue = "0")
    val bright: Int = 0,

    @ColumnInfo(name = "TEMPERATURE")
    val temperature: Int? = null,

    @ColumnInfo(name = "SATURATION")
    val saturation: Int? = null,

    @ColumnInfo(name = "HUE")
    val hue: Int? = null
)
