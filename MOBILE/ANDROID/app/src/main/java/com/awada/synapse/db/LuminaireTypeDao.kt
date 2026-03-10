package com.awada.synapse.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface LuminaireTypeDao {
    @Query("SELECT * FROM LUMINAIRE_TYPES ORDER BY ID ASC")
    suspend fun getAllOrdered(): List<LuminaireTypeEntity>
}
