package com.brunoperdona.pomodroid.di

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.brunoperdona.pomodroid.R
import com.brunoperdona.pomodroid.service.PomodoroHelper
import com.brunoperdona.pomodroid.util.Constants.NOTIFICATION_CHANNEL_ID
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object NotificationModule {

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.start_time))
            .setSmallIcon(R.drawable.android_icon)
            .setOngoing(true)
            .addAction(0, context.getString(R.string.stop), PomodoroHelper.stopPendingIntent(context))
            .addAction(0, context.getString(R.string.cancel), PomodoroHelper.cancelPendingIntent(context))
            .setContentIntent(PomodoroHelper.clickPendingIntent(context))
    }

    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

}