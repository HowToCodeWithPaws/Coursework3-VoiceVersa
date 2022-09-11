package com.example.voiceversa

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.voiceversa.Controller.Controller
import com.example.voiceversa.Controller.makeDirectories
import com.example.voiceversa.Controller.readAudioNames
import com.example.voiceversa.Model.Audio
import com.example.voiceversa.Model.User
import com.example.voiceversa.View.Settings.ProcessActivity
import java.time.Instant
import java.util.*

lateinit var user: User

lateinit var controller: Controller

class AuthActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val home = this.externalMediaDirs!![0]!!.absolutePath

        controller = Controller(home)

        setContentView(R.layout.activity_auth)
        Objects.requireNonNull(supportActionBar)!!.title = "VoiceVersa"

        val login_text = findViewById<EditText>(R.id.login)
        val password_text = findViewById<EditText>(R.id.password)

        val log_in_button = findViewById<Button>(R.id.log_in)
        val sign_in_button = findViewById<Button>(R.id.sign_in)
        val guest_button = findViewById<Button>(R.id.guest)


        log_in_button.setOnClickListener {
            if (controller.signIn(login_text.text.toString(), password_text.text.toString())) {
                user = User(login_text.text.toString())
                // TODO: download user info
                val intent = Intent(this, ProcessActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "Вы ввели неверные данные для авторизации. Попробуйте снова.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        sign_in_button.setOnClickListener {
            if (controller.signUp(login_text.text.toString(), password_text.text.toString())) {
                user = User(login_text.text.toString())
                val intent = Intent(this, ProcessActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "Вас не получилось зарегистрировать. Попробуйте снова.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        guest_button.setOnClickListener{
            user = User("", "")
            val intent = Intent(this, ProcessActivity::class.java)
            startActivity(intent)
        }
    }

}