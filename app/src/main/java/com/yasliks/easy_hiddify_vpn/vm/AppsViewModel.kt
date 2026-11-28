package com.yasliks.easy_hiddify_vpn.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yas.database.data.AppDTO
import com.yas.database.repo.AppsRepo
import com.yasliks.easy_hiddify_vpn.utils.InstalledAppsSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AppsFilter {
    ALL,
    ENABLED,
    DISABLED,
}

@HiltViewModel
class AppsViewModel @Inject constructor(
    appsRepo: AppsRepo,
    private val installedApps: InstalledAppsSync,
) : ViewModel() {

    private val _searchAppsStr = MutableStateFlow("")
    val searchAppsStr = _searchAppsStr.asStateFlow()

    private val _appsFilter = MutableStateFlow(AppsFilter.ALL)
    val appsFilter = _appsFilter.asStateFlow()

    private val allApps = appsRepo.getAllFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = persistentListOf(),
    )

    val apps: StateFlow<ImmutableList<AppDTO>> = combine(allApps, searchAppsStr, appsFilter) { list, query, filter ->
        list.filter { app ->
            val matchesFilter = when (filter) {
                AppsFilter.ALL -> true
                AppsFilter.ENABLED -> app.isSelected
                AppsFilter.DISABLED -> !app.isSelected
            }
            matchesFilter && (app.name.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true))
        }.toImmutableList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = persistentListOf(),
    )

    val isAllSelected = allApps.map { list ->
        list.isNotEmpty() && list.all { it.isSelected }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = false,
    )

    fun changeSearchAppsStr(value: String) {
        _searchAppsStr.value = value
    }

    fun changeAppsFilter(filter: AppsFilter) {
        _appsFilter.value = filter
    }

    fun changeStateApp(app: AppDTO) {
        viewModelScope.launch { installedApps.changeStateApp(app = app) }
    }

    fun changeAllStateApps(isSelected: Boolean) {
        viewModelScope.launch { installedApps.changeAllStateApps(isSelected = isSelected) }
    }
}
