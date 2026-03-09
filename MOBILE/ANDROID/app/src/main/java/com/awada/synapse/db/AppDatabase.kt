package com.awada.synapse.db

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ControllerEntity::class,
        AIMessageEntity::class,
        RoomEntity::class,
        LuminaireEntity::class,
        PresSensorEntity::class,
        BrightSensorEntity::class,
        ButtonPanelEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun controllerDao(): ControllerDao
    abstract fun aiMessageDao(): AIMessageDao
    abstract fun roomDao(): RoomDao
    abstract fun luminaireDao(): LuminaireDao
    abstract fun presSensorDao(): PresSensorDao
    abstract fun brightSensorDao(): BrightSensorDao
    abstract fun buttonPanelDao(): ButtonPanelDao

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
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS LUMINAIRES (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        CONTROLLER_ID INTEGER NOT NULL,
                        ROOM_ID INTEGER,
                        NAME TEXT NOT NULL DEFAULT '',
                        ICO_NUM INTEGER NOT NULL DEFAULT 300,
                        GRID_POS INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (CONTROLLER_ID) REFERENCES CONTROLLERS(ID) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY (CONTROLLER_ID, ROOM_ID) REFERENCES ROOMS(CONTROLLER_ID, ID) ON UPDATE NO ACTION ON DELETE NO ACTION
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_LUMINAIRES_CONTROLLER_ID ON LUMINAIRES (CONTROLLER_ID)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_LUMINAIRES_CONTROLLER_ID_ROOM_ID ON LUMINAIRES (CONTROLLER_ID, ROOM_ID)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_LUMINAIRES_CONTROLLER_ID_ROOM_ID_GRID_POS ON LUMINAIRES (CONTROLLER_ID, ROOM_ID, GRID_POS)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS PRES_SENSORS (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        CONTROLLER_ID INTEGER NOT NULL,
                        ROOM_ID INTEGER,
                        NAME TEXT NOT NULL DEFAULT '',
                        GRID_POS INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (CONTROLLER_ID) REFERENCES CONTROLLERS(ID) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY (CONTROLLER_ID, ROOM_ID) REFERENCES ROOMS(CONTROLLER_ID, ID) ON UPDATE NO ACTION ON DELETE NO ACTION
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_PRES_SENSORS_CONTROLLER_ID ON PRES_SENSORS (CONTROLLER_ID)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_PRES_SENSORS_CONTROLLER_ID_ROOM_ID ON PRES_SENSORS (CONTROLLER_ID, ROOM_ID)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_PRES_SENSORS_CONTROLLER_ID_ROOM_ID_GRID_POS ON PRES_SENSORS (CONTROLLER_ID, ROOM_ID, GRID_POS)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS BRIGHT_SENSORS (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        CONTROLLER_ID INTEGER NOT NULL,
                        ROOM_ID INTEGER,
                        NAME TEXT NOT NULL DEFAULT '',
                        GRID_POS INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (CONTROLLER_ID) REFERENCES CONTROLLERS(ID) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY (CONTROLLER_ID, ROOM_ID) REFERENCES ROOMS(CONTROLLER_ID, ID) ON UPDATE NO ACTION ON DELETE NO ACTION
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_BRIGHT_SENSORS_CONTROLLER_ID ON BRIGHT_SENSORS (CONTROLLER_ID)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_BRIGHT_SENSORS_CONTROLLER_ID_ROOM_ID ON BRIGHT_SENSORS (CONTROLLER_ID, ROOM_ID)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_BRIGHT_SENSORS_CONTROLLER_ID_ROOM_ID_GRID_POS ON BRIGHT_SENSORS (CONTROLLER_ID, ROOM_ID, GRID_POS)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS BUTTON_PANELS (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        CONTROLLER_ID INTEGER NOT NULL,
                        ROOM_ID INTEGER,
                        NAME TEXT NOT NULL DEFAULT '',
                        GRID_POS INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (CONTROLLER_ID) REFERENCES CONTROLLERS(ID) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY (CONTROLLER_ID, ROOM_ID) REFERENCES ROOMS(CONTROLLER_ID, ID) ON UPDATE NO ACTION ON DELETE NO ACTION
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_BUTTON_PANELS_CONTROLLER_ID ON BUTTON_PANELS (CONTROLLER_ID)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_BUTTON_PANELS_CONTROLLER_ID_ROOM_ID ON BUTTON_PANELS (CONTROLLER_ID, ROOM_ID)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_BUTTON_PANELS_CONTROLLER_ID_ROOM_ID_GRID_POS ON BUTTON_PANELS (CONTROLLER_ID, ROOM_ID, GRID_POS)")

                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS TRG_ROOMS_DELETE_LUMINAIRES_TO_ROOT
                    BEFORE DELETE ON ROOMS
                    BEGIN
                        UPDATE LUMINAIRES
                        SET ROOM_ID = NULL
                        WHERE CONTROLLER_ID = OLD.CONTROLLER_ID AND ROOM_ID = OLD.ID;
                    END
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS TRG_ROOMS_DELETE_PRES_SENSORS_TO_ROOT
                    BEFORE DELETE ON ROOMS
                    BEGIN
                        UPDATE PRES_SENSORS
                        SET ROOM_ID = NULL
                        WHERE CONTROLLER_ID = OLD.CONTROLLER_ID AND ROOM_ID = OLD.ID;
                    END
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS TRG_ROOMS_DELETE_BRIGHT_SENSORS_TO_ROOT
                    BEFORE DELETE ON ROOMS
                    BEGIN
                        UPDATE BRIGHT_SENSORS
                        SET ROOM_ID = NULL
                        WHERE CONTROLLER_ID = OLD.CONTROLLER_ID AND ROOM_ID = OLD.ID;
                    END
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS TRG_ROOMS_DELETE_BUTTON_PANELS_TO_ROOT
                    BEFORE DELETE ON ROOMS
                    BEGIN
                        UPDATE BUTTON_PANELS
                        SET ROOM_ID = NULL
                        WHERE CONTROLLER_ID = OLD.CONTROLLER_ID AND ROOM_ID = OLD.ID;
                    END
                    """.trimIndent()
                )
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
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build()
                    .also { INSTANCE = it }
            }
        }
    }
}

