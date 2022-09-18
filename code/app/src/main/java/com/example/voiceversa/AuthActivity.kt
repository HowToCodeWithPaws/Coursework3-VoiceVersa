package com.example.voiceversa

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.voiceversa.Controller.Controller
import com.example.voiceversa.Model.User
import com.example.voiceversa.View.Settings.ProcessActivity
import java.util.*

lateinit var user: User
lateinit var controller: Controller

class AuthActivity : AppCompatActivity() {

    private lateinit var loginText: EditText
    private lateinit var passwordText: EditText
    private lateinit var signInButton: Button
    private lateinit var signUpButton: Button
    private lateinit var guestButton: Button
    private lateinit var eyeButton: Button
    private var showingPassword = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val home = this.externalMediaDirs!![0]!!.absolutePath

        controller = Controller(home)

        setContentView(R.layout.activity_auth)
        Objects.requireNonNull(supportActionBar)!!.title = "VoiceVersa"

        loginText = findViewById(R.id.login)
        passwordText = findViewById(R.id.password)
        signInButton = findViewById(R.id.sign_in)
        signUpButton = findViewById(R.id.sign_up)
        guestButton = findViewById(R.id.guest)
        eyeButton = findViewById(R.id.password_eye)

        passwordText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        checkOnline()
        setListeners()
        checkSaved()
    }

    private fun checkOnline() {
        if (!controller.online) {
            signInButton.isEnabled = false
            signUpButton.isEnabled = false
            Toast.makeText(
                this,
                "К сожалению, вы не подключены к серверу. Вам доступен определенный оффлайн " +
                        "функционал, но для полноценной работы приложения дождитесь, пожалуйста, " +
                        "установки подключения.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setListeners() {

        signInButton.setOnClickListener {
            notGuest("signin")
        }

        signUpButton.setOnClickListener {
            notGuest("signup")
        }

        guestButton.setOnClickListener {
            user = User("", "")
            val intent = Intent(this, ProcessActivity::class.java)
            startActivity(intent)
        }

        eyeButton.setOnClickListener {
            if (!showingPassword) {
                passwordText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                eyeButton.setBackgroundResource(R.drawable.eye_closed)
                showingPassword = true
            } else {
                passwordText.transformationMethod = PasswordTransformationMethod.getInstance()
                eyeButton.setBackgroundResource(R.drawable.eye_open)
                showingPassword = false
            }
        }
    }

    private fun checkSaved() {
        try {
            val sharedPref = this.getSharedPreferences("user", MODE_PRIVATE)
            val nameSaved = sharedPref.getString("name", "").toString()
            val passwordSaved = sharedPref.getString("password", "").toString()
            val tokenSaved = sharedPref.getString("token", "").toString()
            if (nameSaved.isNotEmpty() && passwordSaved.isNotEmpty() && tokenSaved.isNotEmpty()) {
                passwordText.setText(passwordSaved, TextView.BufferType.EDITABLE)
                loginText.setText(nameSaved, TextView.BufferType.EDITABLE)
                controller.token.postValue(tokenSaved)
                notGuest("signin")
            }
        } catch (e: Exception) {
        }
    }

    private fun notGuest(key: String) {
        controller.signInOrUp(
            loginText.text.toString(),
            passwordText.text.toString(),
            key
        ).observe(this) {
            Log.d("AUTH", " $it")
            if (!it.isNullOrEmpty()) {
               proceedAuthorized(it)
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

    private fun proceedAuthorized(token: String){
        user = User(loginText.text.toString(), token = token)

        val sharedPref = this.getSharedPreferences("user", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("name", loginText.text.toString())
            apply()
            putString("password", passwordText.text.toString())
            apply()
            putString("token", token)
            apply()
        }

        val intent = Intent(this, ProcessActivity::class.java)
        startActivity(intent)
    }
}