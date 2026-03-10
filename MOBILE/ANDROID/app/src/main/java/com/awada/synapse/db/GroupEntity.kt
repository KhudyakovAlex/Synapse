package com.awada.synapse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "GROUPS")
data class GroupEntity(
    @PrimaryKey
    @ColumnInfo(name = "ID")
    val id: Int,

    @ColumnInfo(name = "NAME", defaultValue = "''")
    val name: String = ""
)

fun defaultGroupName(groupId: Int): String = "Группа ${groupId + 1}"

fun GroupEntity.displayName(): String = name.ifBlank { defaultGroupName(id) }

