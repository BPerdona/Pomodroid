package com.brunoperdona.pomodroid.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.brunoperdona.pomodroid.R
import com.brunoperdona.pomodroid.util.Constants
import com.brunoperdona.pomodroid.data.TimeState
import com.brunoperdona.pomodroid.util.Constants.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class PomodoroService: Service() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    override fun onBind(intent: Intent?) = PomodoroBinder()

    private val defautDuration = "30m"
    private var duration: Duration = Duration.parse(defautDuration)
    private lateinit var timer: Timer

    var serviceStatus = MutableLiveData(PomodoroStatus.Idle)
        private set

    private var _currentTime = MutableStateFlow(TimeState("", "45", ""))
    val currentTime = _currentTime.asStateFlow()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(SERVICE_TAG, "Receive a Intent: ${intent?.getStringExtra(POMODORO_STATE_EXTRA)}")
        when(intent?.getStringExtra(POMODORO_STATE_EXTRA)){
            IntentType.Start.name -> {
                startForegroundService()
                startPomodoro()
                setPauseNotification()
            }
            IntentType.Stop.name ->{
                setStartNotification()
                stopPomodoro()
            }
            IntentType.Cancel.name ->{
                stopPomodoro()
                cancelPomodoro()
                stopForegroundService()
            }
            IntentType.ChangeTime.name ->{

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService(){
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun startPomodoro(){
        serviceStatus.postValue(PomodoroStatus.Started)
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L){
            if(duration.isNegative()){
                stopPomodoro()
                Log.d(SERVICE_TAG, "Timer stop. Hits 00")
            }
            duration = duration.minus(1.seconds)
            updateTime()
            updateNotification()
        }
    }

    private fun updateTime(){
        if (!duration.isNegative())
            duration.toComponents{ _, m, s, h ->
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

    @SuppressLint("RestrictedApi")
    private fun setStartNotification(){
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                0,
                this.getString(R.string.proceed),
                PomodoroHelper.startPendingIntent(this)
            )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    @SuppressLint("RestrictedApi")
    private fun setPauseNotification(){
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                0,
                this.getString(R.string.stop),
                PomodoroHelper.stopPendingIntent(this)
            )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun cancelPomodoro(){
        duration = Duration.parse(defautDuration)
        serviceStatus.postValue(PomodoroStatus.Idle)
        updateTime()
    }

    private fun stopForegroundService(){
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopPomodoro(){
        if(this::timer.isInitialized){
            timer.cancel()
        }
        serviceStatus.postValue(PomodoroStatus.Stopped)
    }

    private fun createNotificationChannel(){
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    inner class PomodoroBinder: Binder(){
        fun getService(): PomodoroService = this@PomodoroService
    }

    companion object{
        private const val SERVICE_TAG = "Pomodoro Service"

        const val POMODORO_STATE_EXTRA = "POMODORO_INTENT_EXTRA"

        enum class IntentType{
            Start, Stop, Cancel, ChangeTime
        }
    }
}

enum class PomodoroStatus{
    Idle, Started, Stopped
}