package com.chanmi.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chanmi.app.data.model.DailyRecord
import com.chanmi.app.data.model.GoodDeed
import com.chanmi.app.data.model.RosaryEntry

@Database(
    entities = [DailyRecord::class, RosaryEntry::class, GoodDeed::class],
    version = 2,
    exportSchema = true
)
abstract class ChanmiDatabase : RoomDatabase() {
    abstract fun dailyRecordDao(): DailyRecordDao
}
