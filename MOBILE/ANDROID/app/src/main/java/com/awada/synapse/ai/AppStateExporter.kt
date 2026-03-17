package com.awada.synapse.ai

import android.database.Cursor
import com.awada.synapse.db.AppDatabase
import org.json.JSONArray
import org.json.JSONObject

object AppStateExporter {
    private val EXCLUDED_TABLES = setOf(
        "AI_MESSAGES",
        "room_master_table",
        "sqlite_sequence"
    )

    fun exportAsJson(db: AppDatabase): String {
        val sqliteDb = db.openHelper.readableDatabase
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
