package com.yasliks.easy_hiddify_vpn.vm

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yas.database.data.ServerDTO
import com.yas.database.repo.ServerRepo
import com.yasliks.database.data.SettingsData
import com.yasliks.easy_hiddify_vpn.utils.LatencyStatus
import com.yasliks.hiddify_library_lib.EasyHiddify
import com.yasliks.hiddify_library_lib.utils.HiddifyLatencyTest
import com.yasliks.hiddify_library_lib.utils.LatencyResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    private val serverRepo: ServerRepo,
    private val settings: DataStore<SettingsData>,
) : ViewModel() {

    val servers = serverRepo.getAllFlow()
    val subscriptions = serverRepo.subscriptions()

    val lastServerId: Flow<Int> = settings.data
        .map { it.lastServerId }

    private val _isPinging = MutableStateFlow(false)
    val isPinging = _isPinging.asStateFlow()
    private val _pingingServerIds = MutableStateFlow<PersistentSet<Int>>(persistentSetOf())
    val pingingServerIds = _pingingServerIds.asStateFlow()
    private var pingJob: Job? = null

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

    fun insert(
        config: String,
        name: String,
    ) {
        viewModelScope.launch {
            serverRepo.insert(
                data = ServerDTO(
                    config = config,
                    name = name,
                ),
            )
        }
    }

    fun insert(servers: List<ServerDTO>) {
        viewModelScope.launch {
            serverRepo.insert(items = servers)
        }
    }

    fun update(server: ServerDTO) {
        viewModelScope.launch {
            serverRepo.insert(data = server)
        }
    }

    fun remove(server: ServerDTO) {
        viewModelScope.launch {
            serverRepo.remove(data = server)
        }
    }

    fun removeAll() {
        viewModelScope.launch {
            serverRepo.removeAll()
        }
    }

    fun pingAllServers() {
        pingJob?.takeIf { !it.isCompleted }?.let {
            it.cancel()
            return
        }
        pingJob = viewModelScope.launch {
            _isPinging.value = true
            try {
                serverRepo.clearPing()
                val (jsonServers, uriServers) = serverRepo.getAll()
                    .partition { it.config.trimStart().startsWith(char = '{') }
                for (batch in uriServers.chunked(size = HiddifyLatencyTest.MAX_BATCH_SIZE)) {
                    checkBatch(servers = batch)
                }
                for (server in jsonServers) {
                    checkBatch(servers = listOf(element = server))
                }
            } finally {
                _pingingServerIds.value = persistentSetOf()
                _isPinging.value = false
            }
        }
    }

    private suspend fun checkBatch(servers: List<ServerDTO>) {
        _pingingServerIds.value = servers.map { it.id }.toPersistentSet()
        EasyHiddify.instance.latency.measureBatch(
            configs = servers.map { it.config },
            onResult = { index, result ->
                val server = servers[index]
                serverRepo.updatePing(
                    id = server.id,
                    config = server.config,
                    pingMs = when (result) {
                        is LatencyResult.Reachable -> result.milliseconds.toLong()
                        LatencyResult.NoResponse -> LatencyStatus.NO_RESPONSE
                        LatencyResult.Failed -> LatencyStatus.FAILED
                    },
                )
                _pingingServerIds.value = _pingingServerIds.value.removing(element = server.id)
            },
        )
    }
}
