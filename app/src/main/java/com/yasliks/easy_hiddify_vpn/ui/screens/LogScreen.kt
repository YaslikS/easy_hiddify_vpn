package com.yasliks.easy_hiddify_vpn.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yasliks.easy_hiddify_vpn.ui.components.base.ScreenWrapper
import com.yasliks.hiddify_library_lib.EasyHiddify

@Composable
fun LogScreen(hiddify: EasyHiddify, onBack: () -> Unit) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val logs by hiddify.logger.logs.collectAsState()

    val isAtBottom by remember {
        derivedStateOf {
            val l = listState.layoutInfo.visibleItemsInfo
            l.isEmpty() || l.last().index == listState.layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty() && isAtBottom) listState.animateScrollToItem(logs.size - 1)
    }

    ScreenWrapper(
        title = "Logs (${logs.size})", showTopBar = true,
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
        actions = {
            OutlinedButton(
                onClick = {
                    if (logs.isNotEmpty()) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Logs", logs.joinToString("\n") { it.message }))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Share Logs", fontSize = 11.sp)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(20.dp)
            ) {
                SelectionContainer {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(logs, key = { it.id }) { entry ->
                            Text(
                                text = entry.message,
                                color = when (entry.level) {
                                    4 -> Color(0xFFFF5252)
                                    3 -> Color(0xFFFFD740)
                                    else -> Color.LightGray
                                },
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}