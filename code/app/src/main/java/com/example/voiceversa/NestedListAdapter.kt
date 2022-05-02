package com.example.voiceversa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceversa.databinding.AudioItemBinding
import com.example.voiceversa.databinding.ListItemBinding
import kotlinx.android.synthetic.main.audio_item.*
import java.util.ArrayList


class NestedListAdapter(
    private var listList: List<ListForRV>
) : RecyclerView.Adapter<NestedListAdapter.ViewHolder>() {

    private var thisAdapter: NestedListAdapter? = null
    private var adapter: AudiosListAdapter? = null

    inner class ViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) { 

        thisAdapter = this
        with(holder) {
            with(listList[position]) {
                binding.tvListName.text = this.name
            //    if (listList[position].name == "Текущие дела") {
                    var array = ArrayList<Audio>()
                    array.add(Audio("h1", "recording", 23))
                    array.add(Audio("t2", "recording", 23))
                    array.add(Audio("g3", "recording", 32))
                    //userList.plans.filter { !it.isFinished } as ArrayList<Plan>
                        //  if (this.sort == "по важности") {
                        //    array.sortByDescending { it.importance }
                    //} else {
                        //  array.sortBy { it.deadline }
                   // }
                    adapter =
                        AudiosListAdapter(array)
//                } else {
//                    var array =
//                        ArrayList<Audio>()//userList.plans.filter { !it.isFinished } as ArrayList<Plan>
//                    array.add(Audio("hh", "result", 54))
//                    array.add(Audio("hh1", "result", 54))
//                    array.add(Audio("hh2", "result", 45))
//                    array.add(Audio("hh3", "result", 45))
////                    var array = userList.plans.filter { it.isFinished } as ArrayList<Plan>
//                    if (this.sort == "по важности") {
////                        array.sortByDescending { it.importance }
//                    } else {
////                        array.sortBy { it.deadline }
//                    }
//                    adapter = AudiosListAdapter(array)
//                }
                binding.itemsList.adapter = adapter

                binding.eyeOpen.visibility = if (this.expand) {
                    ImageView.VISIBLE
                } else {
                    ImageView.INVISIBLE
                }

                binding.eyeClosed.visibility = if (!this.expand) {
                    ImageView.VISIBLE
                } else {
                    ImageView.INVISIBLE
                }

                binding.expandedView.visibility = if (this.expand) View.VISIBLE else View.GONE
                binding.cardLayout.setOnClickListener {
                    this.expand = !this.expand
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return listList.size
    }
}