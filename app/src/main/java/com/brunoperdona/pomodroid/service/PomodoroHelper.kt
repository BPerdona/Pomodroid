package com.brunoperdona.pomodroid.service
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.brunoperdona.pomodroid.MainActivity
import com.brunoperdona.pomodroid.util.Constants.CLICK_REQUEST_CODE
import com.brunoperdona.pomodroid.util.Constants.CANCEL_REQUEST_CODE
import com.brunoperdona.pomodroid.util.Constants.RESUME_REQUEST_CODE
import com.brunoperdona.pomodroid.util.Constants.STOP_REQUEST_CODE
import com.brunoperdona.pomodroid.service.PomodoroService.Companion.POMODORO_INTENT_EXTRA

object PomodoroHelper {

    private val flag = PendingIntent.FLAG_IMMUTABLE

    fun clickPendingIntent(context: Context): PendingIntent {
        val clickIntent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context, CLICK_REQUEST_CODE, clickIntent, flag
        )
    }

    fun stopPendingIntent(context: Context): PendingIntent {
        val stopIntent = Intent(context, PomodoroService::class.java).apply {
            putExtra(POMODORO_INTENT_EXTRA, PomodoroService.Companion.IntentType.Stop.name)
        }
        return PendingIntent.getService(
            context, STOP_REQUEST_CODE, stopIntent, flag
        )
    }

    fun startPendingIntent(context: Context): PendingIntent {
        val resumeIntent = Intent(context, PomodoroService::class.java).apply {
            putExtra(POMODORO_INTENT_EXTRA, PomodoroService.Companion.IntentType.Start.name)
        }
        return PendingIntent.getService(
            context, RESUME_REQUEST_CODE, resumeIntent, flag
        )
    }

    fun cancelPendingIntent(context: Context): PendingIntent {
        val cancelIntent = Intent(context, PomodoroService::class.java).apply {
            putExtra(POMODORO_INTENT_EXTRA, PomodoroService.Companion.IntentType.Cancel.name)
        }
        return PendingIntent.getService(
            context, CANCEL_REQUEST_CODE, cancelIntent, flag
        )
    }

    fun triggerForegroundService(context: Context, extra: String) {
        Intent(context, PomodoroService::class.java).apply {
            putExtra(POMODORO_INTENT_EXTRA, extra)
            context.startService(this)
        }
    }
}
