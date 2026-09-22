package com.example.commandcenter.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore by preferencesDataStore("amirsalam_launcher_prefs")

data class LauncherAppearance(
    val columns: Int = 4,
    val showSearchBar: Boolean = true
)

class LauncherPrefsRepository(private val context: Context) {
    private val columnsKey = intPreferencesKey("columns")
    private val showSearchKey = booleanPreferencesKey("show_search")

    val appearance: Flow<LauncherAppearance> = context.launcherDataStore.data.map { prefs ->
        LauncherAppearance(
            columns = prefs[columnsKey] ?: 4,
            showSearchBar = prefs[showSearchKey] ?: true
        )
    }

    suspend fun setColumns(value: Int) {
        context.launcherDataStore.edit { it[columnsKey] = value.coerceIn(3, 6) }
    }

    suspend fun setShowSearchBar(value: Boolean) {
        context.launcherDataStore.edit { it[showSearchKey] = value }
    }
}
