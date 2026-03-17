package com.awada.synapse.ai

import android.database.Cursor
import androidx.sqlite.db.SimpleSQLiteQuery
import com.awada.synapse.db.AppDatabase
import org.json.JSONArray
import org.json.JSONObject

object AppStateExporter {
    private val EXCLUDED_TABLES = setOf(
        "AI_MESSAGES",
        "room_master_table",
        "sqlite_sequence"
    )

    fun exportAsJson(db: AppDatabase, controllerId: Int? = null): String {
        val sqliteDb = db.openHelper.readableDatabase
        return if (controllerId == null) {
            exportAllAsJson(sqliteDb)
        } else {
            exportControllerScopedAsJson(sqliteDb, controllerId)
        }
    }

    private fun exportAllAsJson(sqliteDb: androidx.sqlite.db.SupportSQLiteDatabase): String {
        val tablesJson = JSONObject()
        sqliteDb.query(
            """
            SELECT name
            FROM sqlite_master
            WHERE type = 'table' AND name NOT LIKE 'sqlite_%'
            ORDER BY name
            """.trimIndent()
        ).use { tablesCursor ->
            while (tablesCursor.moveToNext()) {
                val tableName = tablesCursor.getString(0)
                if (tableName in EXCLUDED_TABLES) continue
                tablesJson.put(tableName, readTable(sqliteDb, tableName))
            }
        }
        return JSONObject()
            .put("tables", tablesJson)
            .toString()
    }

    private fun exportControllerScopedAsJson(
        sqliteDb: androidx.sqlite.db.SupportSQLiteDatabase,
        controllerId: Int
    ): String {
        val tablesJson = JSONObject()

        tablesJson.put(
            "CONTROLLERS",
            readQuery(
                sqliteDb,
                "SELECT * FROM CONTROLLERS WHERE ID = ? ORDER BY ID ASC",
                listOf(controllerId)
            )
        )
        tablesJson.put(
            "ROOMS",
            readQuery(
                sqliteDb,
                "SELECT * FROM ROOMS WHERE CONTROLLER_ID = ? ORDER BY GRID_POS ASC, ID ASC",
                listOf(controllerId)
            )
        )
        tablesJson.put(
            "LUMINAIRES",
            readQuery(
                sqliteDb,
                "SELECT * FROM LUMINAIRES WHERE CONTROLLER_ID = ? ORDER BY GRID_POS ASC, ID ASC",
                listOf(controllerId)
            )
        )
        tablesJson.put(
            "PRES_SENSORS",
            readQuery(
                sqliteDb,
                "SELECT * FROM PRES_SENSORS WHERE CONTROLLER_ID = ? ORDER BY GRID_POS ASC, ID ASC",
                listOf(controllerId)
            )
        )
        tablesJson.put(
            "BRIGHT_SENSORS",
            readQuery(
                sqliteDb,
                "SELECT * FROM BRIGHT_SENSORS WHERE CONTROLLER_ID = ? ORDER BY GRID_POS ASC, ID ASC",
                listOf(controllerId)
            )
        )
        tablesJson.put(
            "BUTTON_PANELS",
            readQuery(
                sqliteDb,
                "SELECT * FROM BUTTON_PANELS WHERE CONTROLLER_ID = ? ORDER BY GRID_POS ASC, ID ASC",
                listOf(controllerId)
            )
        )
        tablesJson.put(
            "EVENTS",
            readQuery(
                sqliteDb,
                "SELECT * FROM EVENTS WHERE CONTROLLER_ID = ? ORDER BY TIME ASC, ID ASC",
                listOf(controllerId)
            )
        )
        tablesJson.put(
            "GRAPHS",
            readQuery(
                sqliteDb,
                "SELECT * FROM GRAPHS WHERE CONTROLLER_ID = ? ORDER BY ID ASC",
                listOf(controllerId)
            )
        )
        tablesJson.put(
            "SCENARIOS",
            readQuery(
                sqliteDb,
                "SELECT * FROM SCENARIOS WHERE CONTROLLER_ID = ? ORDER BY ID ASC",
                listOf(controllerId)
            )
        )

        val luminaireIds = queryIds(
            sqliteDb,
            "SELECT ID FROM LUMINAIRES WHERE CONTROLLER_ID = ? ORDER BY ID ASC",
            listOf(controllerId)
        )
        val buttonIds = queryIds(
            sqliteDb,
            """
            SELECT b.ID
            FROM BUTTONS b
            JOIN BUTTON_PANELS bp ON bp.ID = b.BUTTON_PANEL_ID
            WHERE bp.CONTROLLER_ID = ?
            ORDER BY b.BUTTON_PANEL_ID ASC, b.NUM ASC, b.ID ASC
            """.trimIndent(),
            listOf(controllerId)
        )
        val graphIds = queryIds(
            sqliteDb,
            "SELECT ID FROM GRAPHS WHERE CONTROLLER_ID = ? ORDER BY ID ASC",
            listOf(controllerId)
        )
        val scenarioIds = queryIds(
            sqliteDb,
            "SELECT ID FROM SCENARIOS WHERE CONTROLLER_ID = ? ORDER BY ID ASC",
            listOf(controllerId)
        )

        tablesJson.put(
            "LUMINAIRE_SCENES",
            readRowsByIds(
                sqliteDb = sqliteDb,
                tableName = "LUMINAIRE_SCENES",
                idColumn = "LUMINAIRE_ID",
                ids = luminaireIds,
                orderBy = "SCENE_NUM ASC, LUMINAIRE_ID ASC"
            )
        )
        tablesJson.put(
            "BUTTONS",
            readRowsByIds(
                sqliteDb = sqliteDb,
                tableName = "BUTTONS",
                idColumn = "ID",
                ids = buttonIds,
                orderBy = "BUTTON_PANEL_ID ASC, NUM ASC, ID ASC"
            )
        )
        tablesJson.put(
            "SCENARIO_SET",
            readRowsByIds(
                sqliteDb = sqliteDb,
                tableName = "SCENARIO_SET",
                idColumn = "BUTTON_ID",
                ids = buttonIds,
                orderBy = "BUTTON_ID ASC, POSITION ASC, ID ASC"
            )
        )
        tablesJson.put(
            "ACTIONS",
            readRowsByIds(
                sqliteDb = sqliteDb,
                tableName = "ACTIONS",
                idColumn = "SCENARIO_ID",
                ids = scenarioIds,
                orderBy = "SCENARIO_ID ASC, POSITION ASC, ID ASC"
            )
        )
        tablesJson.put(
            "GRAPH_POINTS",
            readRowsByIds(
                sqliteDb = sqliteDb,
                tableName = "GRAPH_POINTS",
                idColumn = "GRAPH_ID",
                ids = graphIds,
                orderBy = "GRAPH_ID ASC, TIME ASC, ID ASC"
            )
        )

        tablesJson.put("GROUPS", readTable(sqliteDb, "GROUPS"))
        tablesJson.put("LUMINAIRE_TYPES", readTable(sqliteDb, "LUMINAIRE_TYPES"))

        return JSONObject()
            .put("tables", tablesJson)
            .put("scope", JSONObject().put("controllerId", controllerId))
            .toString()
    }

    private fun readTable(
        sqliteDb: androidx.sqlite.db.SupportSQLiteDatabase,
        tableName: String
    ): JSONArray {
        val tableJson = JSONArray()
        val escapedTableName = tableName.replace("\"", "\"\"")
        sqliteDb.query("""SELECT * FROM "$escapedTableName" ORDER BY rowid""").use { cursor ->
            while (cursor.moveToNext()) {
                tableJson.put(readRow(cursor))
            }
        }
        return tableJson
    }

    private fun readQuery(
        sqliteDb: androidx.sqlite.db.SupportSQLiteDatabase,
        sql: String,
        bindArgs: List<Any?>
    ): JSONArray {
        val result = JSONArray()
        sqliteDb.query(SimpleSQLiteQuery(sql, bindArgs.toTypedArray())).use { cursor ->
            while (cursor.moveToNext()) {
                result.put(readRow(cursor))
            }
        }
        return result
    }

    private fun readRowsByIds(
        sqliteDb: androidx.sqlite.db.SupportSQLiteDatabase,
        tableName: String,
        idColumn: String,
        ids: List<Long>,
        orderBy: String
    ): JSONArray {
        if (ids.isEmpty()) return JSONArray()
        val placeholders = ids.joinToString(", ") { "?" }
        return readQuery(
            sqliteDb = sqliteDb,
            sql = """SELECT * FROM "$tableName" WHERE "$idColumn" IN ($placeholders) ORDER BY $orderBy""",
            bindArgs = ids.map { it }
        )
    }

    private fun queryIds(
        sqliteDb: androidx.sqlite.db.SupportSQLiteDatabase,
        sql: String,
        bindArgs: List<Any?>
    ): List<Long> {
        val ids = mutableListOf<Long>()
        sqliteDb.query(SimpleSQLiteQuery(sql, bindArgs.toTypedArray())).use { cursor ->
            while (cursor.moveToNext()) {
                ids += cursor.getLong(0)
            }
        }
        return ids
    }

    private fun readRow(cursor: Cursor): JSONObject {
        val rowJson = JSONObject()
        for (index in 0 until cursor.columnCount) {
            val columnName = cursor.getColumnName(index)
            when (cursor.getType(index)) {
                Cursor.FIELD_TYPE_NULL -> rowJson.put(columnName, JSONObject.NULL)
                Cursor.FIELD_TYPE_INTEGER -> rowJson.put(columnName, cursor.getLong(index))
                Cursor.FIELD_TYPE_FLOAT -> rowJson.put(columnName, cursor.getDouble(index))
                Cursor.FIELD_TYPE_STRING -> rowJson.put(columnName, cursor.getString(index))
                Cursor.FIELD_TYPE_BLOB -> rowJson.put(columnName, cursor.getBlob(index).joinToString(","))
                else -> rowJson.put(columnName, cursor.getString(index))
            }
        }
        return rowJson
    }
}
