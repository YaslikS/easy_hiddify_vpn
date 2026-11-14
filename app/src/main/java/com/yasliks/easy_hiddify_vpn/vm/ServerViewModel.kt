package com.yasliks.easy_hiddify_vpn.vm

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yas.database.data.ServerDTO
import com.yas.database.repo.ServerRepo
import com.yasliks.database.data.SettingsData
import com.yasliks.easy_hiddify_vpn.utils.PingUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val serverRepo: ServerRepo,
    private val settings: DataStore<SettingsData>,
    private val pingUtils: PingUtils,
) : ViewModel() {

    val servers = serverRepo.getAllFlow()

    val lastServerId: Flow<Int> = settings.data
        .map { it.lastServerId }

    private val _isPinging = MutableStateFlow(false)
    val isPinging = _isPinging.asStateFlow()

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
                    pingMs = -1L
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

    fun pingAllServers() {
        viewModelScope.launch {
            _isPinging.value = true
            val currentServers = serverRepo.getAll()

            val semaphore = Semaphore(7)

            val updatedServers = currentServers.map { server ->
                async {
                    semaphore.withPermit {
                        val ping = pingUtils.pingConfig(server.config)
                        server.copy(pingMs = ping)
                    }
                }
            }.awaitAll()

            serverRepo.insert(updatedServers)
            _isPinging.value = false
        }
    }
}