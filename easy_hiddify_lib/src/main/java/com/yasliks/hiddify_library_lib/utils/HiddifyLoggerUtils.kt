package com.yasliks.hiddify_library_lib.utils

import android.util.Log
import com.hiddify.core.libbox.LogEntry
import com.hiddify.core.libbox.LogIterator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HiddifyLoggerUtils {
    private val TAG = "HiddifySDK"
    private val MAX_LOGS = 500
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs = _logs.asStateFlow()

    // The basic method for the iterator (from `JNI`)
    fun append(iterator: LogIterator) {
        while (iterator.hasNext()) {
            append(iterator.next())
        }
    }

    // The main method for the `LogEntry` object
    fun append(entry: LogEntry) {
        val currentList = _logs.value.toMutableList()
        currentList.add(entry)
        if (currentList.size > MAX_LOGS) {
            currentList.removeAt(0)
        }
        _logs.value = currentList

        // Дублируем в системный Logcat для удобства разработки
        when (entry.level) {
            0 -> Log.v(TAG, entry.message)
            1 -> Log.d(TAG, entry.message)
            2 -> Log.i(TAG, entry.message)
            3 -> Log.w(TAG, entry.message)
            else -> Log.e(TAG, entry.message)
        }
    }

    // Auxiliary method for direct string writing (used in `CommandHandler`)
    fun append(level: Int, message: String) {
        val entry = LogEntry()
        entry.level = level
        entry.message = message
        append(entry)
    }

    fun clear() {
        _logs.value = emptyList()
    }
}