package com.example.voiceversa.View.Settings

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.voiceversa.AccountActivity
import com.example.voiceversa.Controller.Controller
import com.example.voiceversa.Controller.makeDirectories
import com.example.voiceversa.Controller.readAudioNames
import com.example.voiceversa.R
import com.example.voiceversa.controller
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ProcessActivity : AppCompatActivity(), View.OnClickListener,
    AdapterView.OnItemSelectedListener {

    lateinit var actionsRec: ImageView
    lateinit var actionsRes: ImageView
    lateinit var startRecBtn: Button
    lateinit var attachRecBtn: Button
    lateinit var pauseRecBtn: Button
    lateinit var voiceSpinner: Spinner
    lateinit var processBtn: Button
    lateinit var playRecBtn: Button
    lateinit var playResBtn: Button
    lateinit var playVoiceBtn: Button
    lateinit var positionRecBar: SeekBar
    lateinit var positionResBar: SeekBar
    lateinit var elapsedTimeLabelRec: TextView
    lateinit var elapsedTimeLabelRes: TextView
    lateinit var totalTimeLabelRec: TextView
    lateinit var totalTimeLabelRes: TextView
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
        val topbar = findViewById<Toolbar>(R.id.top_bar)
        topbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.account) {
                val intent = Intent(this, AccountActivity::class.java)
                startActivity(intent)
            }
            true
        }

        voiceSpinner = findViewById<Spinner>(R.id.voicesSpinner)
        attachRecBtn = findViewById<Button>(R.id.attachRecBtn)
        processBtn = findViewById<Button>(R.id.processBtn)
        playRecBtn = findViewById<Button>(R.id.playRecBtn)
        playResBtn = findViewById<Button>(R.id.playResBtn)
        playVoiceBtn = findViewById<Button>(R.id.playVoiceBtn)
        positionRecBar = findViewById<SeekBar>(R.id.positionRecBar)
        positionResBar = findViewById<SeekBar>(R.id.positionResBar)
        elapsedTimeLabelRec = findViewById<TextView>(R.id.elapsedTimeLabelRec)
        elapsedTimeLabelRes = findViewById<TextView>(R.id.elapsedTimeLabelRes)
        totalTimeLabelRec = findViewById<TextView>(R.id.totalTimeLabelRec)
        totalTimeLabelRes = findViewById<TextView>(R.id.totalTimeLabelRes)
        actionsRec = findViewById(R.id.actionsRec)
        actionsRes = findViewById(R.id.actionsRes)

        playRecBtn.isEnabled = false
        playResBtn.isEnabled = false
        actionsRec.isEnabled = false
        actionsRes.isEnabled = false

        processBtn.isEnabled = false

        startRecBtn = findViewById(R.id.startRecBtn)
        pauseRecBtn = findViewById(R.id.pauseRecBtn)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(this, permissions, 0)
        }

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
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
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

        var adapterVoices =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                readAudioNames(controller.voicesPath)
            )
        adapterVoices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        voiceSpinner.adapter = adapterVoices
        voiceSpinner.onItemSelectedListener = this


        actionsRec.setOnClickListener {
            val menu = PopupMenu(this, it)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.download -> {
                        //TODO download
                        true
                    }
                    R.id.add -> {
                        //todo add
                        true
                    }
                    R.id.share -> {
                        //todo share
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
                        //TODO download
                        true
                    }
                    R.id.add -> {
                        //todo add
                        true
                    }
                    R.id.share -> {
                        //todo share
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


    override fun onClick(v: View?) {
        val intent = Intent()
        intent.type = "audio/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select audio"), 1)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                chosenRec = (data?.data!!).toString()
                Toast.makeText(this, "You chose the audio $chosenRec", Toast.LENGTH_SHORT).show()
            // TODO    File(chosenRec).copyTo(File(controller.homePath+"/recording.mp3"), overwrite = true)
                getPlayableRecording()
            } else {
                Toast.makeText(this, "You need to choose the audio!", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun mediarecorderInit() {
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(controller.recordingPath)
        mediaRecorder?.prepare()
    }

    private fun startRecording() {
        if (state) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
            Toast.makeText(this, "Recording stopped!", Toast.LENGTH_SHORT).show()
            chosenRec = controller.recordingPath
            startRecBtn.setBackgroundResource(R.drawable.mic)
            getPlayableRecording()
        } else {
            try {
                mediarecorderInit()
                mediaRecorder?.start()
                state = true
                playRecBtn.isEnabled = false
                Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
                startRecBtn.setBackgroundResource(R.drawable.finish)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }


    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun pauseRecording() {
        if (state) {
            if (!recordingStopped) {
                Toast.makeText(this, "Paused!", Toast.LENGTH_SHORT).show()
                mediaRecorder?.pause()
                recordingStopped = true
                pauseRecBtn.setBackgroundResource(R.drawable.play)
            } else {
                resumeRecording()
            }
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun resumeRecording() {
        Toast.makeText(this, "Resume!", Toast.LENGTH_SHORT).show()
        mediaRecorder?.resume()
        pauseRecBtn.setBackgroundResource(R.drawable.stop)
        recordingStopped = false
    }

    @SuppressLint("HandlerLeak")
    var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            var currentPosition = msg.what

            // Update positionBar
            positionRecBar.progress = currentPosition

            // Update Labels
            var elapsedTime = createTimeLabel(currentPosition)
            elapsedTimeLabelRec.text = elapsedTime
            if (elapsedTime == createTimeLabel(totalTime)) {
                playRecBtn.setBackgroundResource(R.drawable.play)
            }
        }
    }

    @SuppressLint("HandlerLeak")
    var handlerRes = object : Handler() {
        override fun handleMessage(msg: Message) {
            var currentPosition = msg.what

            // Update positionBar
            positionResBar.progress = currentPosition

            // Update Labels
            var elapsedTime = createTimeLabel(currentPosition)
            elapsedTimeLabelRes.text = elapsedTime
            if (elapsedTime == createTimeLabel(totalTimeRes)) {
                playResBtn.setBackgroundResource(R.drawable.play)
            }
        }
    }

    @SuppressLint("HandlerLeak")
    var handlerVoice = object : Handler() {
        override fun handleMessage(msg: Message) {
            var currentPosition = msg.what
            var elapsedTime = createTimeLabel(currentPosition)

            if (elapsedTime == createTimeLabel(totalTimeVoice)) {
                playVoiceBtn.setBackgroundResource(R.drawable.play)
            }
        }
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

    fun playRecBtnClick(v: View) {
        if (recPlayer?.isPlaying == true) {
            recPlayer!!.pause()
            playRecBtn.setBackgroundResource(R.drawable.play)

        } else {
            recPlayer?.start()
            playRecBtn.setBackgroundResource(R.drawable.stop)
        }
    }

    private fun getPlayableRecording() {
        var recURL = Uri.parse(chosenRec)
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

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            }
        )

        // Thread
        Thread(Runnable {
            while (recPlayer != null) {
                try {
                    var msg = Message()
                    msg.what = recPlayer?.currentPosition!!
                    handler.sendMessage(msg)
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                }
            }
        }).start()


        playRecBtn.isEnabled = true
        actionsRec.isEnabled = true
        processBtn.isEnabled = true
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

    private fun getPlayableResult() {
        var resURL = Uri.parse(controller.resultPath)
        resPlayer = MediaPlayer.create(this, resURL)
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

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            }
        )

        // Thread
        Thread(Runnable {
            while (resPlayer != null) {
                try {
                    var msg = Message()
                    msg.what = resPlayer?.currentPosition!!
                    handlerRes.sendMessage(msg)
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                }
            }
        }).start()

        playResBtn.isEnabled = true
        actionsRes.isEnabled = true
    }


    fun process(view: View) {
        controller.process(voice)
        processed = true
        Toast.makeText(this, "Your audio was processed!", Toast.LENGTH_SHORT).show()
        getPlayableResult()
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
        var voicePath = controller.voicesPath + "/" + voice + ".mp3"
        var resURL = Uri.parse(voicePath)
        voicePlayer = MediaPlayer.create(this, resURL)
        voicePlayer?.isLooping = false
        voicePlayer?.setVolume(0.5f, 0.5f)
        totalTimeVoice = voicePlayer?.duration!!

        Thread(Runnable {
            while (voicePlayer != null) {
                try {
                    var msg = Message()
                    msg.what = voicePlayer?.currentPosition!!
                    handlerVoice.sendMessage(msg)
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                }
            }
        }).start()
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        voicePlayer?.stop()
        playVoiceBtn.setBackgroundResource(R.drawable.play)
        getPlayableVoice()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

}