package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AI_MESSAGES")
data class AiMessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Long = 0,

    @ColumnInfo(name = "ROLE")
    val role: String,

    @ColumnInfo(name = "TEXT")
    val text: String,

    @ColumnInfo(name = "CREATED_AT")
    val createdAt: Long
)

