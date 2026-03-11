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
        GroupEntity::class,
        LuminaireTypeEntity::class,
        LuminaireEntity::class,
        LuminaireSceneEntity::class,
        PresSensorEntity::class,
        BrightSensorEntity::class,
        ButtonPanelEntity::class,
        ButtonEntity::class,
        ScenarioEntity::class,
        ActionEntity::class,
        ScenarioSetEntity::class
    ],
    version = 16,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun controllerDao(): ControllerDao
    abstract fun aiMessageDao(): AIMessageDao
    abstract fun roomDao(): RoomDao
    abstract fun groupDao(): GroupDao
    abstract fun luminaireTypeDao(): LuminaireTypeDao
    abstract fun luminaireDao(): LuminaireDao
    abstract fun luminaireSceneDao(): LuminaireSceneDao
    abstract fun presSensorDao(): PresSensorDao
    abstract fun brightSensorDao(): BrightSensorDao
    abstract fun buttonPanelDao(): ButtonPanelDao
    abstract fun buttonDao(): ButtonDao
    abstract fun scenarioDao(): ScenarioDao
    abstract fun actionDao(): ActionDao
    abstract fun scenarioSetDao(): ScenarioSetDao

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

        private fun insertGroups(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS GROUPS (
                    ID INTEGER PRIMARY KEY NOT NULL,
                    NAME TEXT NOT NULL DEFAULT ''
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT OR IGNORE INTO GROUPS (ID, NAME) VALUES
                    (0, 'Группа 1'),
                    (1, 'Группа 2'),
                    (2, 'Группа 3'),
                    (3, 'Группа 4'),
                    (4, 'Группа 5'),
                    (5, 'Группа 6'),
                    (6, 'Группа 7'),
                    (7, 'Группа 8'),
                    (8, 'Группа 9'),
                    (9, 'Группа 10'),
                    (10, 'Группа 11'),
                    (11, 'Группа 12'),
                    (12, 'Группа 13'),
                    (13, 'Группа 14'),
                    (14, 'Группа 15'),
                    (15, 'Группа 16')
                """.trimIndent()
            )
            db.execSQL(
                """
                UPDATE GROUPS
                SET NAME = 'Группа ' || (ID + 1)
                WHERE NAME IS NULL OR TRIM(NAME) = ''
                """.trimIndent()
            )
        }

        private val SEED_LUMINAIRE_TYPES_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                insertLuminaireTypes(db)
                insertGroups(db)
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

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Ensure room names are never empty; required for downstream JSON export.
                db.execSQL(
                    """
                    UPDATE ROOMS
                    SET NAME = 'Помещение ' || (ID + 1)
                    WHERE NAME IS NULL OR TRIM(NAME) = ''
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                insertGroups(db)
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add group binding support for luminaires (nullable GROUP_ID).
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS LUMINAIRES_NEW (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        CONTROLLER_ID INTEGER NOT NULL,
                        ROOM_ID INTEGER,
                        GROUP_ID INTEGER,
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
                        FOREIGN KEY (GROUP_ID) REFERENCES GROUPS(ID) ON UPDATE NO ACTION ON DELETE SET NULL,
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
                        GROUP_ID,
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
                        NULL,
                        NAME,
                        ICO_NUM,
                        TYPE_ID,
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
                db.execSQL("CREATE INDEX IF NOT EXISTS index_LUMINAIRES_GROUP_ID ON LUMINAIRES (GROUP_ID)")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add group binding support for bright sensors (nullable GROUP_ID).
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS BRIGHT_SENSORS_NEW (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        CONTROLLER_ID INTEGER NOT NULL,
                        ROOM_ID INTEGER,
                        GROUP_ID INTEGER,
                        NAME TEXT NOT NULL DEFAULT '',
                        GRID_POS INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (CONTROLLER_ID) REFERENCES CONTROLLERS(ID) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY (CONTROLLER_ID, ROOM_ID) REFERENCES ROOMS(CONTROLLER_ID, ID) ON UPDATE NO ACTION ON DELETE NO ACTION,
                        FOREIGN KEY (GROUP_ID) REFERENCES GROUPS(ID) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO BRIGHT_SENSORS_NEW (
                        ID,
                        CONTROLLER_ID,
                        ROOM_ID,
                        GROUP_ID,
                        NAME,
                        GRID_POS
                    )
                    SELECT
                        ID,
                        CONTROLLER_ID,
                        ROOM_ID,
                        NULL,
                        NAME,
                        GRID_POS
                    FROM BRIGHT_SENSORS
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE BRIGHT_SENSORS")
                db.execSQL("ALTER TABLE BRIGHT_SENSORS_NEW RENAME TO BRIGHT_SENSORS")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_BRIGHT_SENSORS_CONTROLLER_ID ON BRIGHT_SENSORS (CONTROLLER_ID)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_BRIGHT_SENSORS_CONTROLLER_ID_ROOM_ID ON BRIGHT_SENSORS (CONTROLLER_ID, ROOM_ID)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_BRIGHT_SENSORS_CONTROLLER_ID_ROOM_ID_GRID_POS ON BRIGHT_SENSORS (CONTROLLER_ID, ROOM_ID, GRID_POS)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_BRIGHT_SENSORS_GROUP_ID ON BRIGHT_SENSORS (GROUP_ID)")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS BUTTONS (
                        ID INTEGER PRIMARY KEY NOT NULL,
                        NUM INTEGER NOT NULL,
                        BUTTON_PANEL_ID INTEGER NOT NULL DEFAULT -1,
                        DALI_INST INTEGER NOT NULL DEFAULT -1,
                        CHECK(ID BETWEEN 0 AND 511),
                        CHECK(BUTTON_PANEL_ID >= -1),
                        CHECK(DALI_INST >= -1)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_BUTTONS_BUTTON_PANEL_ID ON BUTTONS (BUTTON_PANEL_ID)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_BUTTONS_BUTTON_PANEL_ID_NUM ON BUTTONS (BUTTON_PANEL_ID, NUM)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_BUTTONS_DALI_INST ON BUTTONS (DALI_INST)")
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO BUTTONS (ID, NUM, BUTTON_PANEL_ID, DALI_INST)
                    SELECT
                        panels.PANEL_ORDER * 4 + nums.BUTTON_OFFSET,
                        nums.BUTTON_OFFSET + 1,
                        panels.PANEL_ID,
                        -1
                    FROM (
                        SELECT
                            bp1.ID AS PANEL_ID,
                            (
                                SELECT COUNT(*)
                                FROM BUTTON_PANELS bp2
                                WHERE bp2.ID < bp1.ID
                            ) AS PANEL_ORDER
                        FROM BUTTON_PANELS bp1
                    ) panels
                    CROSS JOIN (
                        SELECT 0 AS BUTTON_OFFSET
                        UNION ALL SELECT 1
                        UNION ALL SELECT 2
                        UNION ALL SELECT 3
                    ) nums
                    WHERE panels.PANEL_ORDER * 4 + nums.BUTTON_OFFSET <= 511
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE BUTTONS ADD COLUMN MATRIX_ROW INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE BUTTONS ADD COLUMN MATRIX_COL INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    UPDATE BUTTONS AS b1
                    SET MATRIX_ROW = (
                            SELECT COUNT(*)
                            FROM BUTTONS AS b2
                            WHERE b2.BUTTON_PANEL_ID = b1.BUTTON_PANEL_ID
                              AND (b2.NUM < b1.NUM OR (b2.NUM = b1.NUM AND b2.ID < b1.ID))
                        ) / 4,
                        MATRIX_COL = (
                            SELECT COUNT(*)
                            FROM BUTTONS AS b2
                            WHERE b2.BUTTON_PANEL_ID = b1.BUTTON_PANEL_ID
                              AND (b2.NUM < b1.NUM OR (b2.NUM = b1.NUM AND b2.ID < b1.ID))
                        ) % 4
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_BUTTONS_BUTTON_PANEL_ID_MATRIX_ROW_MATRIX_COL
                    ON BUTTONS (BUTTON_PANEL_ID, MATRIX_ROW, MATRIX_COL)
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS SCENARIOS (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ACTIONS (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        SCENARIO_ID INTEGER NOT NULL,
                        POSITION INTEGER NOT NULL DEFAULT 0,
                        WHERE_ID INTEGER,
                        WHAT_ID INTEGER,
                        VALUE_ID INTEGER,
                        FOREIGN KEY (SCENARIO_ID) REFERENCES SCENARIOS(ID) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ACTIONS_SCENARIO_ID ON ACTIONS (SCENARIO_ID)")
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_ACTIONS_SCENARIO_ID_POSITION
                    ON ACTIONS (SCENARIO_ID, POSITION)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS SCENARIO_SET (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        BUTTON_ID INTEGER NOT NULL,
                        POSITION INTEGER NOT NULL DEFAULT 0,
                        SCENARIO_ID INTEGER NOT NULL,
                        FOREIGN KEY (BUTTON_ID) REFERENCES BUTTONS(ID) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY (SCENARIO_ID) REFERENCES SCENARIOS(ID) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_SCENARIO_SET_BUTTON_ID ON SCENARIO_SET (BUTTON_ID)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_SCENARIO_SET_SCENARIO_ID ON SCENARIO_SET (SCENARIO_ID)")
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_SCENARIO_SET_BUTTON_ID_POSITION
                    ON SCENARIO_SET (BUTTON_ID, POSITION)
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE BUTTONS ADD COLUMN LONG_PRESS_SCENARIO_ID INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_BUTTONS_LONG_PRESS_SCENARIO_ID ON BUTTONS (LONG_PRESS_SCENARIO_ID)")
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
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13,
                    MIGRATION_13_14,
                    MIGRATION_14_15,
                    MIGRATION_15_16
                ).addCallback(SEED_LUMINAIRE_TYPES_CALLBACK).build()
                    .also { INSTANCE = it }
            }
        }
    }
}

