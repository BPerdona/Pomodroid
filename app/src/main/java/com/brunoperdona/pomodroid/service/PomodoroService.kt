package com.brunoperdona.pomodroid.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.brunoperdona.pomodroid.util.Constants
import com.brunoperdona.pomodroid.util.TimeState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.concurrent.thread

@AndroidEntryPoint
class PomodoroService: Service() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    private val job = SupervisorJob()
    private val coroutine = CoroutineScope(Dispatchers.IO+job)

    private var _timer = MutableStateFlow(TimeState("00", "00"))
    val timer = _timer.asStateFlow()

    override fun onBind(intent: Intent?) = PomodoroBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(101, notificationBuilder.build())

        coroutine.launch {
            while (true){
                _timer.update { time ->
                    time.copy(seconds = (_timer.value.seconds.toInt()+1).toString())
                }
                notificationManager.notify(
                    101,
                    notificationBuilder.setContentText(_timer.value.getFormatedTime()).build()
                )
                delay(1000L)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                "TestName",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    inner class PomodoroBinder: Binder(){
        fun getService(): PomodoroService = this@PomodoroService
    }
}