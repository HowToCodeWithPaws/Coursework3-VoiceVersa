package com.example.voiceversa.View.Library

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceversa.R
import java.util.*

class LibraryActivity : AppCompatActivity(),  AdapterView.OnItemSelectedListener {

    private var listList = ArrayList<ListForRV>()
    private lateinit var nestedListAdapter: NestedListAdapter
    lateinit var rvList: RecyclerView
    lateinit var sortSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

       setContentView(R.layout.activity_library)
        Objects.requireNonNull(supportActionBar)!!.title = "Библиотека"

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