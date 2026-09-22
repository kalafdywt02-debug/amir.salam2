package com.example.commandcenter.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore("command_center_rules")

class RuleRepository(private val context: Context) {
    private val key = stringPreferencesKey("rules_json")
    private val serviceOnKey = booleanPreferencesKey("service_on")

    val serviceOn: Flow<Boolean> = context.dataStore.data.map { it[serviceOnKey] ?: false }

    suspend fun setServiceOn(value: Boolean) {
        context.dataStore.edit { it[serviceOnKey] = value }
    }

    val rules: Flow<List<Rule>> = context.dataStore.data.map { prefs ->
        val raw = prefs[key]
        if (raw.isNullOrBlank()) DefaultRules.create() else decode(raw)
    }

    suspend fun initializeIfEmpty() {
        context.dataStore.edit { prefs ->
            if (!prefs.contains(key)) prefs[key] = encode(DefaultRules.create())
        }
    }

    suspend fun save(rules: List<Rule>) {
        context.dataStore.edit { it[key] = encode(rules) }
    }

    private fun encode(rules: List<Rule>): String {
        val arr = JSONArray()
        rules.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id)
                put("source", it.source)
                put("replacement", it.replacement)
                put("enabled", it.enabled)
                put("exactMatch", it.exactMatch)
                put("caseSensitive", it.caseSensitive)
            })
        }
        return arr.toString()
    }

    private fun decode(raw: String): List<Rule> {
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        Rule(
                            id = o.getLong("id"),
                            source = o.getString("source"),
                            replacement = o.getString("replacement"),
                            enabled = o.optBoolean("enabled", true),
                            exactMatch = o.optBoolean("exactMatch", false),
                            caseSensitive = o.optBoolean("caseSensitive", false)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
