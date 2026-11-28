package com.yasliks.easy_hiddify_vpn.vm

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yasliks.database.data.SettingsData
import com.yasliks.hiddify_library_lib.EasyHiddify
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: DataStore<SettingsData>,
    hiddify: EasyHiddify,
) : ViewModel() {

    val hiddifySettings = hiddify.settings

    companion object {
       val logLevels = listOf("trace", "debug", "info", "warn", "error", "fatal")
    }

    val autoStartVpn = settingsDataStore.data
        .map { it.autoStartVpn }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = false,
        )

    fun setAutoStartVpn(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateData { currentSettings ->
                currentSettings.toBuilder()
                    .setAutoStartVpn(enabled)
                    .build()
            }
        }
    }

    fun setDnsServer(dns: String) = hiddifySettings.setDnsServer(dns)
    fun setMtu(mtu: Int) = hiddifySettings.setMtu(mtu)
    fun setEnableIPv6(enable: Boolean) = hiddifySettings.setEnableIPv6(enable)
    fun setBypassLan(enable: Boolean) = hiddifySettings.setBypassLan(enable)
    fun setLogLevel(level: String) = hiddifySettings.setLogLevel(level)
    fun setStrictRoute(enable: Boolean) = hiddifySettings.setStrictRoute(enable)
    fun setEnableMux(enable: Boolean) = hiddifySettings.setEnableMux(enable)
    fun setEnableLocalProxy(enable: Boolean) = hiddifySettings.setEnableLocalProxy(enable)
    fun setEnableAdBlock(enable: Boolean) = hiddifySettings.setEnableAdBlock(enable)
    fun setCustomDirectDomains(domains: String) = hiddifySettings.setCustomDirectDomains(domains)
    fun setEnableFakeIp(enable: Boolean) = hiddifySettings.setEnableFakeIp(enable)
}
