package com.yasliks.easy_hiddify_vpn.utils

import android.content.Context
import androidx.annotation.RestrictTo
import com.yas.database.repo.AppsRepo
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
    fun provideCacheImagesUtils(@ApplicationContext context: Context): AppsRepo {
        return AppsRepo.provide(context)
    }

}
