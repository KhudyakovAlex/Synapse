package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM GROUPS ORDER BY ID ASC")
    fun observeAll(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM GROUPS WHERE ID = :id LIMIT 1")
    fun observeById(id: Int): Flow<GroupEntity?>

    @Query("SELECT * FROM GROUPS ORDER BY ID ASC")
    suspend fun getAllOrdered(): List<GroupEntity>

    @Query("SELECT * FROM GROUPS WHERE ID = :id LIMIT 1")
    suspend fun getById(id: Int): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(group: GroupEntity)

    @Update
    suspend fun update(group: GroupEntity)

    @Query("UPDATE GROUPS SET NAME = :name WHERE ID = :id")
    suspend fun setName(id: Int, name: String)
}

