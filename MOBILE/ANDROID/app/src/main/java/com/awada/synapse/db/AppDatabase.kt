package com.awada.synapse.db

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.room.RoomDatabase.Callback
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ControllerEntity::class,
        AIMessageEntity::class,
        RoomEntity::class,
        LuminaireTypeEntity::class,
        LuminaireEntity::class,
        LuminaireSceneEntity::class,
        PresSensorEntity::class,
        BrightSensorEntity::class,
        ButtonPanelEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun controllerDao(): ControllerDao
    abstract fun aiMessageDao(): AIMessageDao
    abstract fun roomDao(): RoomDao
    abstract fun luminaireTypeDao(): LuminaireTypeDao
    abstract fun luminaireDao(): LuminaireDao
    abstract fun luminaireSceneDao(): LuminaireSceneDao
    abstract fun presSensorDao(): PresSensorDao
    abstract fun brightSensorDao(): BrightSensorDao
    abstract fun buttonPanelDao(): ButtonPanelDao

    companion object {
        private const val DB_NAME = "synapse.db"
        private fun insertLuminaireTypes(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                INSERT OR IGNORE INTO LUMINAIRE_TYPES (ID, NAME) VALUES
                    (${LuminaireTypeEntity.TYPE_ON_OFF}, 'Вкл/выкл'),
                    (${LuminaireTypeEntity.TYPE_DIMMABLE}, 'Диммируемый'),
                    (${LuminaireTypeEntity.TYPE_RGB}, 'RGB'),
                    (${LuminaireTypeEntity.TYPE_TW}, 'TW')
                """.trimIndent()
            )
        }

        private val SEED_LUMINAIRE_TYPES_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                insertLuminaireTypes(db)
            }
        }

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
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE LUMINAIRES ADD COLUMN BRIGHT INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE LUMINAIRES ADD COLUMN TEMPERATURE INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE LUMINAIRES ADD COLUMN SATURATION INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE LUMINAIRES ADD COLUMN HUE INTEGER NOT NULL DEFAULT 0")
            }
        }
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS LUMINAIRE_TYPES (
                        ID INTEGER PRIMARY KEY NOT NULL,
                        NAME TEXT NOT NULL DEFAULT ''
                    )
                    """.trimIndent()
                )
                insertLuminaireTypes(db)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS LUMINAIRES_NEW (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        CONTROLLER_ID INTEGER NOT NULL,
                        ROOM_ID INTEGER,
                        NAME TEXT NOT NULL DEFAULT '',
                        ICO_NUM INTEGER NOT NULL DEFAULT 300,
                        TYPE_ID INTEGER NOT NULL DEFAULT 2,
                        BRIGHT INTEGER NOT NULL DEFAULT 0,
                        TEMPERATURE INTEGER NOT NULL DEFAULT 0,
                        SATURATION INTEGER NOT NULL DEFAULT 0,
                        HUE INTEGER NOT NULL DEFAULT 0,
                        GRID_POS INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (CONTROLLER_ID) REFERENCES CONTROLLERS(ID) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY (CONTROLLER_ID, ROOM_ID) REFERENCES ROOMS(CONTROLLER_ID, ID) ON UPDATE NO ACTION ON DELETE NO ACTION,
                        FOREIGN KEY (TYPE_ID) REFERENCES LUMINAIRE_TYPES(ID) ON UPDATE NO ACTION ON DELETE NO ACTION
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO LUMINAIRES_NEW (
                        ID,
                        CONTROLLER_ID,
                        ROOM_ID,
                        NAME,
                        ICO_NUM,
                        TYPE_ID,
                        BRIGHT,
                        TEMPERATURE,
                        SATURATION,
                        HUE,
                        GRID_POS
                    )
                    SELECT
                        ID,
                        CONTROLLER_ID,
                        ROOM_ID,
                        NAME,
                        ICO_NUM,
                        ${LuminaireTypeEntity.TYPE_DIMMABLE},
                        BRIGHT,
                        TEMPERATURE,
                        SATURATION,
                        HUE,
                        GRID_POS
                    FROM LUMINAIRES
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE LUMINAIRES")
                db.execSQL("ALTER TABLE LUMINAIRES_NEW RENAME TO LUMINAIRES")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_LUMINAIRES_CONTROLLER_ID ON LUMINAIRES (CONTROLLER_ID)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_LUMINAIRES_CONTROLLER_ID_ROOM_ID ON LUMINAIRES (CONTROLLER_ID, ROOM_ID)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_LUMINAIRES_CONTROLLER_ID_ROOM_ID_GRID_POS ON LUMINAIRES (CONTROLLER_ID, ROOM_ID, GRID_POS)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_LUMINAIRES_TYPE_ID ON LUMINAIRES (TYPE_ID)")
            }
        }
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS LUMINAIRE_SCENES (
                        SCENE_NUM INTEGER NOT NULL,
                        LUMINAIRE_ID INTEGER NOT NULL,
                        BRIGHT INTEGER NOT NULL DEFAULT 0,
                        TEMPERATURE INTEGER,
                        SATURATION INTEGER,
                        HUE INTEGER,
                        PRIMARY KEY (SCENE_NUM, LUMINAIRE_ID),
                        FOREIGN KEY (LUMINAIRE_ID) REFERENCES LUMINAIRES(ID) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_LUMINAIRE_SCENES_LUMINAIRE_ID ON LUMINAIRE_SCENES (LUMINAIRE_ID)")
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
                ).addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8
                ).addCallback(SEED_LUMINAIRE_TYPES_CALLBACK).build()
                    .also { INSTANCE = it }
            }
        }
    }
}

