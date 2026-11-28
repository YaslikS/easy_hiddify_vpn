package com.yasliks.easy_hiddify_vpn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yasliks.easy_hiddify_vpn.BuildConfig
import com.yasliks.easy_hiddify_vpn.R

@Composable
internal fun DebugServerPresets(onSelect: (String, String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val vlessName = stringResource(id = R.string.debug_vless_name)
        val ssName = stringResource(id = R.string.debug_ss_name)
        val trojanName = stringResource(id = R.string.debug_trojan_name)

        OutlinedButton(
            modifier = Modifier.weight(weight = 1f),
            onClick = { onSelect(vlessName, BuildConfig.vless_serv) },
        ) {
            Text(text = stringResource(id = R.string.debug_vless))
        }
        OutlinedButton(
            modifier = Modifier.weight(weight = 1f),
            onClick = { onSelect(ssName, BuildConfig.ss_serv) },
        ) {
            Text(text = stringResource(id = R.string.debug_ss))
        }
        OutlinedButton(
            modifier = Modifier.weight(weight = 1f),
            onClick = { onSelect(trojanName, BuildConfig.trojan) },
        ) {
            Text(text = stringResource(id = R.string.debug_trojan))
        }
    }
}
