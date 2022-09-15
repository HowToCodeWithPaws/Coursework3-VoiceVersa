package com.example.voiceversa.View.Settings

import android.app.AlertDialog
import android.content.DialogInterface
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
import com.example.voiceversa.Model.User
import kotlinx.android.synthetic.main.activity_auth.view.*
import java.util.*


class SettingsActivity : AppCompatActivity() {

    lateinit var log_in_out_button : Button
    lateinit var account_status : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        Objects.requireNonNull(supportActionBar)!!.hide()
        val topbar = findViewById<Toolbar>(R.id.settings_top_bar)
        topbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.account) {
                val intent = Intent(this, AccountActivity::class.java)
                startActivity(intent)
            }
            true
        }

         account_status = findViewById<TextView>(R.id.account_status)

         log_in_out_button = findViewById<Button>(R.id.log_in_out)
        val autosave_rec = findViewById<SwitchCompat>(R.id.autosave_rec)
        val autosave_res = findViewById<SwitchCompat>(R.id.autosave_res)
        val delete_library = findViewById<Button>(R.id.delete_library)

        if (!controller.online){
            log_in_out_button.isEnabled = false
            delete_library.isEnabled = false
            Toast.makeText(
                this,
                "К сожалению, вы не подключены к серверу. Вам доступен определенный оффлайн " +
                        "функционал, но для полноценной работы приложения дождитесь, пожалуйста, " +
                        "установки подключения.",
                Toast.LENGTH_SHORT
            ).show()
        }


    refresh()

        log_in_out_button.setOnClickListener{
            if (user.name.isEmpty()){
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
            }else{

                AlertDialog.Builder(this)
                    .setTitle("Внимание! Это действие будет иметь последствия.")
                    .setMessage("Вы точно хотите выйти из аккаунта? Данные приложения больше не" +
                            " будут синхронизироваться.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes,
                        DialogInterface.OnClickListener { dialog, whichButton ->
                            user = User("")
                            Toast.makeText(
                                this,
                                "Вы вышли из аккаунта, теперь вы используете приложение в режиме гостя.",
                                Toast.LENGTH_SHORT
                            ).show()
                            refresh()
                        })
                    .setNegativeButton(android.R.string.no, null).show()
            }
        }

        autosave_rec.isChecked = user.autosaveRec
        autosave_res.isChecked = user.autosaveRes

        autosave_res.setOnCheckedChangeListener { buttonView, isChecked ->
            user.autosaveRes = isChecked
        }
        autosave_rec.setOnCheckedChangeListener { buttonView, isChecked ->
            user.autosaveRec = isChecked
        }

        delete_library.setOnClickListener{

            AlertDialog.Builder(this)
                .setTitle("Внимание! Это действие будет иметь последствия.")
                .setMessage("Вы точно хотите очистить библиотеку? Вы потеряете все несохраненные " +
                        "аудиозаписи приложения.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes,
                    DialogInterface.OnClickListener { dialog, whichButton ->
                        controller.deleteLibrary()
                        Toast.makeText(
                            this,
                            "Библиотека очищена",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                .setNegativeButton(android.R.string.no, null).show()
        }
    }

    fun refresh(){
        if(user.name.isEmpty()){
            account_status.text = "Вы не вошли в аккаунт"
            log_in_out_button.text = "войти"
        }else{
            account_status.text = "Вы вошли в аккаунт: \n${user.name}"
            log_in_out_button.text = "выйти"
        }
    }
}