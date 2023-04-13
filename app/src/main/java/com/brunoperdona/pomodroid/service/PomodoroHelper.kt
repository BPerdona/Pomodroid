package com.brunoperdona.pomodroid.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.brunoperdona.pomodroid.service.PomodoroService.Companion.POMODORO_STATE_EXTRA

object PomodoroHelper {

    private const val flag = PendingIntent.FLAG_IMMUTABLE

    fun pausePendingIntent(context: Context): PendingIntent{
        val stopIntent = Intent(context, PomodoroService::class.java).apply {
            putExtra(POMODORO_STATE_EXTRA, PomodoroService.Companion.IntentType.Pause)
        }
        return PendingIntent.getService(
            context, 0, stopIntent, flag
        )
    }

    fun cancelPendingIntent(context: Context): PendingIntent{
        val cancelIntent = Intent(context, PomodoroService::class.java).apply {
            putExtra(POMODORO_STATE_EXTRA, PomodoroService.Companion.IntentType.Cancel)
        }
        return PendingIntent.getService(
            context, 0, cancelIntent, flag
        )
    }

}