package com.example.voiceversa.View.Library

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
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
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceversa.BuildConfig
import com.example.voiceversa.Model.Audio
import com.example.voiceversa.R
import com.example.voiceversa.controller
import com.example.voiceversa.user
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class AudiosListAdapter(private val audios: ArrayList<Audio>) :
    RecyclerView.Adapter<AudiosListAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {

        var view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.audio_item, parent, false)

        return ListViewHolder(view,this, audios )
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

    class ListViewHolder(itemView: View, var parentAdapter: AudiosListAdapter, var audios: ArrayList<Audio>) :
        RecyclerView.ViewHolder(itemView) {

        private var audioPlayer: MediaPlayer? = null

        @RequiresApi(Build.VERSION_CODES.O)
        var audio: Audio = Audio()
        val positionBar: SeekBar = itemView.findViewById(R.id.positionBar)
        val playBtn: Button = itemView.findViewById(R.id.playBtn)
        lateinit var elapsedTimeLabel: TextView
        private var totalTime: Int = 0
        lateinit var totalTimeLabel: TextView
        private val menuButton = itemView.findViewById<ImageView>(R.id.actions)
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

            menuButton.setOnClickListener {
                val menu = PopupMenu(controller.context, it)
                menu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.delete -> {

                                if( controller.deleteAudio(audio, user)){
                                    var index = audios.indexOf(audio)
                                    audios.removeAt(index)
                                    Toast.makeText(
                                        controller.context,
                                        "Аудио удалено",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    parentAdapter.notifyItemRemoved(index)
                                    parentAdapter.notifyItemRangeChanged(position, audios.size);
                                }else{
                                Toast.makeText(
                                    controller.context,
                                    "Не удалось удалить аудио из библиотеки, попробуйте в другой раз!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            true
                        }
                        R.id.download -> {
                            try{
                                controller.downloadAudio(audio.url, audio.title+".mp3")
                                Toast.makeText(
                                    controller.context,
                                    "Аудио сохранено в загрузки",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            catch (e: Exception){
                                println("Exception while saving audio")
                                e.printStackTrace()
                                Toast.makeText(
                                    controller.context,
                                    "Не удалось сохранить аудио в загрузки, попробуйте в другой раз!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            true
                        }
                        R.id.share -> {
                            try {
                                val file = File(audio.url)
                                if(file.exists()) {
                                    val uri = FileProvider.getUriForFile(controller.context!!, BuildConfig.APPLICATION_ID + ".provider", file)
                                    val intent = Intent(Intent.ACTION_SEND)
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    intent.setType("audio/mp3")
                                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                   controller.context!!.startActivity(intent)
                                }
                            }  catch (e: Exception){
                                println("Exception while sharing audio")
                                e.printStackTrace()
                                Toast.makeText(
                                    controller.context,
                                    "Не удалось отправить аудио, попробуйте в другой раз!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            true
                        }
                        else -> {
                            false
                        }
                    }
                }
                menu.inflate(R.menu.library_actions_menu)

                menu.show()
            }
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