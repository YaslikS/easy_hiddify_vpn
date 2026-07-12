package com.yasliks.hiddify_library_lib.utils

import android.content.Context
import com.hiddify.core.libbox.Libbox
import java.io.File

class HiddifyUtils(private val context: Context) {

    fun getWorkingDir(): String {
        val dir = File(context.filesDir, "hiddify")
        if (!dir.exists()) dir.mkdirs()
        return dir.absolutePath
    }

    fun getTempDir(): String {
        val dir = File(context.cacheDir, "hiddify_temp")
        if (!dir.exists()) dir.mkdirs()
        return dir.absolutePath
    }

    fun generateSecret(): String {
        return Libbox.randomHex(16).value
    }
}