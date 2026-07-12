package com.yasliks.easy_hiddify_vpn.base

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.yasliks.hiddify_library_lib.EasyHiddify

class MainApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this
    }

    override fun onCreate() {
        super.onCreate()

        EasyHiddify.init(this)
    }

    companion object {
        lateinit var application: MainApplication
        val connectivity by lazy { application.getSystemService<ConnectivityManager>()!! }
        val packageManager by lazy { application.packageManager }
    }

}