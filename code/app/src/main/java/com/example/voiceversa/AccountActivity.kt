package com.example.voiceversa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.voiceversa.View.Library.LibraryActivity
import com.example.voiceversa.View.Request.RequestActivity
import com.example.voiceversa.View.Settings.SettingsActivity
import java.util.*


class AccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//TODO сделать красивое??
        setContentView(R.layout.activity_account)

        Objects.requireNonNull(supportActionBar)!!.title = "Личный кабинет"
        val library_button = findViewById<Button>(R.id.library)
        val settings_button = findViewById<Button>(R.id.settings)
        val request_button = findViewById<Button>(R.id.request)

        library_button.setOnClickListener {
            val intent = Intent(this, LibraryActivity::class.java)
            startActivity(intent)
        }
        settings_button.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        request_button.setOnClickListener {
            val intent = Intent(this, RequestActivity::class.java)
            startActivity(intent)
        }
    }
}