package com.example.commandcenter.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.commandcenter.data.AppRepository
import com.example.commandcenter.data.InstalledAppInfo
import com.example.commandcenter.data.LauncherAppearance
import com.example.commandcenter.data.LauncherPrefsRepository
import com.example.commandcenter.data.Rule
import com.example.commandcenter.data.RuleRepository
import com.example.commandcenter.domain.RuleEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AmirSalamViewModel(
    private val ruleRepo: RuleRepository,
    private val prefsRepo: LauncherPrefsRepository,
    private val appRepo: AppRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    val search: StateFlow<String> = query

    val rules: StateFlow<List<Rule>> = ruleRepo.rules.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val appearance: StateFlow<LauncherAppearance> = prefsRepo.appearance.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), LauncherAppearance()
    )

    private val allApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())

    // Renamed (label, app) pairs: renaming happens here so both the grid
    // and the search box always agree on the displayed name.
    val displayedApps: StateFlow<List<Pair<String, InstalledAppInfo>>> =
        combine(allApps, rules, query) { apps, ruleList, q ->
            val renamed = apps.map { app -> RuleEngine.replace(app.label, ruleList) to app }
            if (q.isBlank()) renamed
            else renamed.filter { (name, _) -> name.contains(q, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRules: StateFlow<List<Rule>> = combine(rules, query) { r, q ->
        r // rule management list is unfiltered; search box only filters the home grid
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { ruleRepo.initializeIfEmpty() }
    }

    fun loadApps(context: Context) {
        viewModelScope.launch {
            allApps.value = appRepo.loadApps()
        }
    }

    fun setQuery(value: String) { query.value = value }

    fun setColumns(value: Int) = viewModelScope.launch { prefsRepo.setColumns(value) }
    fun setShowSearchBar(value: Boolean) = viewModelScope.launch { prefsRepo.setShowSearchBar(value) }

    fun add(source: String, replacement: String, exact: Boolean, sensitive: Boolean) {
        if (source.isBlank() || replacement.isBlank()) return
        viewModelScope.launch {
            val nextId = (rules.value.maxOfOrNull { it.id } ?: 0L) + 1
            ruleRepo.save(
                rules.value + Rule(
                    nextId,
                    RuleEngine.normalize(source.trim()),
                    replacement.trim(),
                    true,
                    exact,
                    sensitive
                )
            )
        }
    }

    fun toggle(rule: Rule) = update(rule.copy(enabled = !rule.enabled))
    fun update(rule: Rule) = viewModelScope.launch {
        ruleRepo.save(rules.value.map { if (it.id == rule.id) rule else it })
    }
    fun delete(rule: Rule) = viewModelScope.launch {
        ruleRepo.save(rules.value.filterNot { it.id == rule.id })
    }
    fun duplicate(rule: Rule) = viewModelScope.launch {
        val id = (rules.value.maxOfOrNull { it.id } ?: 0L) + 1
        ruleRepo.save(rules.value + rule.copy(id = id))
    }
}
