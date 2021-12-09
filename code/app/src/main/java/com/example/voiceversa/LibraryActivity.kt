package com.example.voiceversa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class LibraryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_library)
        Objects.requireNonNull(supportActionBar)!!.title = "библиотека"
    }
}