package com.example.voiceversa

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.voiceversa.Controller.AudioApiService
import com.example.voiceversa.Controller.Controller
import com.example.voiceversa.Controller.makeDirectories
import com.example.voiceversa.Controller.readAudioNames
import com.example.voiceversa.Model.Audio
import com.example.voiceversa.Model.User
import com.example.voiceversa.View.Settings.ProcessActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.util.*

lateinit var user: User

lateinit var controller: Controller

class AuthActivity : AppCompatActivity() {

    lateinit var login_text: EditText
    lateinit var password_text :EditText

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val home = this.externalMediaDirs!![0]!!.absolutePath

        controller = Controller(home)

        setContentView(R.layout.activity_auth)
        Objects.requireNonNull(supportActionBar)!!.title = "VoiceVersa"

         login_text = findViewById<EditText>(R.id.login)
         password_text = findViewById<EditText>(R.id.password)

        val sign_in_button = findViewById<Button>(R.id.sign_in)
        val sign_up_button = findViewById<Button>(R.id.sign_up)
        val guest_button = findViewById<Button>(R.id.guest)

        if (!controller.online){
            sign_in_button.isEnabled = false
            sign_up_button.isEnabled = false
            Toast.makeText(
                this,
                "К сожалению, вы не подключены к серверу. Вам доступен определенный оффлайн " +
                        "функционал, но для полноценной работы приложения дождитесь, пожалуйста, " +
                        "установки подключения.",
                Toast.LENGTH_SHORT
            ).show()
        }

        sign_in_button.setOnClickListener {
            notGuest("signin")
        }

        sign_up_button.setOnClickListener {
            notGuest("signup")
        }

        guest_button.setOnClickListener {
            user = User("", "")
            val intent = Intent(this, ProcessActivity::class.java)
            startActivity(intent)
        }
    }


    fun notGuest(key: String) {
        controller.signInOrUp(
            login_text.text.toString(),
            password_text.text.toString(),
            key
        ).observe(this) {
            Log.d("AUTH", " $it")
            if (!it.isNullOrEmpty()) {
                user = User(login_text.text.toString(), token = it)

                if (key == "signin") {
                    // TODO: download user info
                }

                val intent = Intent(this, ProcessActivity::class.java)
                startActivity(intent)
            } else {
                val message = if (key == "signin") {
                    "Вы ввели неверные данные для авторизации. Попробуйте снова."
                } else {
                    "Вас не получилось зарегистрировать. Попробуйте снова."
                }
                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}