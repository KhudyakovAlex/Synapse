package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LuminaireSceneDao {
    @Query(
        """
        SELECT * FROM LUMINAIRE_SCENES
        WHERE LUMINAIRE_ID = :luminaireId
        ORDER BY SCENE_NUM ASC
        """
    )
    fun observeAllForLuminaire(luminaireId: Long): Flow<List<LuminaireSceneEntity>>

    @Query(
        """
        SELECT * FROM LUMINAIRE_SCENES
        WHERE SCENE_NUM = :sceneNum
        ORDER BY LUMINAIRE_ID ASC
        """
    )
    suspend fun getAllForScene(sceneNum: Int): List<LuminaireSceneEntity>

    @Query(
        """
        SELECT * FROM LUMINAIRE_SCENES
        WHERE SCENE_NUM = :sceneNum AND LUMINAIRE_ID IN (:luminaireIds)
        """
    )
    suspend fun getAllForSceneAndLuminaires(
        sceneNum: Int,
        luminaireIds: List<Long>
    ): List<LuminaireSceneEntity>

    @Query(
        """
        SELECT * FROM LUMINAIRE_SCENES
        WHERE SCENE_NUM = :sceneNum AND LUMINAIRE_ID = :luminaireId
        LIMIT 1
        """
    )
    suspend fun getBySceneAndLuminaire(sceneNum: Int, luminaireId: Long): LuminaireSceneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LuminaireSceneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<LuminaireSceneEntity>)

    @Query(
        """
        DELETE FROM LUMINAIRE_SCENES
        WHERE SCENE_NUM = :sceneNum AND LUMINAIRE_ID = :luminaireId
        """
    )
    suspend fun deleteBySceneAndLuminaire(sceneNum: Int, luminaireId: Long)

    @Query("DELETE FROM LUMINAIRE_SCENES WHERE LUMINAIRE_ID = :luminaireId")
    suspend fun deleteAllForLuminaire(luminaireId: Long)
}
