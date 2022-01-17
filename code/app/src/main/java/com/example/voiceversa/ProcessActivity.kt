package com.example.voiceversa

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
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
import java.io.IOException
import java.util.*

class ProcessActivity : AppCompatActivity() {

    lateinit var startRecBtn: Button
    lateinit var pauseRecBtn: Button
    lateinit var stopRecBtn: Button
    lateinit var voiceSpinner: Spinner
    lateinit var actionsRecSpinner: Spinner
    lateinit var actionsResSpinner: Spinner
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
    private  var mp: MediaPlayer? = null
    private var totalTime: Int = 0

    private var flag = false
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false

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
         actionsRecSpinner = findViewById<Spinner>(R.id.actionsRecSpinner)
         actionsResSpinner = findViewById<Spinner>(R.id.actionsResSpinner)
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

    //    Toast.makeText(this,  Uri.parse(this.externalCacheDir!!.absolutePath + "/recording.mp3").toString(), Toast.LENGTH_SHORT).show()


//        mp = MediaPlayer.create(this, R.raw.w)
//        mp.isLooping = true
//        mp.setVolume(0.5f, 0.5f)
//        totalTime = mp.duration
//        totalTimeLabelRec.text = createTimeLabel(totalTime)

        startRecBtn = findViewById(R.id.startRecBtn)
        stopRecBtn = findViewById(R.id.stopRecBtn)
        pauseRecBtn = findViewById(R.id.pauseRecBtn)

        output = this.externalCacheDir!!.absolutePath + "/recording.mp3"


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions,0)
        }

//        mediaRecorder =  MediaRecorder()
//        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
//        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//        mediaRecorder?.setOutputFile(output)

        startRecBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this, permissions,0)
            } else {
                startRecording()
            }
        }

        stopRecBtn.setOnClickListener{
            stopRecording()
        }

        pauseRecBtn.setOnClickListener {
            pauseRecording()
        }
    }

    private fun mediarecorderInit(){
        mediaRecorder =  MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)
        mediaRecorder?.prepare()
    }

    private fun startRecording() {
        if(state){
            Toast.makeText(this, "You are already recording!", Toast.LENGTH_SHORT).show()
        }
        try {
            mediarecorderInit()
            mediaRecorder?.start()
            state = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getPlayableRecording() {
        var recURL = Uri.parse(output)
        mp = MediaPlayer.create(this, recURL)
        mp?.isLooping = false
        mp?.setVolume(0.5f, 0.5f)
        totalTime = mp?.duration!!
        totalTimeLabelRec.text = createTimeLabel(totalTime)

        positionRecBar.max = totalTime
        positionRecBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mp?.seekTo(progress)
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
            while (mp != null) {
                try {
                    var msg = Message()
                    msg.what = mp?.currentPosition!!
                    handler.sendMessage(msg)
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                }
            }
        }).start()


        playRecBtn.isEnabled = true
    }

    private fun stopRecording(){
     //   Toast.makeText(this,  this.externalCacheDir!!.absolutePath + "/recording.mp3", Toast.LENGTH_LONG).show()

        if(state){
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
            Toast.makeText(this, "Recording stopped!", Toast.LENGTH_SHORT).show()
            getPlayableRecording()
        }else{
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun pauseRecording() {
        if(state) {
            if(!recordingStopped){
                Toast.makeText(this,"Paused!", Toast.LENGTH_SHORT).show()
                mediaRecorder?.pause()
                recordingStopped = true
                pauseRecBtn.setBackgroundResource(R.drawable.play)
            }else{
                resumeRecording()
            }
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun resumeRecording() {
        Toast.makeText(this,"Resume!", Toast.LENGTH_SHORT).show()
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
  //      Toast.makeText(this,  Uri.parse(this.externalCacheDir!!.absolutePath + "/recording.mp3").toString(), Toast.LENGTH_LONG).show()

        if (mp?.isPlaying == true) {
            // Stop
            mp!!.pause()
            playRecBtn.setBackgroundResource(R.drawable.play)

        } else {
            // Start
            mp?.start()
            playRecBtn.setBackgroundResource(R.drawable.stop)
        }
    }

    fun playVoiceBtnClick(view: View) {}
    fun playResBtnClick(view: View) {}
    fun process(view: View) {}
}