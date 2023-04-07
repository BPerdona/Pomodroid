package com.brunoperdona.pomodroid

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import com.brunoperdona.pomodroid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setBackgroundDrawable(
            AppCompatResources.getDrawable(applicationContext, R.color.strong_pink))
        supportActionBar?.elevation = 10f
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}