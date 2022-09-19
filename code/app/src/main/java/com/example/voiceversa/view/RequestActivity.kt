package com.example.voiceversa.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.voiceversa.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.util.*

class RequestActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var sendAudio: Button
    private lateinit var sendArchive: TextView
    private lateinit var startRecBtn: Button
    private lateinit var attachRecBtn: Button
    private lateinit var pauseRecBtn: Button
    lateinit var playRecBtn: Button
    lateinit var positionRecBar: SeekBar
    lateinit var elapsedTimeLabelRec: TextView
    private lateinit var totalTimeLabelRec: TextView
    private lateinit var archiveName: TextView
    private lateinit var attachArchive: Button
    private lateinit var requestName: EditText

    private var mediaRecorder: MediaRecorder? = null
    private var recPlayer: MediaPlayer? = null
    private var chosenRec: String? = null
    private var totalTime: Int = 0
    private var state: Boolean = false
    private var recordingStopped: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_request)
        Objects.requireNonNull(supportActionBar)!!.hide()
        val topBar = findViewById<Toolbar>(R.id.request_top_bar)
        topBar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.account) {
                val intent = Intent(this, AccountActivity::class.java)
                startActivity(intent)
            }
            true
        }

        attachRecBtn = findViewById(R.id.request_attachRecBtn)
        playRecBtn = findViewById(R.id.request_playRecBtn)
        positionRecBar = findViewById(R.id.request_positionRecBar)
        elapsedTimeLabelRec = findViewById(R.id.request_elapsedTimeLabelRec)
        totalTimeLabelRec = findViewById(R.id.request_totalTimeLabelRec)
        archiveName = findViewById(R.id.request_archive_name)
        startRecBtn = findViewById(R.id.request_startRecBtn)
        pauseRecBtn = findViewById(R.id.request_pauseRecBtn)
        attachArchive = findViewById(R.id.request_attach_archive)
        requestName = findViewById(R.id.request_name)
        sendAudio = findViewById(R.id.send_audio)
        sendArchive = findViewById(R.id.send_archive)

        sendAudio.isEnabled = false
        sendArchive.isEnabled = false
        playRecBtn.isEnabled = false

        checkOnline()
        setListeners()
    }

    private fun checkOnline() {
        if (!controller.online) {
            Toast.makeText(
                this,
                "К сожалению, вы не подключены к серверу. Вам доступен определенный оффлайн " +
                        "функционал, но для полноценной работы приложения дождитесь, пожалуйста, " +
                        "установки подключения.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun mediaRecorderInit() {
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(controller.requestRecordingPath)
        mediaRecorder?.prepare()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            if (v.id == R.id.request_attachRecBtn) {
                val intent = Intent()
                intent.type = "audio/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Выбор аудиофайла"), 1)

            } else if (v.id == R.id.request_attach_archive) {
                val intent = Intent()
                intent.type = "application/zip"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Выбор архива"), 2)

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                getFileFromStorage(data, controller.requestRecordingPath, "аудиозапись")
            } else {
                Toast.makeText(this, "Пожалуйста, выберите аудиозапись!", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                getFileFromStorage(data, controller.requestArchivePath, "архив")
            } else {
                Toast.makeText(this, "Пожалуйста, выберите архив!", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getFileFromStorage(data: Intent?, destinationPath: String, key: String) {
        try {
            val uri: Uri = data?.data!!
            val src = uri.path!!
            var subSrc = ""

            if (src.contains("storage")) {
                subSrc = src.subSequence(src.indexOf("storage") - 1, src.length).toString()
            } else if (src.contains("primary")) {
                subSrc =
                    "/storage/emulated/0/" + src.subSequence(src.indexOf("primary") + 8, src.length)
                        .toString()
            }

            val source: File = File(subSrc).absoluteFile
            val destination = File(destinationPath)

            copy(source, destination)
            val name = source.name
            Toast.makeText(this, "Вы выбрали $key $name", Toast.LENGTH_SHORT).show()
            if (key == "аудиозапись") {
                getPlayableRecording()
                sendAudio.isEnabled = controller.online
            } else {
                archiveName.text = name
                sendArchive.isEnabled = controller.online
            }
        } catch (e: Exception) {
            println("Error in getting file ")
            e.printStackTrace()
            Toast.makeText(
                this,
                "Не вышло открыть $key! Попробуйте открыть другой файл.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun copy(source: File, destination: File) {
        val inp: FileChannel = FileInputStream(source).channel
        val out: FileChannel = FileOutputStream(destination).channel

        try {
            inp.transferTo(0, inp.size(), out)
        } catch (e: Exception) {
            println("Error in copying the file\n")
            e.printStackTrace()
        } finally {
            inp.close()
            out.close()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setListeners() {
        startRecBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissions = arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                ActivityCompat.requestPermissions(this, permissions, 0)
            } else {
                startRecording()
            }
        }

        pauseRecBtn.setOnClickListener {
            pauseRecording()
        }

        attachRecBtn.setOnClickListener(this)
        attachArchive.setOnClickListener(this)
    }


    @SuppressLint("RestrictedApi", "SetTextI18n")
    private fun pauseRecording() {
        if (state) {
            if (!recordingStopped) {
                Toast.makeText(this, "Аудиозапись приостановлена", Toast.LENGTH_SHORT).show()
                mediaRecorder?.pause()
                recordingStopped = true
                pauseRecBtn.setBackgroundResource(R.drawable.play)
            } else {
                resumeRecording()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRecording() {
        if (state) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
            Toast.makeText(this, "Аудиозапись закончена", Toast.LENGTH_SHORT).show()
            chosenRec = controller.requestRecordingPath
            pauseRecBtn.setBackgroundResource(R.drawable.stop)
            recordingStopped = false
            startRecBtn.setBackgroundResource(R.drawable.mic)
            getPlayableRecording()
            sendAudio.isEnabled = controller.online
        } else {
            try {
                mediaRecorderInit()
                mediaRecorder?.start()
                state = true
                playRecBtn.isEnabled = false
                Toast.makeText(this, "Аудиозапись начата", Toast.LENGTH_SHORT).show()
                startRecBtn.setBackgroundResource(R.drawable.finish)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    private fun resumeRecording() {
        Toast.makeText(this, "Аудиозапись продолжена", Toast.LENGTH_SHORT).show()
        mediaRecorder?.resume()
        pauseRecBtn.setBackgroundResource(R.drawable.stop)
        recordingStopped = false
    }

    @SuppressLint("HandlerLeak")
    var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val currentPosition = msg.what

            positionRecBar.progress = currentPosition

            val elapsedTime = createTimeLabel(currentPosition)
            elapsedTimeLabelRec.text = elapsedTime
            if (elapsedTime == createTimeLabel(totalTime)) {
                playRecBtn.setBackgroundResource(R.drawable.play)
            }
        }
    }

    fun createTimeLabel(time: Int): String {
        var timeLabel: String
        val min = time / 1000 / 60
        val sec = time / 1000 % 60

        timeLabel = "$min:"
        if (sec < 10) timeLabel += "0"
        timeLabel += sec

        return timeLabel
    }

    fun playRecBtnClick(v: View) {
        if (recPlayer?.isPlaying == true) {
            recPlayer!!.pause()
            playRecBtn.setBackgroundResource(R.drawable.play)

        } else {
            recPlayer?.start()
            playRecBtn.setBackgroundResource(R.drawable.stop)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPlayableRecording() {
        val recURL = Uri.parse(controller.requestRecordingPath)
        recPlayer = MediaPlayer.create(this, recURL)
        recPlayer?.isLooping = false
        recPlayer?.setVolume(0.5f, 0.5f)
        totalTime = recPlayer?.duration!!
        totalTimeLabelRec.text = createTimeLabel(totalTime)

        positionRecBar.max = totalTime
        positionRecBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        recPlayer?.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(p0: SeekBar?) {}
            }
        )

        Thread {
            while (recPlayer != null) {
                try {
                    val msg = Message()
                    msg.what = recPlayer?.currentPosition!!
                    handler.sendMessage(msg)
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                }
            }
        }.start()

        playRecBtn.isEnabled = true
    }

    private fun checkName(): Boolean {
        return if (requestName.text.toString().isEmpty()) {
            Toast.makeText(this, "Введите название заявки", Toast.LENGTH_SHORT).show()
            false
        } else true
    }

    fun sendArchive(view: View) {
        if (checkName()) {
            controller.sendRequest("archive" + requestName.text.toString()).observe(this) {
                if (it != null) {
                    Toast.makeText(this, "Заявка с архивом отправлена", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "Не удалось отправить заявку с архивом! Попробуйте в другой раз.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun sendAudio() {
        if (checkName()) {
            controller.sendRequest("audio" + requestName.text.toString()).observe(this) {
                if (it != null) {
                    Toast.makeText(this, "Заявка с аудиозаписью отправлена", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        this,
                        "Не удалось отправить заявку с аудиозаписью! Попробуйте в другой раз.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}