package com.brunoperdona.pomodroid.service

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.brunoperdona.pomodroid.MainActivity
import com.brunoperdona.pomodroid.R
import com.brunoperdona.pomodroid.data.PomodoroState
import com.brunoperdona.pomodroid.data.PomodoroStatus
import com.brunoperdona.pomodroid.data.PomodoroType
import com.brunoperdona.pomodroid.data.TimeState
import com.brunoperdona.pomodroid.util.Constants.NOTIFICATION_CHANNEL_ID
import com.brunoperdona.pomodroid.util.Constants.NOTIFICATION_ID
import com.brunoperdona.pomodroid.util.Constants.NOTIFICATION_NAME
import dagger.hilt.android.AndroidEntryPoint
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

    @Inject
    lateinit var mMediaPlayer: MediaPlayer

    override fun onBind(intent: Intent?) = PomodoroBinder()

    private var duration: Duration = Duration.parse("25m")
    private lateinit var timer: Timer

    var serviceState = MutableLiveData(PomodoroState())
        private set

    private var _currentTime = MutableStateFlow(TimeState("", "25", ""))
    val currentTime = _currentTime.asStateFlow()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(SERVICE_TAG, "Receive a Intent: ${intent?.getStringExtra(POMODORO_INTENT_EXTRA)}")
        when(intent?.getStringExtra(POMODORO_INTENT_EXTRA)){
            IntentType.Start.name -> {
                if (serviceState.value?.pomodoroStatus != PomodoroStatus.Started){
                    startForegroundService()
                    setStopNotification()
                    startPomodoro { time ->
                        updateNotification(time)
                    }
                }
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
                stopPomodoro()
                cancelPomodoro()
                stopForegroundService()
                val intentTime = intent.getStringExtra(POMODORO_INTENT_TIME_VALUE) ?: "25m"
                duration = Duration.parse(intentTime)
                updatePomodoroType(intentTime)
                serviceState.postValue(serviceState.value?.copy(
                    pomodoroStatus = PomodoroStatus.Idle
                ))
                updateTime()
            }
            IntentType.StopAlarm.name -> {
                stopAlarm()
                updateNotificationActions()
                stopForegroundService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopAlarm(){
        mMediaPlayer.pause()
        mMediaPlayer.seekTo(0)
        serviceState.postValue(serviceState.value?.copy(pomodoroStatus = PomodoroStatus.Idle))
        val time = when(serviceState.value?.pomodoroType){
            PomodoroType.Pomodoro -> "25m"
            PomodoroType.Long -> "15m"
            PomodoroType.Short -> "5m"
            else -> "25m"
        }
        duration = Duration.parse(time)
        updateTime()
    }

    private fun startForegroundService(){
        val pomodoroType = when(serviceState.value?.pomodoroType){
            PomodoroType.Pomodoro -> getString(R.string.pomodoro_chip)
            PomodoroType.Long -> getString(R.string.long_break_chip)
            PomodoroType.Short -> getString(R.string.short_break_chip)
            null -> getString(R.string.pomodoro_chip)
        }
        Log.e(SERVICE_TAG, "Start Foreground with $pomodoroType")
        createNotificationChannel()
        startForeground(
            NOTIFICATION_ID,
            notificationBuilder
                .setContentTitle(pomodoroType)
                .build()
        )
    }

    private fun startPomodoro(onTick: (text: String) -> Unit){
        serviceState.postValue(
            serviceState.value?.copy(pomodoroStatus = PomodoroStatus.Started)
        )
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L){
            if(duration.isNegative()){
                Log.d(SERVICE_TAG, "Timer stop. Hits 00")
                stopPomodoro()
                mMediaPlayer.start()
                timeEndNotification()
                serviceState.postValue(serviceState.value?.copy(pomodoroStatus = PomodoroStatus.Alarm))
            }
            duration = duration.minus(1.seconds)
            updateTime()
            onTick(_currentTime.value.getFormatedTime())
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

    private fun updateNotification(time: String){
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder.setContentText(time).build()
        )
    }

    private fun updateNotificationActions(){
        notificationBuilder.clearActions()
        notificationBuilder.addAction(
            0,
            this.getString(R.string.stop),
            PomodoroHelper.stopPendingIntent(this)
        )
        notificationBuilder.addAction(
            0,
            this.getString(R.string.cancel),
            PomodoroHelper.cancelPendingIntent(this)
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    @SuppressLint("RestrictedApi")
    private fun timeEndNotification(){
        notificationBuilder.clearActions()
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    @SuppressLint("RestrictedApi")
    private fun setStartNotification(){
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                0,
                this.getString(R.string.resume),
                PomodoroHelper.startPendingIntent(this)
            )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    @SuppressLint("RestrictedApi")
    private fun setStopNotification(){
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

    private fun updatePomodoroType(time: String){
        when(time){
            "25m" -> {
                serviceState.value?.pomodoroType = PomodoroType.Pomodoro
            }
            "5m" -> {
                serviceState.value?.pomodoroType = PomodoroType.Short
            }
            "15m" -> {
                serviceState.value?.pomodoroType = PomodoroType.Long
            }
        }
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder
                .setContentTitle(serviceState.value?.pomodoroType?.name ?: "Pomodoro")
                .build()
        )
    }

    private fun cancelPomodoro(){
        val time = when(serviceState.value?.pomodoroType){
            PomodoroType.Pomodoro -> "25m"
            PomodoroType.Long -> "15m"
            PomodoroType.Short -> "5m"
            else-> "25m"
        }
        duration = Duration.parse(time)
        serviceState.postValue(
            serviceState.value?.copy(pomodoroStatus = PomodoroStatus.Idle)
        )
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
        serviceState.postValue(
            serviceState.value?.copy(pomodoroStatus = PomodoroStatus.Stopped)
        )
    }

    private fun createNotificationChannel(){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    inner class PomodoroBinder: Binder(){
        fun getService(): PomodoroService = this@PomodoroService
    }

    companion object{
        private const val SERVICE_TAG = "Pomodoro Service"

        const val POMODORO_INTENT_EXTRA = "POMODORO_INTENT_EXTRA"
        const val POMODORO_INTENT_TIME_VALUE = "POMODORO_INTENT_TIME_VALUE"

        enum class IntentType{
            Start, Stop, Cancel, ChangeTime, StopAlarm
        }
    }
}