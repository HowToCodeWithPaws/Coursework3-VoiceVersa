package com.example.voiceversa

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.voiceversa.databinding.ActivityLibraryBinding
import java.util.*

class LibraryActivity : AppCompatActivity(),  AdapterView.OnItemSelectedListener {

    private var _binding: ActivityLibraryBinding? = null
    private val binding get() = _binding!!
    private var listList = ArrayList<ListForRV>()
    private lateinit var nestedListAdapter: NestedListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLibraryBinding.inflate(layoutInflater)

        setContentView(R.layout.activity_library)
        Objects.requireNonNull(supportActionBar)!!.title = "библиотека"

   //     listList = ArrayList()

     //   nestedListAdapter = NestedListAdapter(listList)
    //    binding.rvList.adapter = nestedListAdapter

        var sorts = arrayListOf<String>("!", "по срочности", "по важности")

        var adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, sorts)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.sortSpinner.adapter = adapter
        binding.sortSpinner.onItemSelectedListener = this
        refresh("по срочности")


        ///todo find audios
        ///todo tune filters and lists
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        refresh(binding.sortSpinner.selectedItem as String)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        refresh("по срочности")
    }

    fun refresh(sort:String){
        listList = ArrayList()
        binding.rvList.layoutManager = LinearLayoutManager(this)



        val listCurrent = ListForRV(
            "Текущие дела", true, sort
        )
        val listFinished = ListForRV(
            "Завершенные дела", false,  sort
        )

        listList.add(listCurrent)
        listList.add(listFinished)
        nestedListAdapter = NestedListAdapter(listList)
        binding.rvList.adapter = nestedListAdapter
        nestedListAdapter.notifyDataSetChanged()
    }
}