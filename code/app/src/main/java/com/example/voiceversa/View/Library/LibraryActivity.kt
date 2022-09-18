package com.example.voiceversa.View.Library

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
import com.example.voiceversa.AccountActivity
import com.example.voiceversa.Controller.readAudioNames
import com.example.voiceversa.Model.Audio
import com.example.voiceversa.R
import com.example.voiceversa.controller
import com.example.voiceversa.user
import java.util.*

class LibraryActivity : AppCompatActivity(),  AdapterView.OnItemSelectedListener {

    private var listList = ArrayList<ListForRV>()
    private lateinit var nestedListAdapter: NestedListAdapter
    lateinit var rvList: RecyclerView
    lateinit var sortSpinner: Spinner

    fun getAudios(){
        controller.loadLibrary().observe(this){
            if (!it.isNullOrEmpty()) {
                var array = ArrayList<Audio>()
                for (audio_from_server in it){

                    var name = audio_from_server.audio.name
                    var origin = if (name.contains("recording")) "recording" else "result"
                    array.add(Audio(audio_from_server.id ,name, origin, controller.savedPath + "/" + name + ".mp3"))//TODO: read data like date of creation

                }
                user.audios = array

                //TODO сохранить аудио как файлы в нужную папку - savedPath
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
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
controller.context = this

        getAudios()

      //  var names: ArrayList<String> = readAudioNames(controller.savedPath)
      //  var array = ArrayList<Audio>()

      //  for (name in names) {
      //      var origin = if (name.contains("recording")) "recording" else "result"
      //      array.add(Audio(name, origin, controller.savedPath + "/" + name + ".mp3"))//TODO: read data like date of creation
      //  }

     //   user.audios = array

       setContentView(R.layout.activity_library)
        Objects.requireNonNull(supportActionBar)!!.hide()
        val topbar = findViewById<Toolbar>(R.id.library_top_bar)
        topbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.account) {
                val intent = Intent(this, AccountActivity::class.java)
                startActivity(intent)
            }
            true
        }

         nestedListAdapter = NestedListAdapter(listList)
        rvList = findViewById<RecyclerView>(R.id.rv_list)

     rvList.adapter = nestedListAdapter

        var sorts = arrayListOf<String>("По названию", "По длительности", "По давности")

        var adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, sorts)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        sortSpinner = findViewById<Spinner>(R.id.sort_spinner)
        sortSpinner.adapter = adapter
        sortSpinner.onItemSelectedListener = this

        refresh("По названию")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        refresh(sortSpinner.selectedItem as String)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        refresh("По названию")
    }

    fun refresh(sort:String){
        listList = ArrayList()
        rvList.layoutManager = LinearLayoutManager(this)
        nestedListAdapter = NestedListAdapter(listList)
        rvList.adapter = nestedListAdapter

        val listRecordings = ListForRV(
            "Сохраненные записи", true, sort
        )
        val listResults = ListForRV(
            "Сохраненные результаты", false,  sort
        )

        listList.add(listRecordings)
        listList.add(listResults)
        nestedListAdapter.notifyDataSetChanged()
    }
}