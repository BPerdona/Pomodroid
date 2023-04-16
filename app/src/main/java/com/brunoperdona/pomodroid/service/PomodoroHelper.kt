package com.brunoperdona.pomodroid.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.brunoperdona.pomodroid.service.PomodoroService.Companion.POMODORO_INTENT_EXTRA
import com.brunoperdona.pomodroid.MainActivity

object PomodoroHelper {

    private const val flag = PendingIntent.FLAG_IMMUTABLE

    fun clickPendingIntent(context: Context): PendingIntent{
        val clickIntent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context, 0, clickIntent, flag
        )
    }

    fun startPendingIntent(context: Context): PendingIntent{
        val startIntent = Intent(context, PomodoroService::class.java).apply {
            putExtra(POMODORO_INTENT_EXTRA, PomodoroService.Companion.IntentType.Start.name)
        }
        return PendingIntent.getService(
            context, 0, startIntent, flag
        )
    }

    fun stopPendingIntent(context: Context): PendingIntent{
        val stopIntent = Intent(context, PomodoroService::class.java).apply {
            putExtra(POMODORO_INTENT_EXTRA, PomodoroService.Companion.IntentType.Stop.name)
        }
        return PendingIntent.getService(
            context, 0, stopIntent, flag
        )
    }

    fun cancelPendingIntent(context: Context): PendingIntent{
        val cancelIntent = Intent(context, PomodoroService::class.java).apply {
            putExtra(POMODORO_INTENT_EXTRA, PomodoroService.Companion.IntentType.Cancel.name)
        }
        return PendingIntent.getService(
            context, 0, cancelIntent, flag
        )
    }

}