package com.chanmi.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.chanmi.app.R
import com.chanmi.app.data.model.PrayerReminder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * NotificationChannel 생성 + 알림 빌드 헬퍼
 */
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /** 앱 시작 시 호출하여 채널 생성 */
    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "기도 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "설정한 시간에 기도 알림을 전달합니다"
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    /** 기도 알림 Notification 표시 */
    fun showReminderNotification(reminder: PrayerReminder, pendingContentIntent: android.app.PendingIntent) {
        val (title, body) = randomMessage(reminder)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_latin_cross)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingContentIntent)
            .build()

        notificationManager.notify(reminder.id.hashCode(), notification)
    }

    /** iOS와 동일한 7가지 랜덤 알림 메시지 */
    private fun randomMessage(reminder: PrayerReminder): Pair<String, String> {
        val messages = listOf(
            "🙏 기도 시간" to "${reminder.prayerTitle} 시간입니다. 잠시 멈추고 주님과 함께해요.",
            "✝️ 찬미예수" to "지금 ${reminder.prayerTitle}을(를) 바칠 시간입니다.",
            "🕊️ 평화의 시간" to "${reminder.prayerTitle}로 하루를 봉헌해 보세요.",
            "💒 기도 알림" to "${reminder.prayerTitle} 시간이에요. 주님께서 기다리고 계십니다.",
            "📿 묵상의 시간" to "잠시 멈추고 ${reminder.prayerTitle}을(를) 바쳐볼까요?",
            "🌿 은총의 시간" to "${reminder.prayerTitle}로 마음의 평화를 찾아보세요.",
            "🔔 기도 시간" to "${reminder.prayerTitle} 시간입니다. 오늘도 주님과 동행해요.",
        )
        return messages.random()
    }

    companion object {
        const val CHANNEL_ID = "prayer_reminder_channel"
    }
}
