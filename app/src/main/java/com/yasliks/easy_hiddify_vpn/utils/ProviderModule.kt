package com.yasliks.easy_hiddify_vpn.utils

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.datastore.core.DataStore
import com.yas.database.repo.AppsRepo
import com.yas.database.repo.ServerRepo
import com.yas.database.utils.settingsDataStore
import com.yasliks.database.data.SettingsData
import com.yasliks.hiddify_library_lib.EasyHiddify
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@Module
@InstallIn(SingletonComponent::class)
object ProviderObject {

    @Provides
    @Singleton
    fun provideEasyHiddify(): EasyHiddify = EasyHiddify.instance

    @Provides
    @Singleton
    fun provideAppsRepo(@ApplicationContext context: Context): AppsRepo {
        return AppsRepo.provide(context)
    }

    @Provides
    @Singleton
    fun provideServerRepo(@ApplicationContext context: Context): ServerRepo {
        return ServerRepo.provide(context)
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<SettingsData> {
        return context.settingsDataStore
    }
}
