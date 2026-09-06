package com.yasliks.easy_hiddify_vpn.vm

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.yas.database.data.ServerDTO
import com.yas.database.repo.ServerRepo
import com.yasliks.database.data.SettingsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


@HiltViewModel
class ServerViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val serverRepo: ServerRepo,
    private val settings: DataStore<SettingsData>,
) : ViewModel() {

    val servers = serverRepo.getAllFlow()

    val lastServerId: Flow<Int> = settings.data
        .map { it.lastServerId }

    fun setLastServerId(id: Int) {
        viewModelScope.launch {
            settings.updateData { currentSettings ->
                currentSettings.toBuilder()
                    .setLastServerId(id)
                    .build()
            }
        }
    }

    suspend fun getAllServers(): List<ServerDTO> {
        return serverRepo.getAll()
    }

    fun insert(config: String, name: String) {
        viewModelScope.launch {
            serverRepo.insert(
                ServerDTO(
                    id = 0,
                    config = config,
                    name = name,
                )
            )
        }
    }

    fun insert(servers: List<ServerDTO>) {
        viewModelScope.launch {
            serverRepo.insert(servers)
        }
    }

    fun update(server: ServerDTO) {
        viewModelScope.launch {
            serverRepo.insert(listOf(server))
        }
    }

    fun remove(server: ServerDTO) {
        viewModelScope.launch {
            serverRepo.remove(server)
        }
    }

    fun removeAll() {
        viewModelScope.launch {
            serverRepo.removeAll()
        }
    }
}