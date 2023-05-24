package com.brunoperdona.pomodroid

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.get
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.brunoperdona.pomodroid.data.PomodoroStatus
import com.brunoperdona.pomodroid.data.PomodoroType
import com.brunoperdona.pomodroid.databinding.ActivityMainBinding
import com.brunoperdona.pomodroid.service.PomodoroHelper
import com.brunoperdona.pomodroid.service.PomodoroService
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var isBound = false
    private lateinit var binding: ActivityMainBinding
    private lateinit var pomodoroService: PomodoroService

    private val connection = object :ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PomodoroService.PomodoroBinder
            pomodoroService = binder.getService()
            Log.d("Service", "Service created")
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("Service", "Service desconnected")
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, PomodoroService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(Manifest.permission.POST_NOTIFICATIONS)
        }

        supportActionBar?.setBackgroundDrawable(
            AppCompatResources.getDrawable(applicationContext, R.color.green_600))
        supportActionBar?.elevation = 10f
        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.cancelButton.setOnClickListener {
            PomodoroHelper.triggerForegroundService(
                this@MainActivity,
                PomodoroService.Companion.IntentType.Cancel.name
            )
        }

        binding.alarmCancel.setOnClickListener {
            PomodoroHelper.triggerForegroundService(
                this@MainActivity,
                PomodoroService.Companion.IntentType.StopAlarm.name
            )
            binding.alarmCancel.visibility = View.INVISIBLE
            binding.startButton.visibility = View.VISIBLE
            binding.cancelButton.visibility = View.VISIBLE
            binding.pomodoroChipGroup.visibility = View.VISIBLE
        }

        binding.pomodoroChip.setOnClickListener {
            PomodoroHelper.changePomodoroTime(this, "25m")
        }

        binding.shortBreak.setOnClickListener {
            PomodoroHelper.changePomodoroTime(this, "5m")
        }

        binding.longBreak.setOnClickListener {
            PomodoroHelper.changePomodoroTime(this, "15m")
        }

        lifecycleScope.launch {
            awaitServiceBind()
            repeatOnLifecycle(Lifecycle.State.CREATED){
                pomodoroService.currentTime.collect{
                    binding.time.text = it.getFormatedTime()
                }
            }
        }

        lifecycleScope.launch {
            awaitServiceBind()
            pomodoroService.serviceState.observe(this@MainActivity){
                when(it.pomodoroStatus) {
                    PomodoroStatus.Started -> {
                        binding.startButton.text = getString(R.string.stop)
                        binding.startButton.setOnClickListener {
                            PomodoroHelper.triggerForegroundService(
                                this@MainActivity,
                                PomodoroService.Companion.IntentType.Stop.name
                            )
                        }
                        binding.cancelButton.setBackgroundColor(getColor(R.color.white))
                        binding.cancelButton.isEnabled = true
                    }
                    PomodoroStatus.Idle -> {
                        binding.startButton.text = getString(R.string.start)
                        binding.startButton.setOnClickListener {
                            PomodoroHelper.triggerForegroundService(
                                this@MainActivity,
                                PomodoroService.Companion.IntentType.Start.name
                            )
                        }
                        binding.cancelButton.setBackgroundColor(getColor(R.color.green_900))
                        binding.cancelButton.isEnabled = false
                    }
                    PomodoroStatus.Stopped -> {
                        binding.startButton.text = getString(R.string.resume)
                        binding.startButton.setOnClickListener {
                            PomodoroHelper.triggerForegroundService(
                                this@MainActivity,
                                PomodoroService.Companion.IntentType.Start.name
                            )
                        }
                        binding.cancelButton.isEnabled = true
                    }
                    PomodoroStatus.Alarm -> {
                        binding.alarmCancel.visibility = View.VISIBLE
                        binding.startButton.visibility = View.INVISIBLE
                        binding.cancelButton.visibility = View.INVISIBLE
                        binding.pomodoroChipGroup.visibility = View.INVISIBLE
                    }
                }
            }
        }
        setContentView(binding.root)
    }

    private fun requestPermissions(vararg permissions: String){
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ){ result ->
            result.entries.forEach{
                Log.d("Main", "${it.key} ${it.value}")
            }
        }
        requestPermissionLauncher.launch(permissions.asList().toTypedArray())
    }

    private suspend fun awaitServiceBind(){
        while (!isBound){
            delay(10)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        Log.d("Service", "OnStop unbind Service")
    }
}