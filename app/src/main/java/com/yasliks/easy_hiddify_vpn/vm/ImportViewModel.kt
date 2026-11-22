package com.yasliks.easy_hiddify_vpn.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yas.database.data.SubscriptionDTO
import com.yas.database.repo.ServerRepo
import com.yasliks.easy_hiddify_vpn.utils.ImportKind
import com.yasliks.easy_hiddify_vpn.utils.ImportParser
import com.yasliks.easy_hiddify_vpn.utils.SubscriptionDownload
import com.yasliks.hiddify_library_lib.EasyHiddify
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val servers: ServerRepo,
) : ViewModel() {
    internal data class State(
        val input: String = "",
        val kind: ImportKind = ImportKind.AUTO,
        val busy: Boolean = false,
        val error: Boolean = false,
        val subscription: SubscriptionDTO? = null,
        val preview: ImportParser.Preview? = null,
    )

    private val _state = MutableStateFlow<State?>(null)
    internal val state = _state.asStateFlow()
    internal val subscriptions = servers.subscriptions()

    internal fun open(
        input: String = "",
        kind: ImportKind = ImportKind.AUTO,
    ) {
        if (_state.value?.busy != true) {
            _state.value = State(
                input = input,
                kind = kind,
            )
        }
    }

    internal fun close() {
        if (_state.value?.busy != true) _state.value = null
    }

    internal fun preview() = perform { current ->
        val content = if (current.kind == ImportKind.SUBSCRIPTION ||
            (current.kind == ImportKind.AUTO && current.input.trim().startsWith(prefix = "https://", ignoreCase = true))
        ) {
            SubscriptionDownload.fetch(source = SubscriptionDownload.parseUrl(input = current.input))
        } else null
        val preview = withContext(Dispatchers.Default) {
            ImportParser.parse(input = content?.text ?: current.input)
        }
        require(preview.servers.isNotEmpty())
        require(current.kind != ImportKind.SERVER || (preview.servers.size == 1 && preview.skipped == 0))
        current.copy(
            subscription = content?.subscription,
            preview = preview,
            busy = false,
        )
    }

    internal fun save() = perform { current ->
        servers.saveImport(
            items = requireNotNull(current.preview).servers,
            subscription = current.subscription,
        )
        null
    }

    internal fun deleteSubscription(url: String) = perform { current ->
        removeSubscription(url = url)
        current.copy(busy = false)
    }

    internal fun refreshSubscription(url: String) {
        if (_state.value?.busy == true) return
        open(
            input = url,
            kind = ImportKind.SUBSCRIPTION,
        )
        preview()
    }

    internal fun deleteSubscriptionFromMain(
        url: String,
        onError: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                removeSubscription(url = url)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                onError()
            }
        }
    }

    private suspend fun removeSubscription(url: String) {
        val hiddify = EasyHiddify.instance
        if (servers.getBySource(subscriptionUrl = url).any { it.id == hiddify.state.activeServerId.value }) {
            hiddify.stopVpn()
        }
        servers.deleteSubscription(url = url)
    }

    private fun perform(action: suspend (State) -> State?) {
        val current = _state.value ?: return
        if (current.busy) return
        _state.value = current.copy(busy = true, error = false)
        viewModelScope.launch {
            try {
                _state.value = action(current.copy(error = false))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.value = current.copy(busy = false, error = true)
            }
        }
    }
}
