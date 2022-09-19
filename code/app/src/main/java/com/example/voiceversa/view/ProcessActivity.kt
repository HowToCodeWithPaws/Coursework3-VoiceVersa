package com.example.voiceversa.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.voiceversa.*
import com.example.voiceversa.controller.readAudioNames
import com.example.voiceversa.model.Audio
import com.example.voiceversa.serverClasses.AudioListResponse
import com.example.voiceversa.serverClasses.VoiceFromServer
import java.io.*
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*


class ProcessActivity : AppCompatActivity(), View.OnClickListener,
    AdapterView.OnItemSelectedListener {

    private lateinit var actionsRec: ImageView
    private lateinit var actionsRes: ImageView
    private lateinit var startRecBtn: Button
    private lateinit var attachRecBtn: Button
    private lateinit var pauseRecBtn: Button
    private lateinit var voiceSpinner: Spinner
    private lateinit var processBtn: Button
    lateinit var playRecBtn: Button
    lateinit var playResBtn: Button
    lateinit var playVoiceBtn: Button
    lateinit var positionRecBar: SeekBar
    lateinit var positionResBar: SeekBar
    lateinit var elapsedTimeLabelRec: TextView
    lateinit var elapsedTimeLabelRes: TextView
    private lateinit var totalTimeLabelRec: TextView
    private lateinit var totalTimeLabelRes: TextView
    private var recPlayer: MediaPlayer? = null
    private var resPlayer: MediaPlayer? = null
    private var voicePlayer: MediaPlayer? = null
    private var totalTime: Int = 0
    private var totalTimeRes: Int = 0
    private var totalTimeVoice: Int = 0

    private var processed: Boolean = false
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false
    private var chosenRec: String? = null
    private var voice: String = ""


    @RequiresApi(31)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_process)
        Objects.requireNonNull(supportActionBar)!!.hide()
        val topBar = findViewById<Toolbar>(R.id.top_bar)
        topBar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.account) {
                val intent = Intent(this, AccountActivity::class.java)
                startActivity(intent)
            }
            true
        }

        voiceSpinner = findViewById(R.id.voicesSpinner)
        attachRecBtn = findViewById(R.id.attachRecBtn)
        processBtn = findViewById(R.id.processBtn)
        playRecBtn = findViewById(R.id.playRecBtn)
        playResBtn = findViewById(R.id.playResBtn)
        playVoiceBtn = findViewById(R.id.playVoiceBtn)
        positionRecBar = findViewById(R.id.positionRecBar)
        positionResBar = findViewById(R.id.positionResBar)
        elapsedTimeLabelRec = findViewById(R.id.elapsedTimeLabelRec)
        elapsedTimeLabelRes = findViewById(R.id.elapsedTimeLabelRes)
        totalTimeLabelRec = findViewById(R.id.totalTimeLabelRec)
        totalTimeLabelRes = findViewById(R.id.totalTimeLabelRes)
        actionsRec = findViewById(R.id.actionsRec)
        actionsRes = findViewById(R.id.actionsRes)
        startRecBtn = findViewById(R.id.startRecBtn)
        pauseRecBtn = findViewById(R.id.pauseRecBtn)

        getPermissions()
        setListeners()
        getVoices()
        setEnabled()
        setMenuListeners()
    }

    private fun setEnabled() {
        playRecBtn.isEnabled = false
        playResBtn.isEnabled = false
        actionsRec.isEnabled = false
        actionsRes.isEnabled = false
        processBtn.isEnabled = false

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getVoices() {
        controller.loadVoices().observe(this) { list ->
            if (list.results.isNotEmpty()) {
                downloadAllInArray(list)

            } else {

                Toast.makeText(
                    this,
                    "Не получилось загрузить голоса с сервера! Попробуйте в другой раз",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun downloadAllInArray(list: AudioListResponse<VoiceFromServer>) {
        val array = ArrayList<Audio>()
        for (voice_from_server in list.results) {
            val origin = "voice"
            val name = voice_from_server.name

            controller.downloadAudioByURL(
                voice_from_server.url,
                controller.voicesPath + "/" + name + ".mp3"
            ).observe(this) {
                if (it == false) {
                    Toast.makeText(
                        this,
                        "К сожалению, не удалось загрузить голос $name с сервера. Попробуйте позже.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    array.add(
                        Audio(
                            voice_from_server.id,
                            name,
                            origin,
                            controller.voicesPath + "/" + name + ".mp3"
                        )
                    )
                }
            }
        }
        user.voices = array

        val timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                setVoices()
            }
        }
        timer.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setVoices() {
        val voices = readAudioNames(controller.voicesPath)
        playVoiceBtn.isEnabled = voices.isNotEmpty()

        val adapterVoices =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                voices
            )
        adapterVoices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        voiceSpinner.adapter = adapterVoices
        voiceSpinner.onItemSelectedListener = this
    }

    private fun getPermissions() {
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
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setListeners() {
        startRecBtn.setOnClickListener {
            startRecording()
        }

        pauseRecBtn.setOnClickListener {
            pauseRecording()
        }

        attachRecBtn.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setMenuListeners() {
        actionsRec.setOnClickListener {
            val menu = PopupMenu(this, it)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.download -> {
                        downloadAudio(
                            "Recording", controller.recordingPath,
                            "Запись сохранена в загрузки", "запись"
                        )
                        true
                    }
                    R.id.add -> {
                        addAudioToLibrary(
                            "recording", "Запись добавлена в библиотеку",
                            "запись"
                        )
                        true
                    }
                    R.id.share -> {
                        shareAudio(controller.recordingPath, "recording", "запись")
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
            menu.inflate(R.menu.actions_menu)

            menu.show()
        }

        actionsRes.setOnClickListener {
            val menu = PopupMenu(this, it)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.download -> {
                        downloadAudio(
                            "Result", controller.resultPath,
                            "Результат сохранен в загрузки", "результат"
                        )
                        true
                    }
                    R.id.add -> {
                        addAudioToLibrary(
                            "result", "Результат добавлен в библиотеку",
                            "результат"
                        )
                        true
                    }
                    R.id.share -> {
                        shareAudio(controller.resultPath, "result", "результат")
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
            menu.inflate(R.menu.actions_menu)
            menu.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun downloadAudio(source: String, path: String, message: String, messageSource: String) {
        try {
            val now = Date.from(Instant.now())
            val formatterDate = SimpleDateFormat("dd.MM.yyyy")
            val formatterTime = SimpleDateFormat("HH:mm")
            val filename = "VoiceVersa_" + source + "_" +
                    formatterDate.format(now) + "_" + formatterTime.format(now) + ".mp3"
            controller.downloadAudio(path, filename)
            Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            println("Exception while saving $source")
            e.printStackTrace()
            Toast.makeText(
                this,
                "Не удалось сохранить $messageSource в загрузки, попробуйте в другой раз!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun addAudioToLibrary(source: String, message: String, messageSource: String) {
        try {
            val sdf = SimpleDateFormat("dd.M.yyyy_hh.mm")
            val currentDate = sdf.format(Date())
            File(controller.homePath + "/" + source + ".mp3").copyTo(
                File(controller.savedPath + "/" + source + currentDate + ".mp3"),
                overwrite = false
            )
            Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
            ).show()

            controller.addToLibrary(controller.homePath + "/" + source + ".mp3").observe(this) {
                if (it == null) {
                    Toast.makeText(
                        this,
                        "К сожалению, не получилось добавить аудио в библиотеку на сервере. Попробуйте позже.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            println("Exception while adding $source")
            e.printStackTrace()
            Toast.makeText(
                this,
                "Не удалось добавить $messageSource в библиотеку, попробуйте в другой раз!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun shareAudio(path: String, source: String, messageSource: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.type = "audio/mp3"
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        } catch (e: Exception) {
            println("Exception while sharing $source")
            e.printStackTrace()
            Toast.makeText(
                this,
                "Не удалось отправить $messageSource, попробуйте в другой раз!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onClick(v: View?) {
        val intent = Intent()
        intent.type = "audio/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Выбор аудиофайла"), 1)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                getAudioFromStorage(data, controller.homePath + "/recording.mp3")
            } else {
                Toast.makeText(this, "Пожалуйста, выберите аудиозапись!", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAudioFromStorage(data: Intent?, destinationPath: String) {
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
            Toast.makeText(this, "Вы выбрали аудио $subSrc", Toast.LENGTH_SHORT).show()
            getPlayableRecording()
        } catch (e: Exception) {
            println("Error in getting file ")
            e.printStackTrace()
            Toast.makeText(
                this,
                "Не вышло открыть аудиозапись! Попробуйте открыть другой файл.",
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

    private fun mediaRecorderInit() {
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(controller.recordingPath)
        mediaRecorder?.prepare()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRecording() {
        if (state) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
            Toast.makeText(this, "Аудиозапись закончена", Toast.LENGTH_SHORT).show()
            chosenRec = controller.recordingPath
            pauseRecBtn.setBackgroundResource(R.drawable.stop)
            recordingStopped = false
            startRecBtn.setBackgroundResource(R.drawable.mic)
            getPlayableRecording()
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

    @SuppressLint("HandlerLeak")
    var handlerRes = object : Handler() {
        override fun handleMessage(msg: Message) {
            val currentPosition = msg.what

            positionResBar.progress = currentPosition

            val elapsedTime = createTimeLabel(currentPosition)
            elapsedTimeLabelRes.text = elapsedTime
            if (elapsedTime == createTimeLabel(totalTimeRes)) {
                playResBtn.setBackgroundResource(R.drawable.play)
            }
        }
    }

    @SuppressLint("HandlerLeak")
    var handlerVoice = object : Handler() {
        override fun handleMessage(msg: Message) {
            val currentPosition = msg.what
            val elapsedTime = createTimeLabel(currentPosition)

            if (elapsedTime == createTimeLabel(totalTimeVoice)) {
                playVoiceBtn.setBackgroundResource(R.drawable.play)
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
        val recURL = Uri.parse(controller.recordingPath)
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
        actionsRec.isEnabled = true
        processBtn.isEnabled = controller.online
        if (user.autoSaveRec) {
            downloadAudio(
                "Recording", controller.recordingPath,
                "Запись сохранена в загрузки", "запись"
            )
        }
    }


    fun playResBtnClick(view: View) {
        if (resPlayer?.isPlaying == true) {
            resPlayer!!.pause()
            playResBtn.setBackgroundResource(R.drawable.play)

        } else {
            resPlayer?.start()
            playResBtn.setBackgroundResource(R.drawable.stop)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPlayableResult() {
        val resURL = Uri.parse(controller.resultPath)
        Log.d("PROCESS_RESULT", "URI: ${resURL.path}")
        resPlayer = MediaPlayer.create(this, resURL)
        Log.d("PROCESS_RESULT", "PLAYER: $resPlayer")
        resPlayer?.isLooping = false
        resPlayer?.setVolume(0.5f, 0.5f)
        totalTimeRes = resPlayer?.duration!!
        totalTimeLabelRes.text = createTimeLabel(totalTimeRes)

        positionResBar.max = totalTimeRes
        positionResBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        resPlayer?.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            }
        )

        Thread {
            while (resPlayer != null) {
                try {
                    val msg = Message()
                    msg.what = resPlayer?.currentPosition!!
                    handlerRes.sendMessage(msg)
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                }
            }
        }.start()

        playResBtn.isEnabled = true
        actionsRes.isEnabled = true
        if (user.autoSaveRes) {
            downloadAudio(
                "Result", controller.resultPath,
                "Результат сохранен в загрузки", "результат"
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun process(view: View) {
        var voiceId = -1
        for (one_voice in user.voices) {
            if (one_voice.title == voice)
                voiceId = one_voice.ID
        }
        controller.process(voiceId).observe(this) { resultFromServer ->
            if (resultFromServer != null) {
                val url = resultFromServer.url
                Toast.makeText(this, "Ваша аудиозапись обрабатывается", Toast.LENGTH_SHORT).show()
                controller.downloadAudioByURL(url, controller.resultPath).observe(this) {
                    if (it) {
                        processed = true
                        getPlayableResult()
                    } else {
                        Toast.makeText(
                            this,
                            "Не получилось загрузить результат с сервера! Попробуйте в другой раз",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {

                Toast.makeText(
                    this,
                    "Не получилось обработать аудио! Попробуйте в другой раз",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun playVoiceBtnClick(view: View) {
        if (voicePlayer?.isPlaying == true) {
            voicePlayer!!.pause()
            playVoiceBtn.setBackgroundResource(R.drawable.play)

        } else {
            voicePlayer?.start()
            playVoiceBtn.setBackgroundResource(R.drawable.stop)
        }
    }

    private fun getPlayableVoice() {
        voice = voiceSpinner.selectedItem as String
        val voicePath = controller.voicesPath + "/" + voice + ".mp3"
        val resURL = Uri.parse(voicePath)
        voicePlayer = MediaPlayer.create(this, resURL)
        voicePlayer?.isLooping = false
        voicePlayer?.setVolume(0.5f, 0.5f)
        totalTimeVoice = voicePlayer?.duration!!

        Thread {
            while (voicePlayer != null) {
                try {
                    val msg = Message()
                    msg.what = voicePlayer?.currentPosition!!
                    handlerVoice.sendMessage(msg)
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                }
            }
        }.start()
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        voicePlayer?.stop()
        playVoiceBtn.setBackgroundResource(R.drawable.play)
        getPlayableVoice()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}
}