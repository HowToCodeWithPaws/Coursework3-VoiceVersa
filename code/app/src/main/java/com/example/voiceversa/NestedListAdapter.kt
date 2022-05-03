package com.example.voiceversa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceversa.databinding.ListItemBinding
import kotlinx.android.synthetic.main.list_item.view.*
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
                var array : ArrayList<Audio>? = null
                if (listList[position].name == "Сохраненные записи") {
                    array = user.audios.filter { it.source == "recording" } as ArrayList<Audio>
                } else {
                    array = user.audios.filter { it.source == "result" } as ArrayList<Audio>
                }

                if (this.sort == "По названию") {
                    array.sortBy { it.title }
                } else if (this.sort == "По длительности"){
                    array.sortBy { it.duration }
                }else if (this.sort == "По давности"){
                    array.sortBy { it.date }}

                adapter = AudiosListAdapter(array)
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