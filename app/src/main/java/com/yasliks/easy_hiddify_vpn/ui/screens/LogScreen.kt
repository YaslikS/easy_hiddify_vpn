package com.yasliks.easy_hiddify_vpn.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yasliks.easy_hiddify_vpn.ui.components.base.ScreenWrapper
import com.yasliks.hiddify_library_lib.EasyHiddify

@Composable
fun LogScreen(
    hiddify: EasyHiddify,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val logs by hiddify.logger.logs.collectAsState()

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) {
                true
            } else {
                val lastVisibleItem = visibleItemsInfo.last()
                lastVisibleItem.index == layoutInfo.totalItemsCount - 1
            }
        }
    }

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty() && isAtBottom) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    ScreenWrapper(
        title = "Logs (${logs.size})",
        showTopBar = true,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            OutlinedButton(
                onClick = {
                    if (logs.isNotEmpty()) {
                        val fullLogs = logs.joinToString("\n") { it.message }
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Hiddify Logs", fullLogs)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Logs copied to clipboard", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(context, "Log list is empty", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Logs",
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Share Logs", fontSize = 11.sp)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                color = Color.Black,
                shape = MaterialTheme.shapes.small
            ) {
                SelectionContainer {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(
                            items = logs,
                            key = { it.id },
                        ) { entry ->
                            Text(
                                text = entry.message,
                                color = when (entry.level) {
                                    4 -> Color.Red
                                    3 -> Color.Yellow
                                    else -> Color.LightGray
                                },
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}