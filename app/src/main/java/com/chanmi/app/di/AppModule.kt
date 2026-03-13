package com.chanmi.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.chanmi.app.data.repository.ChurchRepository
import com.chanmi.app.data.repository.PrayerRepository
import com.chanmi.app.location.LocationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chanmi_preferences")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideChurchRepository(@ApplicationContext context: Context): ChurchRepository {
        return ChurchRepository(context)
    }

    @Provides
    @Singleton
    fun providePrayerRepository(@ApplicationContext context: Context): PrayerRepository {
        return PrayerRepository(context)
    }

    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
        return LocationManager(context)
    }
}
