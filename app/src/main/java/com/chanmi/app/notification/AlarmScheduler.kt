package com.chanmi.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.chanmi.app.data.model.PrayerReminder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

/**
 * AlarmManager 래퍼
 *
 * 기도 알림을 정확한 시간에 스케줄링/취소한다.
 * setExactAndAllowWhileIdle()로 1회 알림을 설정하고,
 * ReminderAlarmReceiver에서 다음 알림을 재스케줄링하는 패턴.
 */
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: PrayerReminder) {
        if (!reminder.isEnabled) return
        cancel(reminder) // 기존 알림 취소 후 재등록

        val weekdays = reminder.weekdaySet()
        if (weekdays.isEmpty()) {
            // 매일 반복: 다음 발생 시각에 1회 설정
            scheduleAt(reminder, nextTriggerTime(reminder.hour, reminder.minute), 0)
        } else {
            // 요일별 개별 알림
            weekdays.forEach { weekday ->
                scheduleAt(reminder, nextTriggerTimeForWeekday(reminder.hour, reminder.minute, weekday), weekday)
            }
        }
    }

    fun cancel(reminder: PrayerReminder) {
        // 기본(매일) + 7개 요일별 PendingIntent 모두 취소
        cancelPendingIntent(reminder.requestCode(0))
        (1..7).forEach { cancelPendingIntent(reminder.requestCode(it)) }
    }

    /** 정확한 알림이 허용되는지 확인 (Android 12+) */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun scheduleAt(reminder: PrayerReminder, triggerAtMillis: Long, weekday: Int) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderAlarmReceiver.EXTRA_WEEKDAY, weekday)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.requestCode(weekday),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // 정확한 알림 권한 없으면 비정확 알림으로 fallback
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun cancelPendingIntent(requestCode: Int) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    companion object {
        /**
         * 다음 발생 시각 계산 (매일 반복용)
         * 오늘 해당 시각이 이미 지났으면 내일로 설정
         */
        fun nextTriggerTime(hour: Int, minute: Int): Long {
            val now = Calendar.getInstance()
            val trigger = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (trigger.before(now) || trigger == now) {
                trigger.add(Calendar.DAY_OF_YEAR, 1)
            }
            return trigger.timeInMillis
        }

        /**
         * 특정 요일의 다음 발생 시각 계산
         * @param weekday 1=일요일 ~ 7=토요일 (iOS와 동일한 Convention)
         */
        fun nextTriggerTimeForWeekday(hour: Int, minute: Int, weekday: Int): Long {
            val now = Calendar.getInstance()
            val trigger = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // Calendar.DAY_OF_WEEK: 1=SUNDAY ... 7=SATURDAY (iOS와 동일)
                set(Calendar.DAY_OF_WEEK, weekday)
            }
            // 이번 주 해당 요일이 이미 지났으면 다음 주로
            if (trigger.before(now) || trigger == now) {
                trigger.add(Calendar.WEEK_OF_YEAR, 1)
            }
            return trigger.timeInMillis
        }
    }
}
