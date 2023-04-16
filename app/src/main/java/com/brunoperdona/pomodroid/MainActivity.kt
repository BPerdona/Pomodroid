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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.brunoperdona.pomodroid.databinding.ActivityMainBinding
import com.brunoperdona.pomodroid.service.PomodoroService
import com.brunoperdona.pomodroid.service.PomodoroService.Companion.POMODORO_INTENT_EXTRA
import com.brunoperdona.pomodroid.service.PomodoroStatus
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
            AppCompatResources.getDrawable(applicationContext, R.color.strong_pink))
        supportActionBar?.elevation = 10f
        binding = ActivityMainBinding.inflate(layoutInflater)

        lifecycleScope.launch {
            while (!isBound){
                delay(10)
            }
            repeatOnLifecycle(Lifecycle.State.CREATED){
                pomodoroService.currentTime.collect{
                    binding.time.text = it.getFormatedTime()
                }
            }
        }

        lifecycleScope.launch{
            while (!isBound){
                delay(10)
            }
            pomodoroService.serviceStatus.observe(this@MainActivity){
                when(it) {
                    PomodoroStatus.Started -> {
                        binding.startButton.text = getString(R.string.stop)
                        binding.startButton.setOnClickListener {
                            Intent(applicationContext, PomodoroService::class.java).apply {
                                putExtra(POMODORO_INTENT_EXTRA, PomodoroService.Companion.IntentType.Stop.name)
                                applicationContext.startService(this)
                            }
                        }
                    }
                    PomodoroStatus.Idle -> {
                        binding.startButton.text = getString(R.string.start)
                        binding.startButton.setOnClickListener {
                            Intent(applicationContext, PomodoroService::class.java).apply {
                                putExtra(POMODORO_INTENT_EXTRA, PomodoroService.Companion.IntentType.Start.name)
                                applicationContext.startService(this)
                            }
                        }
                    }
                    PomodoroStatus.Stopped -> {
                        binding.startButton.text = getString(R.string.resume)
                        binding.startButton.setOnClickListener {
                            Intent(applicationContext, PomodoroService::class.java).apply {
                                putExtra(POMODORO_INTENT_EXTRA, PomodoroService.Companion.IntentType.Start.name)
                                applicationContext.startService(this)
                            }
                        }
                    }
                    null -> {}
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

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        Log.d("Service", "OnStop unbind Service")
    }
}