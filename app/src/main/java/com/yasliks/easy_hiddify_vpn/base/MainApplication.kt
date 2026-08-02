package com.yasliks.easy_hiddify_vpn.base

import android.app.Application
import android.content.Context
import com.yasliks.hiddify_library_lib.EasyHiddify
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
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
    }

}