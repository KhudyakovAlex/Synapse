package com.awada.synapse.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ControllerDao _controllerDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `CONTROLLERS` (`ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `NAME` TEXT NOT NULL DEFAULT '', `PASSWORD` TEXT NOT NULL DEFAULT '', `IS_SCHEDULE` INTEGER NOT NULL DEFAULT 0, `IS_AUTO` INTEGER NOT NULL DEFAULT 0, `ICO_NUM` INTEGER NOT NULL DEFAULT 100, `STATUS` TEXT NOT NULL DEFAULT 'A', `SCENE_NUM` INTEGER NOT NULL DEFAULT -1, `TIMESTAMP` INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '7a6c6c2209a7bd4a8520a7a366a485e3')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `CONTROLLERS`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCONTROLLERS = new HashMap<String, TableInfo.Column>(9);
        _columnsCONTROLLERS.put("ID", new TableInfo.Column("ID", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCONTROLLERS.put("NAME", new TableInfo.Column("NAME", "TEXT", true, 0, "''", TableInfo.CREATED_FROM_ENTITY));
        _columnsCONTROLLERS.put("PASSWORD", new TableInfo.Column("PASSWORD", "TEXT", true, 0, "''", TableInfo.CREATED_FROM_ENTITY));
        _columnsCONTROLLERS.put("IS_SCHEDULE", new TableInfo.Column("IS_SCHEDULE", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsCONTROLLERS.put("IS_AUTO", new TableInfo.Column("IS_AUTO", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsCONTROLLERS.put("ICO_NUM", new TableInfo.Column("ICO_NUM", "INTEGER", true, 0, "100", TableInfo.CREATED_FROM_ENTITY));
        _columnsCONTROLLERS.put("STATUS", new TableInfo.Column("STATUS", "TEXT", true, 0, "'A'", TableInfo.CREATED_FROM_ENTITY));
        _columnsCONTROLLERS.put("SCENE_NUM", new TableInfo.Column("SCENE_NUM", "INTEGER", true, 0, "-1", TableInfo.CREATED_FROM_ENTITY));
        _columnsCONTROLLERS.put("TIMESTAMP", new TableInfo.Column("TIMESTAMP", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCONTROLLERS = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCONTROLLERS = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCONTROLLERS = new TableInfo("CONTROLLERS", _columnsCONTROLLERS, _foreignKeysCONTROLLERS, _indicesCONTROLLERS);
        final TableInfo _existingCONTROLLERS = TableInfo.read(db, "CONTROLLERS");
        if (!_infoCONTROLLERS.equals(_existingCONTROLLERS)) {
          return new RoomOpenHelper.ValidationResult(false, "CONTROLLERS(com.awada.synapse.db.ControllerEntity).\n"
                  + " Expected:\n" + _infoCONTROLLERS + "\n"
                  + " Found:\n" + _existingCONTROLLERS);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "7a6c6c2209a7bd4a8520a7a366a485e3", "e09def729c4e3af6d25b7195dc48566b");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "CONTROLLERS");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `CONTROLLERS`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ControllerDao.class, ControllerDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ControllerDao controllerDao() {
    if (_controllerDao != null) {
      return _controllerDao;
    } else {
      synchronized(this) {
        if(_controllerDao == null) {
          _controllerDao = new ControllerDao_Impl(this);
        }
        return _controllerDao;
      }
    }
  }
}
