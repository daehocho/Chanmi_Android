package com.chanmi.app

import android.app.Application
import com.chanmi.app.data.repository.PrayerReminderRepository
import com.chanmi.app.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ChanmiApplication : Application() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var reminderRepository: PrayerReminderRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // 알림 채널 생성 (Android 8+ 필수)
        notificationHelper.createNotificationChannel()
        // 앱 시작 시 활성 알림 동기화 (iOS syncAllReminders 대응)
        applicationScope.launch {
            reminderRepository.syncAllAlarms()
        }
    }
}
