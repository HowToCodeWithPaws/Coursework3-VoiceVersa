package com.example.voiceversa.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.voiceversa.R
import com.example.voiceversa.view.libraryActivity.LibraryActivity
import java.util.*


class AccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_account)

        Objects.requireNonNull(supportActionBar)!!.hide()
        val topBar = findViewById<Toolbar>(R.id.account_top_bar)
        topBar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.process) {
                val intent = Intent(this, ProcessActivity::class.java)
                startActivity(intent)
            }
            true
        }
        val libraryButton = findViewById<Button>(R.id.library)
        val settingsButton = findViewById<Button>(R.id.settings)
        val requestButton = findViewById<Button>(R.id.request)

        libraryButton.setOnClickListener {
            val intent = Intent(this, LibraryActivity::class.java)
            startActivity(intent)
        }
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        requestButton.setOnClickListener {
            val intent = Intent(this, RequestActivity::class.java)
            startActivity(intent)
        }


        println("LOOK HERE AUDIOS ACCOUNT "+ user.audios.size)
    }
}