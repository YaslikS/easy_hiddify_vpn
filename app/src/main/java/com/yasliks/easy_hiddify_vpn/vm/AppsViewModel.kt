package com.yasliks.easy_hiddify_vpn.vm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AtomicFile
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yas.database.data.AppDTO
import com.yas.database.repo.AppsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

enum class AppsFilter {
    ALL,
    ENABLED,
    DISABLED,
}

@HiltViewModel
class AppsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appsRepo: AppsRepo,
) : ViewModel() {

    var config by mutableStateOf("")
        private set

    private val _searchAppsStr = MutableStateFlow("")
    val searchAppsStr: StateFlow<String> = _searchAppsStr.asStateFlow()

    private val _appsFilter = MutableStateFlow(AppsFilter.ALL)
    val appsFilter: StateFlow<AppsFilter> = _appsFilter.asStateFlow()

    val apps = combine(
        appsRepo.getAllFlow(),
        _searchAppsStr,
        _appsFilter,
    ) { list, query, filter ->
        val filteredByState = when (filter) {
            AppsFilter.ALL -> list
            AppsFilter.ENABLED -> list.filter { it.isSelected }
            AppsFilter.DISABLED -> list.filter { !it.isSelected }
        }

        if (query.isNotEmpty()) {
            filteredByState.filter { app ->
                app.name.contains(query, ignoreCase = true) ||
                        app.packageName.contains(query, ignoreCase = true)
            }
        } else {
            filteredByState
        }
    }

    val selectedApps = appsRepo.getAllFlow().map { appRoom ->
        appRoom.filter {
            it.isSelected
        }.map {
            it.packageName
        }
    }

    val isAllSelected: Flow<Boolean> = appsRepo.getAllFlow().map { allApps ->
        allApps.isNotEmpty() && allApps.all { app -> app.isSelected }
    }

    private val appChanges = Channel<Unit>(capacity = Channel.CONFLATED)
    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent,
        ) {
            if (intent.action == Intent.ACTION_PACKAGE_REMOVED &&
                intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
            ) return
            appChanges.trySend(element = Unit)
        }
    }

    init {
        ContextCompat.registerReceiver(
            context,
            packageReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addDataScheme("package")
            },
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        appChanges.trySend(Unit)
        viewModelScope.launch(Dispatchers.IO) {
            for (change in appChanges) {
                appsMutex.withLock { syncInstalledApps() }
            }
        }
    }

    override fun onCleared() {
        context.unregisterReceiver(packageReceiver)
        appChanges.close()
        super.onCleared()
    }

    fun changeConfig(value: String) {
        config = value
    }

    fun changeSearchAppsStr(value: String) {
        _searchAppsStr.value = value
    }

    fun changeAppsFilter(filter: AppsFilter) {
        _appsFilter.value = filter
    }

    /**
     * Changes the status of the VPN application
     */
    fun changeStateApp(app: AppDTO) {
        viewModelScope.launch {
            appsMutex.withLock {
                val savedApp = appsRepo.getAll().firstOrNull { it.id == app.id }
                    ?: return@withLock
                appsRepo.insert(data = savedApp.copy(isSelected = !savedApp.isSelected))
            }
        }
    }

    /**
     * Changes the status of all applications
     *
     * @param isSelected state
     */
    fun changeAllStateApps(isSelected: Boolean = true) {
        viewModelScope.launch {
            appsMutex.withLock {
                appsRepo.insert(items = appsRepo.getAll().map { it.copy(isSelected = isSelected) })
            }
        }
    }

    private suspend fun syncInstalledApps() {
        val savedApps = appsRepo.getAll()
        val savedByPackage = savedApps.associateBy { it.packageName }
        val packageManager = context.packageManager

        val appsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }

        val installedApps = appsList.filterNot { it.packageName.equals(other = "filled", ignoreCase = true) }
        val installedPackages = installedApps.mapTo(HashSet()) { it.packageName }
        val iconDirectory = File(context.filesDir, "app_icons")
        val changedApps = installedApps.mapNotNull { appInfo ->
            coroutineContext.ensureActive()
            val savedApp = savedByPackage[appInfo.packageName]
            val icon = File(iconDirectory, "${appInfo.packageName}.png")
            val iconPath = if (icon.isFile && icon.length() > 0) {
                icon.absolutePath
            } else {
                saveIconToStorage(
                    file = icon,
                    drawable = packageManager.getApplicationIcon(appInfo),
                )
            }
            val name = packageManager.getApplicationLabel(appInfo).toString()
            val updated = savedApp?.copy(
                name = name,
                iconPath = iconPath,
            ) ?: AppDTO(
                packageName = appInfo.packageName,
                name = name,
                isSelected = true,
                iconPath = iconPath,
            )
            updated.takeUnless { it == savedApp }
        }

        appsRepo.syncInstalledApps(
            removed = savedApps.filterNot { it.packageName in installedPackages },
            changed = changedApps,
        )
        iconDirectory.listFiles()?.forEach { file ->
            val name = file.name.removeSuffix(suffix = ".new").removeSuffix(suffix = ".bak")
            if (name.endsWith(suffix = ".png") &&
                name.removeSuffix(suffix = ".png") !in installedPackages
            ) {
                file.delete()
            }
        }
    }

    private fun saveIconToStorage(
        file: File,
        drawable: Drawable,
    ): String? {
        val atomicFile = AtomicFile(file)
        return try {
            val bitmap = createBitmap(
                width = drawable.intrinsicWidth.coerceAtLeast(1),
                height = drawable.intrinsicHeight.coerceAtLeast(1),
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(
                /* left = */ 0,
                /* top = */ 0,
                /* right = */ canvas.width,
                /* bottom = */ canvas.height,
            )
            drawable.draw(canvas)

            val output = atomicFile.startWrite()
            try {
                if (!bitmap.compress(
                    /* format = */ Bitmap.CompressFormat.PNG,
                    /* quality = */ 100,
                    /* stream = */ output,
                )) {
                    throw IOException("Failed to encode the app icon as PNG.")
                }
                atomicFile.finishWrite(output)
            } catch (error: Throwable) {
                atomicFile.failWrite(output)
                throw error
            }

            file.absolutePath
        } catch (error: IOException) {
            Log.w("AppsViewModel", "Failed to save app icon ${file.name}", error)
            null
        }
    }

    private companion object {
        val appsMutex = Mutex()
    }
}
