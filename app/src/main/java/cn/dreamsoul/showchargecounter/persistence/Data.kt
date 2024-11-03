package cn.dreamsoul.showchargecounter.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * DataStore的相关操作（主要是存入）
 * */
val MAX_CHARGECOUNTER = stringPreferencesKey("max_chargecounter")
val MIN_CHARGECOUNTER = stringPreferencesKey("min_chargecounter")
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

suspend fun saveMaxAndMin(context: Context, max: String, min: String) {
    context.dataStore.edit { settings ->
        settings[MAX_CHARGECOUNTER] = max
        settings[MIN_CHARGECOUNTER] = min
    }
}
