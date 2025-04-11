package dev.jdtech.jellyfin.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.jdtech.jellyfin.AppPreferences
import dev.jdtech.jellyfin.settings.domain.DanmuPreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppPreferencesModule {
    @Singleton
    @Provides
    fun provideAppPreferences(sp: SharedPreferences): AppPreferences {
        return AppPreferences(sp)
    }

    @Singleton
    @Provides
    fun provideDanmuPreferences(sp: SharedPreferences): DanmuPreferences {
        return DanmuPreferences(sp)
    }
}
