package com.yasliks.easy_hiddify_vpn.ui.screens

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.ui.components.base.ScreenWrapper
import com.yasliks.easy_hiddify_vpn.ui.components.base.TopBar
import com.yasliks.hiddify_library_lib.EasyHiddify
import com.yasliks.hiddify_library_lib.model.LogItem

@Composable
fun LogScreen(
    hiddify: EasyHiddify,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val logs by hiddify.logger.logs.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (logs.size - 1).coerceAtLeast(0)
    )

    val launchState = remember { object { var isInitial = true } }

    val isNearBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty() || layoutInfo.totalItemsCount == 0) {
                true
            } else {
                val lastVisibleIndex = visibleItems.last().index
                lastVisibleIndex >= layoutInfo.totalItemsCount - 4
            }
        }
    }

    LaunchedEffect(logs.lastOrNull()?.id) {
        if (logs.isNotEmpty()) {
            if (launchState.isInitial) {
                launchState.isInitial = false
                listState.scrollToItem(logs.size)
            } else if (isNearBottom) {
                listState.animateScrollToItem(logs.size)
            }
        }
    }

    @SuppressLint("LocalContextGetResourceValueCall")
    val shareLogsAction = remember(context, hiddify) {
        {
            val currentLogs = hiddify.logger.logs.value
            if (currentLogs.isNotEmpty()) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        context.getString(R.string.logs_clip_label),
                        currentLogs.joinToString("\n") { it.message }
                    )
                )
                Toast.makeText(context, context.getString(R.string.logs_copied), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, context.getString(R.string.logs_empty), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val currentOnBack by rememberUpdatedState(onBack)
    val navIcon: @Composable () -> Unit = remember {
        {
            IconButton(onClick = currentOnBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back)
                )
            }
        }
    }

    ScreenWrapper(
        title = stringResource(R.string.logs_title, logs.size),
        showTopBar = !isLandscape,
        navigationIcon = navIcon,
        actions = {
            ShareLogsButton(
                onClick = shareLogsAction,
                modifier = Modifier.padding(end = 16.dp)
            )
        }
    ) { innerPadding ->
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight()
                ) {
                    TopBar(
                        title = stringResource(R.string.logs_title, logs.size),
                        navigationIcon = navIcon
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ShareLogsButton(
                        onClick = shareLogsAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                LogsTerminalSection(
                    logs = logs,
                    listState = listState,
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                )
            }
        } else {
            LogsTerminalSection(
                logs = logs,
                listState = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
            )
        }
    }
}

@Composable
private fun ShareLogsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.share_logs), fontSize = 11.sp)
    }
}

@Composable
private fun LogsTerminalSection(
    logs: List<LogItem>,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(20.dp)
    ) {
        SelectionContainer {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp)
            ) {
                items(items = logs, key = { it.id }) { entry ->
                    LogEntryRow(entry = entry)
                }

                item(key = "bottom_anchor") {
                    Spacer(modifier = Modifier.height(1.dp))
                }
            }
        }
    }
}

@Composable
private fun LogEntryRow(entry: LogItem) {
    val textColor = remember(entry.level) {
        when (entry.level) {
            4 -> Color(0xFFFF5252)
            3 -> Color(0xFFFFD740)
            else -> Color.LightGray
        }
    }

    Text(
        text = entry.message,
        color = textColor,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}