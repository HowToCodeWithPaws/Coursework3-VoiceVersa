package com.example.voiceversa

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class LibraryActivity : AppCompatActivity(),  AdapterView.OnItemSelectedListener {

    //private var _binding: LibraryActivityBinding? = null
    //private val binding get() = _binding!!
    private var listList = ArrayList<ListForRV>()
    private lateinit var nestedListAdapter: NestedListAdapter
    lateinit var rvList: RecyclerView
    lateinit var sortSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        //_binding = LibraryActivityBinding.inflate(layoutInflater)

       setContentView(R.layout.library_activity)
        Objects.requireNonNull(supportActionBar)!!.title = "Библиотека"

   //     listList = ArrayList()
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


        ///todo find audios
        ///todo tune filters and lists

        //return binding.root
    }


//    override fun onDestroy() {
//        super.onDestroy()
//        _binding = null
//    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        refresh(sortSpinner.selectedItem as String)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        refresh("По названию")
    }

    fun refresh(sort:String){
        listList = ArrayList()
        //listList.add()
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