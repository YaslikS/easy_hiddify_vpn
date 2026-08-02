package com.yasliks.hiddify_library_lib.core

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.IpPrefix
import android.net.VpnService
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.annotation.RequiresApi
import com.hiddify.core.libbox.*
import com.yasliks.easy_hiddify_lib.R
import com.yasliks.hiddify_library_lib.EasyHiddify
import com.yasliks.hiddify_library_lib.prefs.HiddifyPrefs
import java.net.InetAddress
import java.net.InetSocketAddress

class HiddifyPlatform(
    private val service: VpnService,
    private val isEnabledApps: Boolean = false,
    private val appsList: List<String> = emptyList(),
) : PlatformInterface {

    private val sdk get() = EasyHiddify.instance

    override fun openTun(options: TunOptions): Int {
        Log.d(
            /* tag = */ HiddifyPrefs.HIDDIFY,
            /* msg = */ service.getString(R.string.opentun_called_core),
        )
        val builder = service.Builder()
        if (!isEnabledApps || appsList.isEmpty()) {
            try {
                // Exclude the application itself (and all its processes, including :vpn) from the tunnel
                builder.addDisallowedApplication(service.packageName)
            } catch (e: Exception) {
                Log.e(
                    /* tag = */ HiddifyPrefs.HIDDIFY,
                    /* msg = */ service.getString(R.string.failed_to_disallow, e.message ?: ""),
                )
            }
        }

        builder.setSession(HiddifyPrefs.HIDDIFY_SMALL)
        builder.setMtu(if (options.mtu > 0) options.mtu else HiddifyPrefs.BASE_MTU)

        // Setting up IP addresses
        val inet4 = options.inet4Address
        while (inet4.hasNext()) {
            val addr = inet4.next()
            builder.addAddress(addr.address(), addr.prefix())
        }

        val inet6 = options.inet6Address
        while (inet6.hasNext()) {
            val addr = inet6.next()
            builder.addAddress(addr.address(), addr.prefix())
        }

        // DNS Configuration
        val dns = options.dnsServerAddress
        if (dns != null && dns.value.isNotEmpty()) {
            builder.addDnsServer(dns.value)
        } else {
            builder.addDnsServer(HiddifyPrefs.BASE_DNS_SERVER)
        }

        // Routing
        if (options.autoRoute) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val inet4RouteAddress = options.inet4RouteAddress
                if (inet4RouteAddress.hasNext()) {
                    while (inet4RouteAddress.hasNext()) {
                        builder.addRoute(inet4RouteAddress.next().toIpPrefix())
                    }
                } else {
                    builder.addRoute(HiddifyPrefs.BASE_ROUTE, 0)
                }

                val inet6RouteAddress = options.inet6RouteAddress
                if (inet6RouteAddress.hasNext()) {
                    while (inet6RouteAddress.hasNext()) {
                        builder.addRoute(inet6RouteAddress.next().toIpPrefix())
                    }
                } else {
                    builder.addRoute("::", 0)
                }
            } else {
                builder.addRoute(HiddifyPrefs.BASE_ROUTE, 0)
                builder.addRoute(HiddifyPrefs.ZERO_ROUTE, 0)
            }

            Log.d(
                /* tag = */ HiddifyPrefs.HIDDIFY,
                /* msg = */ "isEnabledApps = $isEnabledApps, appsList == $appsList",
            )
            builder.enabledApps(
                isEnabledApps = isEnabledApps,
                listApps = appsList,
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        val pfd = builder.establish() ?: error(
            service.getString(R.string.vpn_prepare_required),
        )
        return pfd.fd
    }

    /**
     * Extends VPN operation to a list of tunneling applications
     *
     * @param listApps список приложения для туннелирования
     * @param isEnabledApps включить список приложения для туннелирования
     */
    private fun VpnService.Builder.enabledApps(
        isEnabledApps: Boolean = true,
        listApps: List<String>,
    ): VpnService.Builder {
        if (!isEnabledApps || listApps.isEmpty()) {
            return this
        }
        Log.d(
            /* tag = */ HiddifyPrefs.HIDDIFY,
            /* msg = */ "isEnabledApps -> start",
        )
        try {
            val addedApps = HashSet<String>()
            for (app in listApps) {
                if (app.isBlank() || app in addedApps) continue
                if (app == service.packageName) continue

                if (appIsExists(app)) {
                    try {
                        Log.d(
                            /* tag = */ HiddifyPrefs.HIDDIFY,
                            /* msg = */ "enabledApps -> app = $app",
                        )
                        this.addAllowedApplication(app)
                        addedApps.add(app)
                    } catch (e: Exception) {
                        // Пакет может быть удален в процессе или не подходить
                        // The package may be deleted in the process or may not be suitable.

                    }
                }
            }
        } catch (e: Exception) {

        }

        return this
    }

    /**
     * Проверяет установлено ли приложение на устройстве
     *
     * @param packageName Название пакета
     * @return true - если приложение установлено, false - в остальных случаях
     */
    private fun appIsExists(packageName: String): Boolean {
        try {
            val ctx = service
            ctx.packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            // The app isn't installed.
            return false
        }
        return true
    }

    /**
     * Processing notifications from the core
     *
     * @param notification notification
     */
    override fun sendNotification(notification: Notification) {
        sdk.state.updateNotification(notification)

        val systemNotification = sdk.notifications.createNotification(
            serverName = null,
            notification = notification,
        )

        val manager = service.getSystemService(
            /* name = */ Context.NOTIFICATION_SERVICE,
        ) as NotificationManager
        manager.notify(
            /* id = */ HiddifyPrefs.NOTIFICATION_ID,
            /* notification = */ systemNotification,
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun RoutePrefix.toIpPrefix() = IpPrefix(
        /* address = */ InetAddress.getByName(address()),
        /* prefixLength = */ prefix(),
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun findConnectionOwner(
        ipProtocol: Int,
        sourceAddress: String,
        sourcePort: Int,
        destinationAddress: String,
        destinationPort: Int,
    ): ConnectionOwner {
        return try {
            val manager = service.getSystemService(
                /* name = */ Context.CONNECTIVITY_SERVICE,
            ) as ConnectivityManager
            val uid = manager.getConnectionOwnerUid(
                /* protocol = */ ipProtocol,
                /* local = */ InetSocketAddress(sourceAddress, sourcePort),
                /* remote = */ InetSocketAddress(destinationAddress, destinationPort),
            )

            val owner = ConnectionOwner()
            owner.userId = uid
            if (uid != Process.INVALID_UID) {
                val packageName = service.packageManager.getPackagesForUid(
                    /* uid = */ uid,
                )?.firstOrNull() ?: ""
                owner.androidPackageName = packageName
                owner.userName = packageName
            }
            owner
        } catch (e: Exception) {
            ConnectionOwner()
        }
    }

    override fun readWIFIState(): WIFIState = WIFIState("", "")
    override fun clearDNSCache() {}
    override fun getInterfaces(): NetworkInterfaceIterator? = null
    override fun includeAllNetworks(): Boolean = true
    override fun localDNSTransport(): LocalDNSTransport? = null
    override fun startDefaultInterfaceMonitor(listener: InterfaceUpdateListener) {}
    override fun closeDefaultInterfaceMonitor(listener: InterfaceUpdateListener) {}
    override fun systemCertificates(): StringIterator? = null
    override fun underNetworkExtension(): Boolean = false
    override fun usePlatformAutoDetectInterfaceControl(): Boolean = true
    override fun useProcFS(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    override fun autoDetectInterfaceControl(fd: Int) {}
}