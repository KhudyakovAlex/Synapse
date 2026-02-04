package com.awada.synapse.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val AI_ENABLED = booleanPreferencesKey("ai_enabled")
}

class SettingsRepository(private val context: Context) {
    
    val isAIEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SettingsKeys.AI_ENABLED] ?: true // default: enabled
        }
    
    suspend fun setAIEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SettingsKeys.AI_ENABLED] = enabled
        }
    }
}
