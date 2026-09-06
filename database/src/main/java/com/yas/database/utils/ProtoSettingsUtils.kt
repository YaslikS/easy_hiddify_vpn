package com.yas.database.utils

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.yasliks.database.data.SettingsData
import java.io.InputStream
import java.io.OutputStream

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
internal object ProtoSettingsUtils : Serializer<SettingsData> {

    override val defaultValue: SettingsData
        get() = SettingsData.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): SettingsData {
        try {
            return SettingsData.parseFrom(input)
        } catch (e: Exception) {
            // ignore
        }

        return defaultValue
    }

    override suspend fun writeTo(t: SettingsData, output: OutputStream) {
        t.writeTo(output)
    }
}

val Context.settingsDataStore: DataStore<SettingsData> by dataStore(
    fileName = "settings.db",
    serializer = ProtoSettingsUtils,
)

