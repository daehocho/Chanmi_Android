package com.chanmi.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chanmi.app.data.local.PrayerReminderDao
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 기기 재부팅 시 모든 활성화된 기도 알림을 재스케줄링
 *
 * Android에서 재부팅 시 AlarmManager의 모든 알림이 사라지므로,
 * BOOT_COMPLETED 수신 시 DB에서 활성 알림을 읽어 재등록한다.
 */
class BootCompleteReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootEntryPoint {
        fun reminderDao(): PrayerReminderDao
        fun alarmScheduler(): AlarmScheduler
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            BootEntryPoint::class.java
        )
        val dao = entryPoint.reminderDao()
        val scheduler = entryPoint.alarmScheduler()

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val enabledReminders = dao.getEnabled()
                enabledReminders.forEach { scheduler.schedule(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
