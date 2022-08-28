package com.example.voiceversa.View.Settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.voiceversa.R
import java.util.*

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        Objects.requireNonNull(supportActionBar)!!.title = "настройки"
    }
}