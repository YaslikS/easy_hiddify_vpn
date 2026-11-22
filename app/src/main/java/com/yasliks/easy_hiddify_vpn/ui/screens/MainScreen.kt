package com.yasliks.easy_hiddify_vpn.ui.screens

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.VpnService
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yas.database.data.ServerDTO
import com.yas.database.data.SubscriptionDTO
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.data.ServerGroup
import com.yasliks.easy_hiddify_vpn.ui.components.main.ServerItem
import com.yasliks.easy_hiddify_vpn.ui.components.main.ServerItemActions
import com.yasliks.easy_hiddify_vpn.ui.components.base.ScreenWrapper
import com.yasliks.easy_hiddify_vpn.ui.components.common.BaseHorizontalDivider
import com.yasliks.easy_hiddify_vpn.ui.components.common.listPanel
import com.yasliks.easy_hiddify_vpn.ui.components.common.listPanelColor
import com.yasliks.easy_hiddify_vpn.ui.components.main.TrafficCard
import com.yasliks.easy_hiddify_vpn.ui.components.main.VpnControlPanel
import com.yasliks.easy_hiddify_vpn.ui.components.main.SwipeActionRow
import com.yasliks.easy_hiddify_vpn.ui.components.main.subscriptionRemainingLabel
import com.yasliks.easy_hiddify_vpn.ui.components.main.rememberSubscriptionTime
import com.yasliks.easy_hiddify_vpn.vm.ServerViewModel
import com.yasliks.easy_hiddify_vpn.utils.SubscriptionDownload
import com.yasliks.hiddify_library_lib.EasyHiddify
import kotlin.math.hypot
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun MainScreen(
    serverViewModel: ServerViewModel,
    hiddify: EasyHiddify,
    onImportClick: () -> Unit,
    onRefreshSubscription: (String) -> Unit,
    onDeleteSubscription: (String) -> Unit,
    onStartServer: (config: String, serverName: String, serverId: Int) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val startServer by rememberUpdatedState(newValue = onStartServer)

    var pendingConfig by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingServerName by rememberSaveable { mutableStateOf("") }
    var pendingServerId by rememberSaveable { mutableStateOf<Int?>(null) }
    var activeServerId by rememberSaveable { mutableStateOf<Int?>(null) }

    var showServerSheet by rememberSaveable { mutableStateOf(false) }
    var serverToEdit by remember { mutableStateOf<ServerDTO?>(null) }

    val isConnected by hiddify.state.connected.collectAsStateWithLifecycle()
    val serviceActiveServerId by hiddify.state.activeServerId.collectAsStateWithLifecycle()
    val serviceActiveServerName by hiddify.state.activeServerName.collectAsStateWithLifecycle()
    val servers by serverViewModel.servers.collectAsStateWithLifecycle(initialValue = persistentListOf())
    val subscriptions by serverViewModel.subscriptions.collectAsStateWithLifecycle(initialValue = persistentListOf())
    val collapsedServerGroups by serverViewModel.collapsedServerGroups.collectAsStateWithLifecycle(
        initialValue = persistentSetOf(),
    )
    val lastServerId by serverViewModel.lastServerId.collectAsStateWithLifecycle(initialValue = 0)
    val isPinging by serverViewModel.isPinging.collectAsStateWithLifecycle()
    val pingingServerIds by serverViewModel.pingingServerIds.collectAsStateWithLifecycle()

    val effectiveSelectedId = remember(servers, lastServerId) {
        if (servers.any { it.id == lastServerId }) lastServerId else servers.firstOrNull()?.id ?: 0
    }
    val displayedServerName = remember(
        isConnected,
        serviceActiveServerId,
        serviceActiveServerName,
        activeServerId,
        effectiveSelectedId,
        servers,
    ) {
        if (isConnected) {
            servers.firstOrNull { it.id == (serviceActiveServerId ?: activeServerId) }?.name
                ?: serviceActiveServerName.takeIf { it.isNotBlank() }
        } else {
            servers.firstOrNull { it.id == effectiveSelectedId }?.name
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val density = LocalDensity.current

    val maxRadius = remember(configuration.screenWidthDp, configuration.screenHeightDp, density) {
        val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
        val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
        hypot(screenWidthPx, screenHeightPx)
    }

    var boxPositionInRoot by remember { mutableStateOf(Offset.Zero) }
    var buttonCenterInRoot by remember { mutableStateOf(Offset.Zero) }

    val animatedRadius by animateFloatAsState(
        targetValue = if (isConnected) maxRadius else 0f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "reveal"
    )

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    val mainTextColor by animateColorAsState(
        targetValue = if (isConnected) Color.White else onSurfaceColor,
        animationSpec = tween(durationMillis = 1000),
        label = "mainText",
    )
    val buttonContentColor by animateColorAsState(
        targetValue = if (isConnected) Color.White else primaryColor,
        animationSpec = tween(durationMillis = 1000),
        label = "btnContent",
    )
    val buttonBorderColor by animateColorAsState(
        targetValue = if (isConnected) Color.White.copy(alpha = 0.5f) else outlineColor,
        animationSpec = tween(durationMillis = 1000),
        label = "btnBorder",
    )

    LaunchedEffect(isConnected, serviceActiveServerId) {
        activeServerId = if (!isConnected) null else serviceActiveServerId ?: activeServerId
        ?: effectiveSelectedId
    }

    val startHiddify: () -> Unit = remember {
        {
            pendingConfig?.let { config ->
                startServer(
                    config,
                    pendingServerName,
                    pendingServerId ?: 0,
                )
            }
        }
    }

    val vpnPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) startHiddify()
        }

    val checkVpnPermissionAndStart: () -> Unit = remember(context, vpnPermissionLauncher, startHiddify) {
        {
            val intent = VpnService.prepare(context)
            if (intent != null) vpnPermissionLauncher.launch(input = intent) else startHiddify()
        }
    }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            checkVpnPermissionAndStart()
        }

    val handleStartClick: (ServerDTO) -> Unit = remember(
        context,
        serverViewModel,
        notificationPermissionLauncher,
        checkVpnPermissionAndStart,
    ) {
        { server ->
            serverViewModel.setLastServerId(id = server.id)
            pendingConfig = server.config
            pendingServerName = server.name
            pendingServerId = server.id
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(input = Manifest.permission.POST_NOTIFICATIONS)
            } else checkVpnPermissionAndStart()
        }
    }

    val emptyServersText = stringResource(id = R.string.list_servers_empty)
    val onToggleVpnClick: () -> Unit = remember(
        isConnected,
        hiddify,
        servers,
        effectiveSelectedId,
        handleStartClick,
        context,
        emptyServersText,
    ) {
        {
            if (isConnected) {
                hiddify.stopVpn()
                activeServerId = null
            } else {
                val targetServer = servers.firstOrNull { it.id == effectiveSelectedId }
                if (targetServer != null) {
                    handleStartClick(targetServer)
                } else {
                    Toast.makeText(
                        context,
                        emptyServersText,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    ScreenWrapper(showTopBar = false) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    boxPositionInRoot = coordinates.positionInRoot()
                }
                .drawBehind {
                    val centerOffset = if (buttonCenterInRoot != Offset.Zero) {
                        buttonCenterInRoot - boxPositionInRoot
                    } else {
                        Offset(size.width / 2f, size.height / 2f)
                    }
                    drawCircle(Color(0xFF4CAF50), animatedRadius, centerOffset)
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                MainScreenLayout(
                    isLandscape = isLandscape,
                    controls = {
                        VpnControlPanel(
                            isConnected = isConnected,
                            serverName = displayedServerName,
                            onToggle = onToggleVpnClick,
                            onButtonPositioned = { buttonCenterInRoot = it },
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                        )
                        TrafficSection(
                            hiddify = hiddify,
                            isConnected = isConnected,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    },
                    servers = {
                        if (isLandscape) Spacer(modifier = Modifier.height(8.dp))
                        ServersHeaderSection(
                            hasServers = servers.isNotEmpty(),
                            isPinging = isPinging,
                            mainTextColor = mainTextColor,
                            buttonBorderColor = buttonBorderColor,
                            buttonContentColor = buttonContentColor,
                            onPingClick = serverViewModel::pingAllServers,
                            onImportClick = onImportClick,
                            onAddServerClick = {
                                serverToEdit = null
                                showServerSheet = true
                            },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ServersListSection(
                            servers = servers,
                            subscriptions = subscriptions,
                            collapsedGroups = collapsedServerGroups,
                            onToggleGroup = serverViewModel::toggleServerGroup,
                            onRefreshSubscription = onRefreshSubscription,
                            onDeleteSubscription = onDeleteSubscription,
                            pingingServerIds = pingingServerIds,
                            effectiveSelectedId = effectiveSelectedId,
                            activeServerId = activeServerId,
                            isConnected = isConnected,
                            onConnectClick = { server ->
                                if (!isConnected || server.id != (serviceActiveServerId ?: activeServerId)) {
                                    handleStartClick(server)
                                }
                            },
                            onEditClick = { server ->
                                serverToEdit = server
                                showServerSheet = true
                            },
                            onCopyClick = { server ->
                                scope.launch {
                                    clipboard.setClipEntry(clipEntry = ClipEntry(ClipData.newPlainText(null, server.config)))
                                    Toast.makeText(
                                        context,
                                        R.string.config_copied,
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            },
                            onDeleteClick = { server ->
                                if (isConnected && activeServerId == server.id) {
                                    hiddify.stopVpn()
                                    activeServerId = null
                                }
                                if (server.id == effectiveSelectedId) {
                                    serverViewModel.setLastServerId(
                                        id = servers.firstOrNull { it.id != server.id }?.id ?: 0,
                                    )
                                }
                                serverViewModel.remove(server = server)
                            },
                            modifier = Modifier.weight(weight = 1f, fill = false),
                        )
                    },
                )
            }

            if (showServerSheet) {
                ServerBottomSheet(
                    serverToEdit = serverToEdit,
                    onDismiss = {
                        showServerSheet = false
                        serverToEdit = null
                    },
                    onSave = { name, config ->
                        val editedServer = serverToEdit
                        if (editedServer != null) {
                            serverViewModel.update(
                                server = editedServer.copy(
                                    name = name,
                                    config = config,
                                ),
                            )
                        } else {
                            serverViewModel.insert(config = config, name = name)
                        }
                        showServerSheet = false
                        serverToEdit = null
                    },
                )
            }
        }
    }
}

@Composable
private fun MainScreenLayout(
    isLandscape: Boolean,
    controls: @Composable () -> Unit,
    servers: @Composable ColumnScope.() -> Unit,
) {
    if (isLandscape) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(space = 24.dp),
        ) {
            Column(
                modifier = Modifier.weight(weight = 0.4f)
                    .fillMaxHeight()
                    .verticalScroll(state = rememberScrollState()),
            ) { controls() }
            Column(
                modifier = Modifier.weight(weight = 0.6f).fillMaxHeight(),
                content = servers,
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            controls()
            servers()
        }
    }
}

@Composable
private fun TrafficSection(
    hiddify: EasyHiddify,
    isConnected: Boolean
) {
    val traffic by hiddify.state.trafficStats.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TrafficCard(
            title = stringResource(R.string.download),
            speed = traffic.getDownlinkSpeed(),
            total = traffic.getDownlinkTotal(),
            icon = Icons.Rounded.ArrowDownward,
            iconTint = Color(0xFF4CAF50),
            isConnected = isConnected,
            modifier = Modifier.weight(1f)
        )
        TrafficCard(
            title = stringResource(R.string.upload),
            speed = traffic.getUplinkSpeed(),
            total = traffic.getUplinkTotal(),
            icon = Icons.Rounded.ArrowUpward,
            iconTint = Color(0xFF2196F3),
            isConnected = isConnected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ServersHeaderSection(
    hasServers: Boolean,
    isPinging: Boolean,
    mainTextColor: Color,
    buttonBorderColor: Color,
    buttonContentColor: Color,
    onPingClick: () -> Unit,
    onImportClick: () -> Unit,
    onAddServerClick: () -> Unit
) {
    var showAddMenu by remember { mutableStateOf(false) }
    val cancelPingDescription = stringResource(id = R.string.cancel)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = R.string.servers_and_subscriptions),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
                fontWeight = FontWeight.Bold,
                color = mainTextColor
            )

            if (hasServers) {
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onPingClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    if (isPinging) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp).semantics {
                                contentDescription = cancelPingDescription
                            },
                            color = mainTextColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = stringResource(R.string.cd_ping_servers),
                            tint = mainTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Box {
            OutlinedButton(
                onClick = { showAddMenu = true },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp),
                border = BorderStroke(width = 1.dp, color = buttonBorderColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = buttonContentColor),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(id = R.string.add), fontSize = 12.sp, maxLines = 1)
            }
            DropdownMenu(
                expanded = showAddMenu,
                onDismissRequest = { showAddMenu = false },
            ) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.import_menu_server)) },
                    onClick = {
                        showAddMenu = false
                        onAddServerClick()
                    },
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.import_servers)) },
                    onClick = {
                        showAddMenu = false
                        onImportClick()
                    },
                )
            }
        }
    }
}

@Composable
private fun ServersListSection(
    servers: ImmutableList<ServerDTO>,
    subscriptions: ImmutableList<SubscriptionDTO>,
    collapsedGroups: ImmutableSet<String>,
    onToggleGroup: (String) -> Unit,
    onRefreshSubscription: (String) -> Unit,
    onDeleteSubscription: (String) -> Unit,
    pingingServerIds: ImmutableSet<Int>,
    effectiveSelectedId: Int,
    activeServerId: Int?,
    isConnected: Boolean,
    onConnectClick: (ServerDTO) -> Unit,
    onEditClick: (ServerDTO) -> Unit,
    onCopyClick: (ServerDTO) -> Unit,
    onDeleteClick: (ServerDTO) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groups = remember(servers) {
        servers.groupBy { it.subscriptionUrlNotNull }.entries
            .sortedBy { it.key.isNotEmpty() }
            .map { (url, items) ->
                ServerGroup(
                    subscriptionUrl = url,
                    servers = items.toImmutableList(),
                )
            }.toImmutableList()
    }
    val titles = remember(subscriptions) {
        subscriptions.associate {
            it.url to (it.titleNotNull.takeIf { title -> title.isNotEmpty() } ?: SubscriptionDownload.fallbackTitle(url = it.url))
        }
    }
    val expirationByUrl = remember(subscriptions) {
        subscriptions.filter { it.expiresAtNotNull > 0 }.associate { it.url to it.expiresAtNotNull }
    }
    val now by rememberSubscriptionTime(active = expirationByUrl.isNotEmpty())
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
    ) {
        groups.forEachIndexed { groupIndex, serverGroup ->
            val (url, group) = serverGroup
            val groupKey = serverGroup.key
            val expanded = groupKey !in collapsedGroups
            item(key = groupKey) {
                Column(modifier = Modifier.animateItem()) {
                    if (groupIndex > 0 && groups[groupIndex - 1].key in collapsedGroups) {
                        BaseHorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = if (isConnected) Color.White.copy(alpha = 0.55f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            thickness = 1f,
                        )
                    }
                    SwipeActionRow(
                        isConnected = isConnected,
                        primaryActionIcon = Icons.Outlined.Refresh,
                        onPrimaryAction = { onRefreshSubscription(url) },
                        onDelete = { onDeleteSubscription(url) },
                        deleteTitle = stringResource(id = R.string.import_delete_subscription),
                        deleteMessage = stringResource(id = R.string.import_delete_subscription_message),
                        enabled = url.isNotEmpty(),
                        restingShape = RoundedCornerShape(12.dp),
                    ) {
                        ServerGroupHeader(
                            title = if (url.isEmpty()) {
                                stringResource(id = R.string.servers_added)
                            } else {
                                titles[url] ?: SubscriptionDownload.fallbackTitle(url = url)
                                    ?: stringResource(id = R.string.servers_subscription)
                            },
                            expiresAt = expirationByUrl[url] ?: 0L,
                            now = if (url in expirationByUrl) now else 0L,
                            expanded = expanded,
                            isConnected = isConnected,
                            onToggle = { onToggleGroup(groupKey) },
                            onRefresh = { onRefreshSubscription(url) },
                        )
                    }
                }
            }
            if (!expanded) return@forEachIndexed
            itemsIndexed(
                items = group,
                key = { _, server -> server.id },
            ) { index, server ->
                Column(
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = tween(durationMillis = 220),
                            fadeOutSpec = tween(durationMillis = 150),
                        )
                        .listPanel(
                            color = if (isConnected) Color.White.copy(alpha = 0.05f)
                            else MaterialTheme.colorScheme.listPanelColor,
                            shape = RoundedCornerShape(
                                topStart = if (index == 0) 16.dp else 0.dp,
                                topEnd = if (index == 0) 16.dp else 0.dp,
                                bottomStart = if (index == group.lastIndex) 16.dp else 0.dp,
                                bottomEnd = if (index == group.lastIndex) 16.dp else 0.dp,
                            ),
                        ),
                ) {
                    ServerItem(
                        server = server,
                        isPinging = server.id in pingingServerIds,
                        isSelected = server.id == effectiveSelectedId,
                        isCurrentConnected = isConnected && activeServerId == server.id,
                        isVpnActive = isConnected,
                        actions = ServerItemActions(
                            onConnect = { onConnectClick(server) },
                            onEdit = { onEditClick(server) },
                            onCopy = { onCopyClick(server) },
                            onDelete = { onDeleteClick(server) },
                        ),
                    )
                    if (index < group.lastIndex) BaseHorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ServerGroupHeader(
    title: String,
    expiresAt: Long,
    now: Long,
    expanded: Boolean,
    isConnected: Boolean,
    onToggle: () -> Unit,
    onRefresh: () -> Unit,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "serverGroupArrow",
    )
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                role = Role.Button,
                onClick = onToggle,
            )
            .heightIn(min = 48.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isConnected) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (expiresAt > 0) {
                Text(
                    text = subscriptionRemainingLabel(expiresAt = expiresAt, now = now),
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isConnected -> Color.White.copy(alpha = 0.85f)
                        expiresAt <= now -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                if (expiresAt <= now) {
                    TextButton(
                        onClick = onRefresh,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isConnected) Color.White else MaterialTheme.colorScheme.primary,
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = 0.dp,
                        ),
                    ) {
                        Text(text = stringResource(id = R.string.import_refresh))
                    }
                }
            }
        }
        Icon(
            imageVector = Icons.Default.ExpandMore,
            contentDescription = null,
            tint = if (isConnected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.rotate(arrowRotation),
        )
    }
}
