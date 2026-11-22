package com.yasliks.easy_hiddify_vpn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.ui.components.main.ServerItem
import com.yasliks.easy_hiddify_vpn.ui.components.common.DeleteConfirmationDialog
import com.yasliks.easy_hiddify_vpn.ui.components.common.dividedItems
import com.yasliks.easy_hiddify_vpn.ui.components.common.listPanel
import com.yasliks.easy_hiddify_vpn.ui.components.common.listPanelColor
import com.yasliks.easy_hiddify_vpn.ui.components.main.subscriptionRemainingLabel
import com.yasliks.easy_hiddify_vpn.ui.components.main.rememberSubscriptionTime
import com.yasliks.easy_hiddify_vpn.vm.ImportViewModel
import com.yasliks.easy_hiddify_vpn.utils.SubscriptionDownload
import kotlinx.collections.immutable.persistentListOf
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImportSheet(viewModel: ImportViewModel) {
    val dialog by viewModel.state.collectAsStateWithLifecycle()
    val state = dialog ?: return
    val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle(initialValue = persistentListOf())
    val preview = state.preview
    val busy by rememberUpdatedState(newValue = state.busy)
    val focusManager = LocalFocusManager.current
    var pendingDeletion by rememberSaveable { mutableStateOf<String?>(null) }
    val now by rememberSubscriptionTime()

    ModalBottomSheet(
        onDismissRequest = viewModel::close,
        sheetGesturesEnabled = preview == null && subscriptions.isEmpty() && !busy,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { !busy || it != SheetValue.Hidden },
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(0.9f).imePadding().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.import_servers),
                style = MaterialTheme.typography.titleLarge,
            )
            if (preview == null) {
                OutlinedTextField(
                    value = state.input,
                    onValueChange = { viewModel.open(input = it, kind = state.kind) },
                    label = { Text(text = stringResource(id = R.string.import_input)) },
                    enabled = !state.busy,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(text = stringResource(R.string.import_summary, preview.servers.size, preview.skipped))
                state.subscription?.let {
                    SubscriptionExpiry(
                        expiresAt = it.expiresAtNotNull,
                        now = now,
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .listPanel(color = MaterialTheme.colorScheme.listPanelColor),
            ) {
                if (preview != null) {
                    dividedItems(
                        items = preview.servers,
                        key = { it.config },
                    ) { server ->
                        ServerItem(server = server)
                    }
                } else if (subscriptions.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(id = R.string.import_subscriptions),
                            modifier = Modifier.padding(all = 12.dp),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    dividedItems(
                        items = subscriptions,
                        key = { it.url },
                    ) { subscription ->
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                            Text(
                                text = subscription.titleNotNull.takeIf { it.isNotEmpty() }
                                    ?: SubscriptionDownload.fallbackTitle(url = subscription.url)
                                    ?: stringResource(id = R.string.servers_subscription),
                                maxLines = 2,
                            )
                            SubscriptionExpiry(
                                expiresAt = subscription.expiresAtNotNull,
                                now = now,
                            )
                            Row {
                                TextButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.refreshSubscription(url = subscription.url)
                                    },
                                    enabled = !state.busy,
                                ) { Text(text = stringResource(id = R.string.import_refresh)) }
                                TextButton(
                                    onClick = { pendingDeletion = subscription.url },
                                    enabled = !state.busy,
                                ) { Text(text = stringResource(id = R.string.delete)) }
                            }
                        }
                    }
                }
            }
            if (state.busy) Text(text = stringResource(id = R.string.import_working))
            if (state.error) {
                Text(text = stringResource(id = R.string.import_error), color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = viewModel::close, enabled = !state.busy) {
                    Text(text = stringResource(id = R.string.import_close))
                }
                TextButton(
                    onClick = {
                        focusManager.clearFocus()
                        if (preview == null) viewModel.preview() else viewModel.save()
                    },
                    enabled = !state.busy && state.input.isNotBlank(),
                ) {
                    Text(text = stringResource(id = if (preview == null) R.string.import_preview else R.string.save))
                }
            }
        }
    }

    pendingDeletion?.let { url ->
        DeleteConfirmationDialog(
            title = stringResource(id = R.string.import_delete_subscription),
            message = stringResource(id = R.string.import_delete_subscription_message),
            onDismiss = { pendingDeletion = null },
            onConfirm = {
                pendingDeletion = null
                viewModel.deleteSubscription(url = url)
            },
        )
    }
}

@Composable
private fun SubscriptionExpiry(
    expiresAt: Long,
    now: Long,
) {
    if (expiresAt <= 0) {
        Text(text = stringResource(id = R.string.import_expiry_unknown), style = MaterialTheme.typography.bodySmall)
        return
    }
    Text(
        text = stringResource(
            R.string.import_expiry_date,
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(expiresAt * 1000)),
        ),
        style = MaterialTheme.typography.bodySmall,
    )
    Text(
        text = subscriptionRemainingLabel(expiresAt = expiresAt, now = now),
        style = MaterialTheme.typography.bodySmall,
    )
}
