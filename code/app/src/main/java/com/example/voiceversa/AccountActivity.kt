package com.example.voiceversa

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.voiceversa.View.Library.LibraryActivity
import com.example.voiceversa.View.Request.RequestActivity
import com.example.voiceversa.View.Settings.ProcessActivity
import com.example.voiceversa.View.Settings.SettingsActivity
import java.util.*


class AccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_account)

        Objects.requireNonNull(supportActionBar)!!.hide()
        val topbar = findViewById<Toolbar>(R.id.account_top_bar)
        topbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.process) {
                val intent = Intent(this, ProcessActivity::class.java)
                startActivity(intent)
            }
            true
        }
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