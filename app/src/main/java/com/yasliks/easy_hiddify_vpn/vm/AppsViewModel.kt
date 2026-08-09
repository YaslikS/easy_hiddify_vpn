package com.yasliks.easy_hiddify_vpn.vm

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.toLowerCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yas.database.data.AppDTO
import com.yas.database.repo.AppsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import javax.inject.Inject
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.flow.Flow
import java.util.Locale


@HiltViewModel
class AppsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appsRepo: AppsRepo,
) : ViewModel() {

    var config by mutableStateOf("")
        private set

    private val _searchAppsStr = MutableStateFlow("")
    val searchAppsStr: StateFlow<String> = _searchAppsStr

    val apps = appsRepo.getAllFlow().combine(_searchAppsStr) { list, query ->
        if (query.isNotEmpty()) {
            list.filter { app ->
                app.name.contains(query, ignoreCase = true) ||
                        app.packageName.contains(query, ignoreCase = true)
            }
        } else {
            list
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
            for (app in apps) {
                appsRepo.insert(app.copy(isSelected = isSelected))
            }
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
        drawable: Drawable
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