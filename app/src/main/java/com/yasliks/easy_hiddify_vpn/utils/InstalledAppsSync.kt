package com.yasliks.easy_hiddify_vpn.utils

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
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.yas.database.data.AppDTO
import com.yas.database.repo.AppsRepo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

@Singleton
class InstalledAppsSync @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appsRepo: AppsRepo,
) {
    private val appsMutex = Mutex()
    private val appChanges = Channel<Unit>(capacity = Channel.CONFLATED)
    private val initialSync: Deferred<Unit>
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
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        initialSync = scope.async {
            appsMutex.withLock { syncInstalledApps() }
        }
        scope.launch {
            initialSync.await()
            for (change in appChanges) {
                appsMutex.withLock { syncInstalledApps() }
            }
        }
    }

    suspend fun selectedPackages(): List<String> {
        initialSync.await()
        return appsMutex.withLock {
            appsRepo.getAll().filter { it.isSelected }.map { it.packageName }
        }
    }

    /**
     * Changes the status of the VPN application
     */
    suspend fun changeStateApp(app: AppDTO) {
        initialSync.await()
        appsMutex.withLock {
            val savedApp = appsRepo.getAll().firstOrNull { it.id == app.id }
                ?: return@withLock
            appsRepo.insert(data = savedApp.copy(isSelected = !savedApp.isSelected))
        }
    }

    /**
     * Changes the status of all applications
     *
     * @param isSelected state
     */
    suspend fun changeAllStateApps(isSelected: Boolean) {
        initialSync.await()
        appsMutex.withLock {
            appsRepo.insert(items = appsRepo.getAll().map { it.copy(isSelected = isSelected) })
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
            Log.w("InstalledAppsSync", "Failed to save app icon ${file.name}", error)
            null
        }
    }

}
