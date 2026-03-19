package com.chanmi.app.di

import android.content.Context
import com.chanmi.app.notification.AlarmScheduler
import com.chanmi.app.notification.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideAlarmScheduler(
        @ApplicationContext context: Context
    ): AlarmScheduler = AlarmScheduler(context)

    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper = NotificationHelper(context)
}
