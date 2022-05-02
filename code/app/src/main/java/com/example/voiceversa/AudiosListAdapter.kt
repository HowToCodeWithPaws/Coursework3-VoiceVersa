package com.example.voiceversa

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class AudiosListAdapter(private val audios: ArrayList<Audio>) :
    RecyclerView.Adapter<AudiosListAdapter.ListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {

        var view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.audio_item, parent, false)

        return ListViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int {
        println("size = ${audios.size}")
        return audios.size
    }

    private fun getItem(position: Int): Audio {
        return audios[position];
    }

    class ListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        @RequiresApi(Build.VERSION_CODES.O)
       var audio: Audio = Audio()

        init {
           // itemView.setOnClickListener {
         //       openDeadlineScreenEdit(audio)
            //}
        }

        fun createTimeLabel(time: Int): String {
            var timeLabel = ""
            var min = time / 1000 / 60
            var sec = time / 1000 % 60

            timeLabel = "$min:"
            if (sec < 10) timeLabel += "0"
            timeLabel += sec

            return timeLabel
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(audioItem: Audio) {

            audio = audioItem
           // val rating: RatingBar = itemView.findViewById(R.id.ratingBar)
            val name: TextView = itemView.findViewById(R.id.audio_name)
            val elapsedTimeLabel: TextView = itemView.findViewById(R.id.elapsedTimeLabel)
            val totalTimeLabel: TextView = itemView.findViewById(R.id.totalTimeLabel)
            val actions: ImageView = itemView.findViewById(R.id.actions)
            val positionBar: SeekBar = itemView.findViewById(R.id.positionBar)
            val playBtn: Button = itemView.findViewById(R.id.playBtn)

            name.text = audioItem.title
            print("!!!!LOOK  "+audioItem.title)
            elapsedTimeLabel.text = createTimeLabel(0)
            totalTimeLabel.text= createTimeLabel(audioItem.duration)
          //  rating.rating = deadlineItem.importance.toFloat()

          //  val date: TextView = itemView.findViewById(R.id.date)
          //  val deadline: TextView = itemView.findViewById(R.id.deadline)
          //  var formattedDate = deadlineItem.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
          //  var formattedDeadline =
          //      deadlineItem.deadline.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        }


//        fun openDeadlineScreenEdit(deadline: Audio) {
//            deadlineToEdit = deadline
//            userToEdit = userList
//            val intent = Intent(parentActivityList, DeadlineEditActivity::class.java)
//            intent.putExtra("deadline", deadline)
//            parentActivityList.startActivity(intent)
//        }
    }
}