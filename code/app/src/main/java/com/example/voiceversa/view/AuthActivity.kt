package com.example.voiceversa.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.voiceversa.R
import com.example.voiceversa.controller.Controller
import com.example.voiceversa.model.User
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
        println("\n\n\n\n controller "+ controller+"\n\n\n\n\n")

        setContentView(R.layout.activity_auth)
        Objects.requireNonNull(supportActionBar)!!.title = "VoiceVersa"

        loginText = findViewById(R.id.login)
        passwordText = findViewById(R.id.password)
        signInButton = findViewById(R.id.sign_in)
        signUpButton = findViewById(R.id.sign_up)
        guestButton = findViewById(R.id.guest)
        eyeButton = findViewById(R.id.password_eye)

        passwordText.transformationMethod = PasswordTransformationMethod.getInstance()

        checkOnline()
        setListeners()
        checkSaved()
    }

    private fun checkOnline() {
     //   controller.loadLibrary()
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
            notGuest("signIn")
        }
        signUpButton.setOnClickListener {
            notGuest("signUp")
        }

        guestButton.setOnClickListener {
            user = User("")
            controller.online = false
            val sharedPref = this.getSharedPreferences("user", MODE_PRIVATE)
            getAutoSavedPrefs(sharedPref)
            println("LOOK HERE AUDIOS GUEST "+ user.audios.size)
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
                notGuest("signIn")
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
                controller.online = false
                val message = if (key == "signIn") {
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

    private fun proceedAuthorized(token: String) {
        user = User(loginText.text.toString())

        val sharedPref = this.getSharedPreferences("user", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("name", loginText.text.toString())
            apply()
            putString("password", passwordText.text.toString())
            apply()
            putString("token", token)
            apply()
        }

       getAutoSavedPrefs(sharedPref)

        println("LOOK HERE AUDIOS AUTH "+ user.audios.size)
        controller.online = true
        val intent = Intent(this, ProcessActivity::class.java)
        startActivity(intent)
    }

    private fun getAutoSavedPrefs(sharedPref : SharedPreferences){
        try {
            val autoSaveRes = sharedPref.getString("autoSaveRes", "").toString()
            val autoSaveRec = sharedPref.getString("autoSaveRec", "").toString()
            user.autoSaveRec = autoSaveRec == "true"
            user.autoSaveRes = autoSaveRes == "true"
        } catch (e: Exception) {
        }
    }
}