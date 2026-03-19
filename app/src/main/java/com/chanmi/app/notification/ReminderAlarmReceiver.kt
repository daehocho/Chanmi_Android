package com.chanmi.app.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chanmi.app.MainActivity
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
 * 알림 시각에 호출되는 BroadcastReceiver
 *
 * 1. 알림을 표시한다
 * 2. 다음 알림을 재스케줄링한다 (1회 알림 + 재스케줄링 패턴)
 */
class ReminderAlarmReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReceiverEntryPoint {
        fun reminderDao(): PrayerReminderDao
        fun alarmScheduler(): AlarmScheduler
        fun notificationHelper(): NotificationHelper
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: return
        val weekday = intent.getIntExtra(EXTRA_WEEKDAY, 0)

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReceiverEntryPoint::class.java
        )
        val dao = entryPoint.reminderDao()
        val scheduler = entryPoint.alarmScheduler()
        val notificationHelper = entryPoint.notificationHelper()

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val reminder = dao.getById(reminderId) ?: return@launch
                if (!reminder.isEnabled) return@launch

                // 알림 탭 시 해당 기도문으로 이동하는 PendingIntent
                val contentIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(MainActivity.EXTRA_PRAYER_ID, reminder.prayerId)
                    putExtra(MainActivity.EXTRA_NAVIGATE_TO, "prayer_detail")
                }
                val contentPendingIntent = PendingIntent.getActivity(
                    context,
                    reminder.requestCode(weekday),
                    contentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 알림 표시
                notificationHelper.showReminderNotification(reminder, contentPendingIntent)

                // 다음 알림 재스케줄링 (cancel 후 다음 시각으로 재등록)
                scheduler.schedule(reminder)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_WEEKDAY = "weekday"
    }
}
