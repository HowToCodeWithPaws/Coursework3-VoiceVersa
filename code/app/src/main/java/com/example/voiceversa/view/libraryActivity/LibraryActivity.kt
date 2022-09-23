package com.example.voiceversa.view.libraryActivity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceversa.R
import com.example.voiceversa.model.Audio
import com.example.voiceversa.serverClasses.AudioFromServer
import com.example.voiceversa.serverClasses.AudioListResponse
import com.example.voiceversa.view.AccountActivity
import com.example.voiceversa.view.controller
import com.example.voiceversa.view.user
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class LibraryActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var nestedListAdapter: NestedListAdapter
    private lateinit var rvList: RecyclerView
    private lateinit var sortSpinner: Spinner
    private var listList = ArrayList<ListForRV>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller.context = this

        setContentView(R.layout.activity_library)
        Objects.requireNonNull(supportActionBar)!!.hide()
        val topBar = findViewById<Toolbar>(R.id.library_top_bar)
        topBar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.account) {
                val intent = Intent(this, AccountActivity::class.java)
                startActivity(intent)
            }
            true
        }

        if (!controller.online) {
            Toast.makeText(
                this,
                "К сожалению, вы не подключены к серверу. Вам доступен определенный оффлайн " +
                        "функционал, но для полноценной работы приложения дождитесь, пожалуйста, " +
                        "установки подключения.",
                Toast.LENGTH_SHORT
            ).show()
        }


        println("LOOK HERE IN LIB " + user.audios.size)
            //   if(user.audios.isEmpty()){
            getAudios()
            val timer = object : CountDownTimer(5000, 1000) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    println("LOOK HERE IN LIB AFTER DOWNLOAD" + user.audios.size)
                    setUp()
                    refresh("По названию")
                }
            }
            timer.start()
       // }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAudios() {
        controller.loadLibrary().observe(this) {
            if (it != null && it.results.isNotEmpty()) {
                println("LOOK HERE size res"+ it.results.size + " " + it.results.toString())
                downloadAllFromArray(it)
            } else {
                Toast.makeText(
                    this,
                    "Не получилось загрузить библиотеку с сервера! Попробуйте в другой раз",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun downloadAllFromArray(list: AudioListResponse<AudioFromServer>) {
        user.audios = ArrayList<Audio>()
        for (audio_from_server in list.results) {
            print("HERE "+ audio_from_server.id)
            val name = audio_from_server.audio.name
            val origin = if (audio_from_server.is_processed) "result" else "recording"

            controller.downloadAudioByURL(
                audio_from_server.audio.url,
                controller.savedPath + "/" + origin+   audio_from_server.id+ ".mp3"
            ).observe(this) {
                if (it) {
                    var add = true
                    for (audio in user.audios){
                        if (audio.ID == audio_from_server.id){
                            add = false
                        }
                    }
                    if(add){
                    user.audios.add(
                        Audio(
                            audio_from_server.id,
                            origin+   audio_from_server.id,
                            origin,
                            controller.savedPath + "/" + origin+   audio_from_server.id+ ".mp3", 0,
                            Date.from((ZonedDateTime.parse(audio_from_server.created)).toInstant())
                        )
                    )}
                } else if(it == false) {
                    Toast.makeText(
                        this,
                        "К сожалению, не удалось загрузить голос $name с сервера. Попробуйте позже.",
                        Toast.LENGTH_LONG
                    ).show()
                    }
                }
            }
    }

    private fun setUp() {
        nestedListAdapter = NestedListAdapter(listList)
        rvList = findViewById(R.id.rv_list)
        rvList.adapter = nestedListAdapter

        val sorts = arrayListOf("По названию", "По длительности", "По давности")

        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, sorts)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        sortSpinner = findViewById(R.id.sort_spinner)
        sortSpinner.adapter = adapter
        sortSpinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        refresh(sortSpinner.selectedItem as String)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        refresh("По названию")
    }

    private fun refresh(sort: String) {
        listList = ArrayList()
        rvList.layoutManager = LinearLayoutManager(this)
        nestedListAdapter = NestedListAdapter(listList)
        rvList.adapter = nestedListAdapter

        val listRecordings = ListForRV(
            "Сохраненные записи", true, sort
        )
        val listResults = ListForRV(
            "Сохраненные результаты", false, sort
        )

        listList.add(listRecordings)
        listList.add(listResults)
        nestedListAdapter.notifyDataSetChanged()
    }
}