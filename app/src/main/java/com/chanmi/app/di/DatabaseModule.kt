package com.chanmi.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // v2: 인덱스 추가 및 스키마 최적화
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_rosary_entries_daily_record_id` ON `rosary_entries` (`daily_record_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_good_deeds_daily_record_id` ON `good_deeds` (`daily_record_id`)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChanmiDatabase {
        return Room.databaseBuilder(
            context,
            ChanmiDatabase::class.java,
            "chanmi_database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
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
