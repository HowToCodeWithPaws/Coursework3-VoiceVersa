package com.example.voiceversa

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.time.Instant
import java.util.*

lateinit var user:User

class AuthActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auth)
        Objects.requireNonNull(supportActionBar)!!.title = "VoiceVersa"

        var login: String = ""
        var password: String = ""

        val login_text = findViewById<EditText>(R.id.login)
        val login_password = findViewById<EditText>(R.id.password)

        val log_in_button = findViewById<Button>(R.id.log_in)
        val sign_in_button = findViewById<Button>(R.id.sign_in)

        log_in_button.setOnClickListener {
            //TODO проверОчка на сервере

            var array = ArrayList<Audio>()
            array.add(Audio("h1eeeeeee", "recording", 23, Date.from(Instant.now())))
            array.add(Audio("t2eeee", "recording", 23, Date(0)))
            array.add(Audio("geeeeeeeee3", "result", 32, Date.from(Instant.now())))

            user = User("hii", "df", array)
            val intent = Intent(this, ProcessActivity::class.java)
            startActivity(intent)
        }

        sign_in_button.setOnClickListener {
            //TODO какая-то регистрация
        }
    }

}