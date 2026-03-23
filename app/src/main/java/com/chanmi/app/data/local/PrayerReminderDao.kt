package com.chanmi.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.chanmi.app.data.model.PrayerReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerReminderDao {

    @Query("SELECT * FROM prayer_reminders ORDER BY sortOrder, hour, minute")
    fun getAll(): Flow<List<PrayerReminder>>

    @Query("SELECT * FROM prayer_reminders WHERE isEnabled = 1")
    suspend fun getEnabled(): List<PrayerReminder>

    @Query("SELECT * FROM prayer_reminders WHERE id = :id")
    suspend fun getById(id: String): PrayerReminder?

    @Upsert
    suspend fun upsert(reminder: PrayerReminder)

    @Update
    suspend fun updateAll(reminders: List<PrayerReminder>)

    @Delete
    suspend fun delete(reminder: PrayerReminder)
}
