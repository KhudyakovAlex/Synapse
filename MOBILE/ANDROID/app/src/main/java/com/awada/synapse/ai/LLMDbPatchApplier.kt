package com.awada.synapse.ai

import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

object LLMDbPatchApplier {
    private data class TablePatchSpec(
        val keyColumns: Set<String>,
        val mutableColumns: Set<String>
    )

    private val tableSpecs = mapOf(
        "CONTROLLERS" to TablePatchSpec(
            keyColumns = setOf("ID"),
            mutableColumns = setOf(
                "NAME", "PASSWORD", "IS_SCHEDULE", "IS_GRAPHS", "IS_AUTO",
                "ICO_NUM", "STATUS", "SCENE_NUM", "TIMESTAMP", "GRID_POS"
            )
        ),
        "ROOMS" to TablePatchSpec(
            keyColumns = setOf("CONTROLLER_ID", "ID"),
            mutableColumns = setOf("NAME", "ICO_NUM", "IS_AUTO", "SCENE_NUM", "GRID_POS")
        ),
        "GROUPS" to TablePatchSpec(
            keyColumns = setOf("ID"),
            mutableColumns = setOf("NAME")
        ),
        "LUMINAIRE_TYPES" to TablePatchSpec(
            keyColumns = setOf("ID"),
            mutableColumns = setOf("NAME")
        ),
        "LUMINAIRES" to TablePatchSpec(
            keyColumns = setOf("ID"),
            mutableColumns = setOf(
                "CONTROLLER_ID", "ROOM_ID", "GROUP_ID", "NAME", "ICO_NUM", "TYPE_ID",
                "BRIGHT", "TEMPERATURE", "SATURATION", "HUE", "GRID_POS"
            )
        ),
        "LUMINAIRE_SCENES" to TablePatchSpec(
            keyColumns = setOf("SCENE_NUM", "LUMINAIRE_ID"),
            mutableColumns = setOf("BRIGHT", "TEMPERATURE", "SATURATION", "HUE")
        ),
        "PRES_SENSORS" to TablePatchSpec(
            keyColumns = setOf("ID"),
            mutableColumns = setOf("CONTROLLER_ID", "ROOM_ID", "NAME", "GRID_POS")
        ),
        "BRIGHT_SENSORS" to TablePatchSpec(
            keyColumns = setOf("ID"),
            mutableColumns = setOf("CONTROLLER_ID", "ROOM_ID", "GROUP_ID", "NAME", "GRID_POS")
        ),
        "BUTTON_PANELS" to TablePatchSpec(
            keyColumns = setOf("ID"),
            mutableColumns = setOf("CONTROLLER_ID", "ROOM_ID", "NAME", "GRID_POS")
        ),
        "BUTTONS" to TablePatchSpec(
            keyColumns = setOf("ID"),
            mutableColumns = setOf(
                "NUM", "BUTTON_PANEL_ID", "DALI_INST", "MATRIX_ROW",
                "MATRIX_COL", "LONG_PRESS_SCENARIO_ID"
            )
        ),
        "SCENARIOS" to TablePatchSpec(
            keyColumns = setOf("ID"),
            mutableColumns = setOf("CONTROLLER_ID")
        ),
        "ACTIONS" to TablePatchSpec(
            keyColumns = setOf("ID"),
            mutableColumns = setOf(
                "SCENARIO_ID", "POSITION", "OBJECT_TYPE_ID", "OBJECT_ID",
                "CHANGE_TYPE_ID", "CHANGE_VALUE_ID"
            )
        ),
        "SCENARIO_SET" to TablePatchSpec(
            keyColumns = setOf("ID"),
            mutableColumns = setOf("BUTTON_ID", "POSITION", "SCENARIO_ID")
        ),
        "EVENTS" to TablePatchSpec(
            keyColumns = setOf("ID"),
            mutableColumns = setOf("CONTROLLER_ID", "DAYS", "TIME", "SCENARIO_ID")
        ),
        "GRAPHS" to TablePatchSpec(
            keyColumns = setOf("ID"),
            mutableColumns = setOf("CONTROLLER_ID", "OBJECT_TYPE_ID", "OBJECT_ID", "CHANGE_TYPE_ID")
        ),
        "GRAPH_POINTS" to TablePatchSpec(
            keyColumns = setOf("ID"),
            mutableColumns = setOf("GRAPH_ID", "TIME", "VALUE")
        )
    )

    fun apply(sqliteDb: SupportSQLiteDatabase, patch: LLMDbPatch) {
        patch.updates.forEach { update ->
            applyUpdate(sqliteDb, update)
        }
    }

    private fun applyUpdate(sqliteDb: SupportSQLiteDatabase, update: LLMDbUpdate) {
        val tableName = update.table.uppercase()
        val spec = tableSpecs[tableName]
            ?: error("Unsupported LLM patch table: $tableName")

        if (!update.where.keys.containsAll(spec.keyColumns)) {
            error("Missing key columns for $tableName. Required: ${spec.keyColumns.joinToString()}")
        }

        val valueEntries = update.values.entries
            .onEach { (column, _) ->
                check(column in spec.mutableColumns) { "Column $column is not mutable for $tableName" }
            }

        if (valueEntries.isEmpty()) return

        val whereEntries = spec.keyColumns.map { column ->
            column to (update.where[column] ?: error("Missing where value for $column"))
        }

        val setClause = valueEntries.joinToString(", ") { (column, _) -> "${quote(column)} = ?" }
        val whereClause = whereEntries.joinToString(" AND ") { (column, _) -> "${quote(column)} = ?" }
        val sql = "UPDATE ${quote(tableName)} SET $setClause WHERE $whereClause"
        val bindArgs = buildList<Any?> {
            valueEntries.forEach { (_, value) -> add(jsonElementToSqlValue(value)) }
            whereEntries.forEach { (_, value) -> add(jsonElementToSqlValue(value)) }
        }.toTypedArray()

        sqliteDb.execSQL(sql, bindArgs)
    }

    private fun quote(identifier: String): String = "\"${identifier.replace("\"", "\"\"")}\""

    private fun jsonElementToSqlValue(value: JsonElement): Any? {
        if (value is JsonNull) return null
        val primitive = value as? JsonPrimitive ?: return value.toString()
        if (primitive.isString) return primitive.content
        val content = primitive.content
        return when {
            content.equals("true", ignoreCase = true) -> 1
            content.equals("false", ignoreCase = true) -> 0
            content.toLongOrNull() != null -> content.toLong()
            content.toDoubleOrNull() != null -> content.toDouble()
            else -> content
        }
    }
}
