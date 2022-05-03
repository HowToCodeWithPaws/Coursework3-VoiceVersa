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

        var array = ArrayList<Audio>()

        var path1 = this.externalCacheDir!!.absolutePath + "/saved/saved_1.mp3"
        var path2 = this.externalCacheDir!!.absolutePath + "/saved/saved_2.mp3"
        var path3 = this.externalCacheDir!!.absolutePath + "/saved/saved_result_3.mp3"
        array.add(Audio("saved_1", "recording",path1, 23, Date.from(Instant.now())))
        array.add(Audio("saved_2", "recording",path2, 24, Date(0)))
        array.add(Audio("saved_result_3", "result",path3, 32, Date.from(Instant.now())))

        user = User("hii", "df", array)


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


            val intent = Intent(this, ProcessActivity::class.java)
            startActivity(intent)
        }

        sign_in_button.setOnClickListener {
            //TODO какая-то регистрация
        }
    }

}