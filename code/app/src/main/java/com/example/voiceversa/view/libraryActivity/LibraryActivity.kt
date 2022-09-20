package com.example.voiceversa.view.libraryActivity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceversa.view.AccountActivity
import com.example.voiceversa.model.Audio
import com.example.voiceversa.serverClasses.AudioFromServer
import com.example.voiceversa.serverClasses.AudioListResponse
import com.example.voiceversa.R
import com.example.voiceversa.view.controller
import com.example.voiceversa.view.user
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
        getAudios()

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
        setUp()
        refresh("По названию")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAudios() {
        controller.loadLibrary().observe(this) {
            if (it != null && it.results.isNotEmpty()) {
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
        val array = ArrayList<Audio>()
        for (audio_from_server in list.results) {

            val name = audio_from_server.audio.name
            val origin = if (name.contains("recording")) "recording" else "result"


            println(audio_from_server.audio.url)
            controller.downloadAudioByURL(
                audio_from_server.audio.url,
                controller.savedPath + "/" + name + ".mp3"
            ).observe(this) {
                if (it == false) {
                    Toast.makeText(
                        this,
                        "К сожалению, не удалось загрузить голос $name с сервера. Попробуйте позже.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    array.add(
                        Audio(
                            audio_from_server.id,
                            name,
                            origin,
                            controller.savedPath + "/" + name + ".mp3"
                        )
                    )
                }
            }
        }
        user.audios = array
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