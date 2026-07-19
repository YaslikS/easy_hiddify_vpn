package com.yasliks.hiddify_library_lib.utils

import android.content.Context
import android.util.Log
import com.hiddify.core.libbox.LogEntry
import com.hiddify.core.libbox.LogIterator
import com.yasliks.hiddify_library_lib.prefs.HiddifyPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HiddifyLoggerUtils(private val context: Context) {
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
        if (currentList.size > HiddifyPrefs.MAX_LOGS) {
            currentList.removeAt(0)
        }
        _logs.value = currentList

        when (entry.level) {
            0 -> Log.v(HiddifyPrefs.HIDDIFY_LIB, entry.message)
            1 -> Log.d(HiddifyPrefs.HIDDIFY_LIB, entry.message)
            2 -> Log.i(HiddifyPrefs.HIDDIFY_LIB, entry.message)
            3 -> Log.w(HiddifyPrefs.HIDDIFY_LIB, entry.message)
            else -> Log.e(HiddifyPrefs.HIDDIFY_LIB, entry.message)
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