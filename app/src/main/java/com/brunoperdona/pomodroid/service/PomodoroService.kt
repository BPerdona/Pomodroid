package com.brunoperdona.pomodroid.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.brunoperdona.pomodroid.util.Constants
import com.brunoperdona.pomodroid.data.TimeState
import com.brunoperdona.pomodroid.util.Constants.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class PomodoroService: Service() {

    private val SERVICE_TAG = "PomodoroService"
    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    override fun onBind(intent: Intent?) = PomodoroBinder()

    private var duration: Duration = Duration.ZERO
    private lateinit var timer: Timer

    private val job = SupervisorJob()
    private val coroutine = CoroutineScope(Dispatchers.IO+job)

    private var _serviceStatus = MutableStateFlow(PomodoroStatus.Idle)
    val serviceStatus = _serviceStatus.asStateFlow()

    private var _currentTime = MutableStateFlow(TimeState("00", "03", ""))
    val currentTime = _currentTime.asStateFlow()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.getStringExtra(Constants.POMODORO_STATE_EXTRA)){
            PomodoroStatus.Started.name -> {
                duration = Duration.parse("30m")
                startForegroundService()
                startPomodoro()
            }
            PomodoroStatus.Stopped.name ->{
                stopPomodoro()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService(){
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun startPomodoro(){
        _serviceStatus.update {
            PomodoroStatus.Started
        }
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L){
            duration = duration.minus(1.seconds)
            updateTime()
            updateNotification()
        }
    }

    private fun updateTime(){
        duration.toComponents{ _, m, s, h ->
            Log.d(SERVICE_TAG, "Time count: $h:$m:$s")
            _currentTime.update {
                it.copy(
                    seconds = s.toString(),
                    minutes = m.toString(),
                    hours = h.toString()
                )
            }
        }
    }

    private fun updateNotification(){
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder.setContentText(
                _currentTime.value.getFormatedTime()
            ).build()
        )
    }


    private fun stopPomodoro(){
        if(this::timer.isInitialized){
            timer.cancel()
        }
        _serviceStatus.update {
            PomodoroStatus.Stopped
        }
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

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    inner class PomodoroBinder: Binder(){
        fun getService(): PomodoroService = this@PomodoroService
    }

    companion object{
        const val ACTION_SERVICE_START = "ACTION_SERVICE_START"
        const val ACTION_SERVICE_STOP = "ACTION_SERVICE_STOP"
        const val ACTION_SERVICE_CANCEL = "ACTION_SERVICE_CANCEL"
    }
}

enum class PomodoroStatus{
    Idle, Started, Stopped, Canceled
}