package com.example.voiceversa.view.libraryActivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceversa.model.Audio
import com.example.voiceversa.databinding.ListItemBinding
import com.example.voiceversa.user
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

                setUpArray(this, position)

                binding.tvListName.text = this.name
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

    private fun setUpArray(list: ListForRV, position: Int) {

        val array: ArrayList<Audio> = if (listList[position].name == "Сохраненные записи") {
            user.audios.filter { it.source == "recording" } as ArrayList<Audio>
        } else {
            user.audios.filter { it.source == "result" } as ArrayList<Audio>
        }

        when (list.sort) {
            "По названию" -> {
                array.sortBy { it.title }
            }
            "По длительности" -> {
                array.sortBy { it.duration }
            }
            "По давности" -> {
                array.sortBy { it.date }
            }
        }

        adapter = AudiosListAdapter(array)
    }

    override fun getItemCount(): Int {
        return listList.size
    }
}