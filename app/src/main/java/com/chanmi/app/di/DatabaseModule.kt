package com.chanmi.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chanmi.app.data.local.ChanmiDatabase
import com.chanmi.app.data.local.DailyRecordDao
import com.chanmi.app.data.local.PrayerReminderDao
import com.chanmi.app.data.repository.CalendarRepository
import com.chanmi.app.data.repository.PrayerReminderRepository
import com.chanmi.app.notification.AlarmScheduler
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

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // v3: 기도 알림 테이블 추가
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS prayer_reminders (
                    id TEXT NOT NULL PRIMARY KEY,
                    prayerId TEXT NOT NULL,
                    prayerTitle TEXT NOT NULL,
                    categoryName TEXT NOT NULL DEFAULT '',
                    hour INTEGER NOT NULL,
                    minute INTEGER NOT NULL,
                    isEnabled INTEGER NOT NULL DEFAULT 1,
                    weekdays TEXT NOT NULL DEFAULT '',
                    createdAt INTEGER NOT NULL DEFAULT 0
                )
            """)
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // v4: rosary_entries에 decadeCount 컬럼 추가 (기도 단수 per-record 저장)
            db.execSQL("ALTER TABLE rosary_entries ADD COLUMN decadeCount INTEGER NOT NULL DEFAULT 5")
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }

    @Provides
    fun provideDailyRecordDao(database: ChanmiDatabase): DailyRecordDao {
        return database.dailyRecordDao()
    }

    @Provides
    fun providePrayerReminderDao(database: ChanmiDatabase): PrayerReminderDao {
        return database.prayerReminderDao()
    }

    @Provides
    @Singleton
    fun provideCalendarRepository(dao: DailyRecordDao): CalendarRepository {
        return CalendarRepository(dao)
    }

    @Provides
    @Singleton
    fun providePrayerReminderRepository(
        dao: PrayerReminderDao,
        alarmScheduler: AlarmScheduler
    ): PrayerReminderRepository {
        return PrayerReminderRepository(dao, alarmScheduler)
    }
}
