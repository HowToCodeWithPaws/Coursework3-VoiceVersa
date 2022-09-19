package com.example.voiceversa.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.example.voiceversa.*
import com.example.voiceversa.model.User
import java.util.*


class SettingsActivity : AppCompatActivity() {

    private lateinit var logInOutButton: Button
    private lateinit var accountStatus: TextView
    private lateinit var autoSaveRec: SwitchCompat
    private lateinit var autoSaveRes: SwitchCompat
    private lateinit var deleteLibrary: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        Objects.requireNonNull(supportActionBar)!!.hide()
        val topBar = findViewById<Toolbar>(R.id.settings_top_bar)

        topBar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.account) {
                val intent = Intent(this, AccountActivity::class.java)
                startActivity(intent)
            }
            true
        }

        accountStatus = findViewById(R.id.account_status)
        logInOutButton = findViewById(R.id.log_in_out)
        autoSaveRec = findViewById(R.id.autosave_rec)
        autoSaveRes = findViewById(R.id.autosave_res)
        deleteLibrary = findViewById(R.id.delete_library)

        checkOnline()
        refresh()
        setLogInOut()
        setListeners()
    }

    private fun checkOnline() {
        if (!controller.online) {
            logInOutButton.isEnabled = false
            deleteLibrary.isEnabled = false
            Toast.makeText(
                this,
                "К сожалению, вы не подключены к серверу. Вам доступен определенный оффлайн " +
                        "функционал, но для полноценной работы приложения дождитесь, пожалуйста, " +
                        "установки подключения.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setLogInOut() {
        logInOutButton.setOnClickListener {
            if (user.name.isEmpty()) {
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Внимание! Это действие будет иметь последствия.")
                    .setMessage(
                        "Вы точно хотите выйти из аккаунта? Данные приложения больше не" +
                                " будут синхронизироваться."
                    )
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(
                        android.R.string.yes
                    ) { _, _ ->
                        logOut()
                    }
                    .setNegativeButton(android.R.string.no, null).show()
            }
        }
    }

    private fun logOut() {
        user = User("")

        val sharedPref = this.getSharedPreferences("user", MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("name")
            apply()
            remove("password")
            apply()
            remove("token")
            apply()
        }

        Toast.makeText(
            this,
            "Вы вышли из аккаунта, теперь вы используете приложение в режиме гостя.",
            Toast.LENGTH_SHORT
        ).show()
        refresh()
    }

    private fun setListeners() {
        autoSaveRec.isChecked = user.autoSaveRec
        autoSaveRes.isChecked = user.autoSaveRes

        autoSaveRes.setOnCheckedChangeListener { _, isChecked ->
            user.autoSaveRes = isChecked
        }

        autoSaveRec.setOnCheckedChangeListener { _, isChecked ->
            user.autoSaveRec = isChecked
        }
        setLibraryDelete()
    }

    private fun setLibraryDelete() {
        deleteLibrary.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Внимание! Это действие будет иметь последствия.")
                .setMessage(
                    "Вы точно хотите очистить библиотеку? Вы потеряете все несохраненные " +
                            "аудиозаписи приложения."
                )
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(
                    android.R.string.yes
                ) { _, _ ->
                    controller.deleteLibrary()
                    Toast.makeText(
                        this,
                        "Библиотека очищена",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton(android.R.string.no, null).show()
        }
    }

    private fun refresh() {
        if (user.name.isEmpty()) {
            accountStatus.text = "Вы не вошли в аккаунт"
            logInOutButton.text = "войти"
        } else {
            accountStatus.text = "Вы вошли в аккаунт: \n${user.name}"
            logInOutButton.text = "выйти"
        }
    }
}