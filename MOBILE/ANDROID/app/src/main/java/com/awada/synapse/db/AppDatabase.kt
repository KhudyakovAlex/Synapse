package com.awada.synapse.db

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ControllerEntity::class, AIMessageEntity::class, RoomEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun controllerDao(): ControllerDao
    abstract fun aiMessageDao(): AIMessageDao
    abstract fun roomDao(): RoomDao

    companion object {
        private const val DB_NAME = "synapse.db"
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE CONTROLLERS ADD COLUMN GRID_POS INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS AI_MESSAGES (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        ROLE TEXT NOT NULL,
                        TEXT TEXT NOT NULL,
                        CREATED_AT INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ROOMS (
                        CONTROLLER_ID INTEGER NOT NULL,
                        ID INTEGER NOT NULL,
                        NAME TEXT NOT NULL DEFAULT '',
                        ICO_NUM INTEGER NOT NULL DEFAULT 200,
                        IS_AUTO INTEGER NOT NULL DEFAULT 0,
                        SCENE_NUM INTEGER NOT NULL DEFAULT -1,
                        GRID_POS INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY (CONTROLLER_ID, ID),
                        FOREIGN KEY (CONTROLLER_ID) REFERENCES CONTROLLERS(ID) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ROOMS_CONTROLLER_ID ON ROOMS (CONTROLLER_ID)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ROOMS_CONTROLLER_ID_GRID_POS ON ROOMS (CONTROLLER_ID, GRID_POS)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
                    .also { INSTANCE = it }
            }
        }
    }
}

