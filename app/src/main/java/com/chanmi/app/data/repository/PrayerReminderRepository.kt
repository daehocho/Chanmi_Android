package com.chanmi.app.data.repository

import com.chanmi.app.data.local.PrayerReminderDao
import com.chanmi.app.data.model.PrayerReminder
import com.chanmi.app.notification.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 기도 알림 Repository
 *
 * Room CRUD + AlarmManager 스케줄링을 통합하여
 * 데이터 변경 시 알림도 함께 갱신한다.
 */
class PrayerReminderRepository @Inject constructor(
    private val dao: PrayerReminderDao,
    private val alarmScheduler: AlarmScheduler
) {
    fun getAll(): Flow<List<PrayerReminder>> = dao.getAll()

    suspend fun getById(id: String): PrayerReminder? = dao.getById(id)

    suspend fun save(reminder: PrayerReminder) {
        dao.upsert(reminder)
        if (reminder.isEnabled) {
            alarmScheduler.schedule(reminder)
        } else {
            alarmScheduler.cancel(reminder)
        }
    }

    suspend fun delete(reminder: PrayerReminder) {
        alarmScheduler.cancel(reminder)
        dao.delete(reminder)
    }

    suspend fun toggleEnabled(reminder: PrayerReminder) {
        val updated = reminder.copy(isEnabled = !reminder.isEnabled)
        dao.upsert(updated)
        if (updated.isEnabled) {
            alarmScheduler.schedule(updated)
        } else {
            alarmScheduler.cancel(updated)
        }
    }

    /** 앱 시작/재부팅 시 활성화된 모든 알림 재스케줄링 */
    suspend fun syncAllAlarms() {
        val enabled = dao.getEnabled()
        enabled.forEach { alarmScheduler.schedule(it) }
    }
}
