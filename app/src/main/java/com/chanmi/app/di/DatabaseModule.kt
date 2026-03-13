package com.chanmi.app.di

import android.content.Context
import androidx.room.Room
import com.chanmi.app.data.local.ChanmiDatabase
import com.chanmi.app.data.local.DailyRecordDao
import com.chanmi.app.data.repository.CalendarRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChanmiDatabase {
        return Room.databaseBuilder(
            context,
            ChanmiDatabase::class.java,
            "chanmi_database"
        ).build()
    }

    @Provides
    fun provideDailyRecordDao(database: ChanmiDatabase): DailyRecordDao {
        return database.dailyRecordDao()
    }

    @Provides
    @Singleton
    fun provideCalendarRepository(dao: DailyRecordDao): CalendarRepository {
        return CalendarRepository(dao)
    }
}
