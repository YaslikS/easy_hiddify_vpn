package com.yasliks.easy_hiddify_vpn.utils

import android.content.Context
import androidx.annotation.RestrictTo
import com.yas.database.repo.AppsRepo
import com.yas.database.repo.ServerRepo
import com.yas.database.utils.settingsDataStore
import androidx.datastore.core.DataStore
import com.yasliks.database.data.SettingsData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@Module
@InstallIn(ActivityComponent::class,  ViewModelComponent::class)
object ProviderObject {

    @Provides
    fun provideAppsRepo(@ApplicationContext context: Context): AppsRepo {
        return AppsRepo.provide(context)
    }

    @Provides
    fun provideServerRepo(@ApplicationContext context: Context): ServerRepo {
        return ServerRepo.provide(context)
    }

    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<SettingsData> {
        return context.settingsDataStore
    }
}
