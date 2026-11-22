package com.yasliks.easy_hiddify_vpn.ui.components.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.res.stringResource
import com.yasliks.easy_hiddify_vpn.R
import kotlinx.coroutines.delay

@Composable
internal fun rememberSubscriptionTime(active: Boolean = true): State<Long> = produceState(
    initialValue = System.currentTimeMillis() / 1000,
    key1 = active,
) {
    while (active) {
        value = System.currentTimeMillis() / 1000
        delay(timeMillis = 30_000)
    }
}

@Composable
internal fun subscriptionRemainingLabel(
    expiresAt: Long,
    now: Long,
): String {
    if (expiresAt <= now) return stringResource(id = R.string.import_expired)
    val minutes = (expiresAt - now + 59) / 60
    return stringResource(
        id = R.string.import_expiry_remaining,
        formatArgs = arrayOf(minutes / 1440, minutes / 60 % 24, minutes % 60),
    )
}
