package com.yasliks.easy_hiddify_vpn.vm

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yas.database.data.AppDTO
import com.yas.database.repo.AppsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import javax.inject.Inject

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

    init {
        viewModelScope.launch {
            getInstalledApps()
        }
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
            appsRepo.insert(app.copy(isSelected = !app.isSelected))
        }
    }

    /**
     * Changes the status of all applications
     *
     * @param isSelected state
     */
    fun changeAllStateApps(isSelected: Boolean = true) {
        viewModelScope.launch {
            val apps = appsRepo.getAll()
            appsRepo.insert(apps.map { it.copy(isSelected = isSelected) })
        }
    }

    /**
     * If the database is empty, it saves the applications installed on the phone.
     */
    private suspend fun getInstalledApps(ignoreSavedApps: Boolean = false) {
        val savedApps = appsRepo.getAll()
        if (savedApps.isNotEmpty() && !ignoreSavedApps) return

        val packageManager = context.packageManager

        val appsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }

        val appsInPhone = appsList
            .filter {
                it.packageName.lowercase(Locale.getDefault()) != "filled"
            }
            .map { appInfo ->
                val iconDrawable = packageManager.getApplicationIcon(appInfo)
                val savedPath = saveIconToStorage(context, appInfo.packageName, iconDrawable)

                AppDTO(
                    packageName = appInfo.packageName,
                    name = packageManager.getApplicationLabel(appInfo).toString(),
                    isSelected = true,
                    iconPath = savedPath,
                )
            }

        appsRepo.insert(appsInPhone)
    }

    private fun saveIconToStorage(
        context: Context,
        packageName: String,
        drawable: Drawable,
    ): String? {
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

            val directory = File(context.filesDir, "app_icons")
            if (!directory.exists()) directory.mkdirs()

            val file = File(directory, "$packageName.png")

            FileOutputStream(file).use { out ->
                bitmap.compress(
                    /* format = */ Bitmap.CompressFormat.PNG,
                    /* quality = */ 100,
                    /* stream = */ out,
                )
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}