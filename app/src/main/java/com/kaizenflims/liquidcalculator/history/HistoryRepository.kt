package com.kaizenflims.liquidcalculator.history

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class HistoryRepository(
    private val store: DataStore<Preferences>,
) {
    val history: Flow<List<HistoryEntry>> = store.data.map {
        HistoryCodec.decode(it[historyKey].orEmpty())
    }

    suspend fun add(expression: String, result: String, timestampMillis: Long) {
        store.edit { preferences ->
            val current = HistoryCodec.decode(preferences[historyKey].orEmpty())
            val entry = HistoryEntry(
                id = UUID.randomUUID().toString(),
                expression = expression,
                result = result,
                timestampMillis = timestampMillis,
            )
            preferences[historyKey] = HistoryCodec.encode((listOf(entry) + current).take(MAX_ENTRIES))
        }
    }

    suspend fun clear() {
        store.edit { it.remove(historyKey) }
    }

    private companion object {
        const val MAX_ENTRIES = 100
        val historyKey = stringPreferencesKey("calculation_history")
    }
}

