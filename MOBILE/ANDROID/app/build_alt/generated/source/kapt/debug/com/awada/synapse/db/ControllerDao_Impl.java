package com.awada.synapse.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ControllerDao_Impl implements ControllerDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ControllerEntity> __insertionAdapterOfControllerEntity;

  private final EntityDeletionOrUpdateAdapter<ControllerEntity> __deletionAdapterOfControllerEntity;

  private final EntityDeletionOrUpdateAdapter<ControllerEntity> __updateAdapterOfControllerEntity;

  public ControllerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfControllerEntity = new EntityInsertionAdapter<ControllerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `CONTROLLERS` (`ID`,`NAME`,`PASSWORD`,`IS_SCHEDULE`,`IS_AUTO`,`ICO_NUM`,`STATUS`,`SCENE_NUM`,`TIMESTAMP`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ControllerEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getPassword() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPassword());
        }
        statement.bindLong(4, entity.isSchedule());
        statement.bindLong(5, entity.isAuto());
        statement.bindLong(6, entity.getIcoNum());
        if (entity.getStatus() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getStatus());
        }
        statement.bindLong(8, entity.getSceneNum());
        statement.bindLong(9, entity.getTimestamp());
      }
    };
    this.__deletionAdapterOfControllerEntity = new EntityDeletionOrUpdateAdapter<ControllerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `CONTROLLERS` WHERE `ID` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ControllerEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfControllerEntity = new EntityDeletionOrUpdateAdapter<ControllerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `CONTROLLERS` SET `ID` = ?,`NAME` = ?,`PASSWORD` = ?,`IS_SCHEDULE` = ?,`IS_AUTO` = ?,`ICO_NUM` = ?,`STATUS` = ?,`SCENE_NUM` = ?,`TIMESTAMP` = ? WHERE `ID` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ControllerEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getPassword() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPassword());
        }
        statement.bindLong(4, entity.isSchedule());
        statement.bindLong(5, entity.isAuto());
        statement.bindLong(6, entity.getIcoNum());
        if (entity.getStatus() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getStatus());
        }
        statement.bindLong(8, entity.getSceneNum());
        statement.bindLong(9, entity.getTimestamp());
        statement.bindLong(10, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final ControllerEntity controller,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfControllerEntity.insertAndReturnId(controller);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ControllerEntity controller,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfControllerEntity.handle(controller);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ControllerEntity controller,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfControllerEntity.handle(controller);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ControllerEntity>> observeAll() {
    final String _sql = "SELECT * FROM CONTROLLERS ORDER BY ID ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"CONTROLLERS"}, new Callable<List<ControllerEntity>>() {
      @Override
      @NonNull
      public List<ControllerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "ID");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "NAME");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "PASSWORD");
          final int _cursorIndexOfIsSchedule = CursorUtil.getColumnIndexOrThrow(_cursor, "IS_SCHEDULE");
          final int _cursorIndexOfIsAuto = CursorUtil.getColumnIndexOrThrow(_cursor, "IS_AUTO");
          final int _cursorIndexOfIcoNum = CursorUtil.getColumnIndexOrThrow(_cursor, "ICO_NUM");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "STATUS");
          final int _cursorIndexOfSceneNum = CursorUtil.getColumnIndexOrThrow(_cursor, "SCENE_NUM");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "TIMESTAMP");
          final List<ControllerEntity> _result = new ArrayList<ControllerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ControllerEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpPassword;
            if (_cursor.isNull(_cursorIndexOfPassword)) {
              _tmpPassword = null;
            } else {
              _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            }
            final int _tmpIsSchedule;
            _tmpIsSchedule = _cursor.getInt(_cursorIndexOfIsSchedule);
            final int _tmpIsAuto;
            _tmpIsAuto = _cursor.getInt(_cursorIndexOfIsAuto);
            final int _tmpIcoNum;
            _tmpIcoNum = _cursor.getInt(_cursorIndexOfIcoNum);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final int _tmpSceneNum;
            _tmpSceneNum = _cursor.getInt(_cursorIndexOfSceneNum);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new ControllerEntity(_tmpId,_tmpName,_tmpPassword,_tmpIsSchedule,_tmpIsAuto,_tmpIcoNum,_tmpStatus,_tmpSceneNum,_tmpTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getById(final int id, final Continuation<? super ControllerEntity> $completion) {
    final String _sql = "SELECT * FROM CONTROLLERS WHERE ID = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ControllerEntity>() {
      @Override
      @Nullable
      public ControllerEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "ID");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "NAME");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "PASSWORD");
          final int _cursorIndexOfIsSchedule = CursorUtil.getColumnIndexOrThrow(_cursor, "IS_SCHEDULE");
          final int _cursorIndexOfIsAuto = CursorUtil.getColumnIndexOrThrow(_cursor, "IS_AUTO");
          final int _cursorIndexOfIcoNum = CursorUtil.getColumnIndexOrThrow(_cursor, "ICO_NUM");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "STATUS");
          final int _cursorIndexOfSceneNum = CursorUtil.getColumnIndexOrThrow(_cursor, "SCENE_NUM");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "TIMESTAMP");
          final ControllerEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpPassword;
            if (_cursor.isNull(_cursorIndexOfPassword)) {
              _tmpPassword = null;
            } else {
              _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            }
            final int _tmpIsSchedule;
            _tmpIsSchedule = _cursor.getInt(_cursorIndexOfIsSchedule);
            final int _tmpIsAuto;
            _tmpIsAuto = _cursor.getInt(_cursorIndexOfIsAuto);
            final int _tmpIcoNum;
            _tmpIcoNum = _cursor.getInt(_cursorIndexOfIcoNum);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final int _tmpSceneNum;
            _tmpSceneNum = _cursor.getInt(_cursorIndexOfSceneNum);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _result = new ControllerEntity(_tmpId,_tmpName,_tmpPassword,_tmpIsSchedule,_tmpIsAuto,_tmpIcoNum,_tmpStatus,_tmpSceneNum,_tmpTimestamp);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
