package com.example.voiceversa.View.Library

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceversa.Model.Audio
import com.example.voiceversa.R
import java.text.SimpleDateFormat
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


        private var audioPlayer: MediaPlayer? = null

        @RequiresApi(Build.VERSION_CODES.O)
        var audio: Audio = Audio()
        val positionBar: SeekBar = itemView.findViewById(R.id.positionBar)
        val playBtn: Button = itemView.findViewById(R.id.playBtn)
        lateinit var elapsedTimeLabel: TextView
        private var totalTime: Int = 0
        lateinit var totalTimeLabel: TextView

        init {}

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
            val name: TextView = itemView.findViewById(R.id.audio_name)
             elapsedTimeLabel = itemView.findViewById(R.id.elapsedTimeLabel)
             totalTimeLabel = itemView.findViewById(R.id.totalTimeLabel)
            val actions: ImageView = itemView.findViewById(R.id.actions)


            val date: TextView = itemView.findViewById(R.id.date)
            val pattern = "dd.MM.yyyy HH:mm"
            val simpleDateFormat = SimpleDateFormat(pattern)
            val formattedDate: String = simpleDateFormat.format(audioItem.date)

           date.text = formattedDate

            name.text = audioItem.title
            elapsedTimeLabel.text = createTimeLabel(0)
            totalTimeLabel.text = createTimeLabel(audioItem.duration)

            getPlayableAudio()
            playBtn.setOnClickListener { playBtnClick(itemView) }
        }

        fun playBtnClick(view: View) {
            if (audioPlayer?.isPlaying == true) {
                audioPlayer!!.pause()
                playBtn.setBackgroundResource(R.drawable.play)

            } else {
                audioPlayer?.start()
                playBtn.setBackgroundResource(R.drawable.stop)
            }
        }

        private fun getPlayableAudio() {
            var resURL = Uri.parse(audio.url)
            audioPlayer = MediaPlayer.create(itemView.context, resURL)
            audioPlayer?.isLooping = false
            audioPlayer?.setVolume(0.5f, 0.5f)
            totalTime = audioPlayer?.duration!!
            totalTimeLabel.text = createTimeLabel(totalTime)

            positionBar.max = totalTime
            positionBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            audioPlayer?.seekTo(progress)
                        }
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                    }
                }
            )

            Thread(Runnable {
                while (audioPlayer != null) {
                    try {
                        var msg = Message()
                        msg.what = audioPlayer?.currentPosition!!
                        handlerAudio.sendMessage(msg)
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                    }
                }
            }).start()

            playBtn.isEnabled = true
        }

        @SuppressLint("HandlerLeak")
        var handlerAudio = object : Handler() {
            override fun handleMessage(msg: Message) {
                var currentPosition = msg.what

                // Update positionBar
                positionBar.progress = currentPosition

                // Update Labels
                var elapsedTime = createTimeLabel(currentPosition)
                elapsedTimeLabel.text = elapsedTime
                if (elapsedTime == createTimeLabel(totalTime)) {
                    playBtn.setBackgroundResource(R.drawable.play)
                }
            }
        }

    }
}