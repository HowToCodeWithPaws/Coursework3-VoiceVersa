package com.example.voiceversa.controller

import android.content.ContentResolver
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.voiceversa.BuildConfig
import com.example.voiceversa.model.*
import com.example.voiceversa.serverClasses.*
import com.example.voiceversa.view.controller
import com.example.voiceversa.view.user
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.util.concurrent.TimeUnit


fun readAudioNames(path: String): ArrayList<String> {
    val audioNames: ArrayList<String> = ArrayList()
    File(path).walkTopDown().forEach { file ->
        if (file.extension == "mp3") {
            val idxDot = file.absolutePath.indexOfLast { it == '.' }
            val withoutExt = file.absolutePath.substring(0, idxDot)
            val name =
                withoutExt.substring(withoutExt.indexOfLast { it == '/' } + 1, withoutExt.length)

            audioNames.add(name)
        }
    }
    return audioNames
}


fun makeDirectories(path: String, name: String): String {

    val mediaStorageDir = File(path, name)

    if (!mediaStorageDir.exists()) {
        if (!mediaStorageDir.mkdirs()) {
            return ""
        }
    }

    return mediaStorageDir.absolutePath
}

class Controller(homePath_: String = "empty") : ViewModel() {

    var context: Context? = null
    var homePath: String = "empty"
    var requestRecordingPath: String = "empty"
    var requestArchivePath: String = "empty"
    var recordingPath: String = "empty"
    var resultPath: String = "empty"
    var voicesPath: String = "empty"
    var savedPath: String = "empty"
    var online: Boolean = false

    companion object {
        private const val BASE_URL = "http://192.168.43.217:8080" //TODO change to ours
    }

    private var service: AudioApiService? = null

    val token: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val result: MutableLiveData<ResultFromServer> by lazy {
        MutableLiveData<ResultFromServer>()
    }

    private val voices: MutableLiveData<AudioListResponse<VoiceFromServer>> by lazy {
        MutableLiveData<AudioListResponse<VoiceFromServer>>()
    }

    private val library: MutableLiveData<AudioListResponse<AudioFromServer>> by lazy {
        MutableLiveData<AudioListResponse<AudioFromServer>>()
    }

    private val requestResult: MutableLiveData<Any> by lazy {
        MutableLiveData<Any>()
    }

    private val deleteResult: MutableLiveData<Any> by lazy {
        MutableLiveData<Any>()
    }

    private val saveResult: MutableLiveData<Any> by lazy {
        MutableLiveData<Any>()
    }

    private val downloadBody: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    init {
        homePath = homePath_
        requestRecordingPath = "$homePath/request.mp3"
        requestArchivePath = "$homePath/request.zip"
        recordingPath = "$homePath/recording.mp3"
        resultPath = "$homePath/result.mp3"
        voicesPath = makeDirectories(homePath, "voices")
        savedPath = makeDirectories(homePath, "saved")

        try {
            val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.MINUTES)
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build()
            service = retrofit.create(AudioApiService::class.java)
            online = true
        } catch (e: Exception) {
            println("\n\nEXCEPTION WHILE CONNECTING TO SERVER\n\n" + e.printStackTrace())
        }
    }

    fun downloadAudioByURL(url: String, path: String): LiveData<Boolean> {
        val apiInterface = service!!.downloadFileWithDynamicUrlSync(url, token.value!!)
        serverDownloadAudioByURL(apiInterface, downloadBody, path)
        return downloadBody
    }

    private fun serverDownloadAudioByURL(
        apiInterface: Call<ResponseBody>,
        downloadBody: MutableLiveData<Boolean>, path: String
    ) {

        apiInterface.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.body() != null)
                    downloadBody.postValue(writeResponseBodyToDisk(response.body()!!, path))
                Log.d("LIB", "SUCCESS: ${response.body().toString()}")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                downloadBody.postValue(false)
                Log.d("LIB", "ERROR: ${t.message}")
            }
        })
    }

    private fun writeResponseBodyToDisk(body: ResponseBody, path: String): Boolean {
        return try {
            println("\n\n\n\nENTER DOWNLOAD\n\n\n\n")
            val file = File(path)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            println("\n\n\n\nSTREAMS MADE\n\n\n\n")
            try {
                val fileReader = ByteArray(4096)
                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0

                println("\n\n\n\nCHECKPOINT 1\n\n\n\n")
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)

                while (true) {
                    println("\n\n\n\nENTER WHILE\n\n\n\n")
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {

                        println("\n\n\n\nEXIT DOWNLOAD\n\n\n\n")
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                    println("file download: $fileSizeDownloaded of $fileSize")
                }
                outputStream.flush()
                true
            } catch (e: IOException) {
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            false
        }
    }

    fun loadLibrary(): LiveData<AudioListResponse<AudioFromServer>> {
        return try {
            val apiInterface = service!!.loadLibrary(token.value!!)
            serverLoadLibrary(apiInterface, library)
            library
        } catch (e: NullPointerException) {
            controller.online = false
            library
        }
    }

    private fun serverLoadLibrary(
        apiInterface: Call<AudioListResponse<AudioFromServer>>,
        library: MutableLiveData<AudioListResponse<AudioFromServer>>
    ) {
        apiInterface.enqueue(object : Callback<AudioListResponse<AudioFromServer>> {
            override fun onResponse(
                call: Call<AudioListResponse<AudioFromServer>>,
                response: Response<AudioListResponse<AudioFromServer>>
            ) {
                library.postValue(response.body())
                Log.d("LIB", "SUCCESS: ${response.body()}")
            }

            override fun onFailure(call: Call<AudioListResponse<AudioFromServer>>, t: Throwable) {
                library.postValue(null)
                Log.d("LIB", "ERROR: ${t.message}")
            }
        })
    }

    fun loadVoices(): LiveData<AudioListResponse<VoiceFromServer>> {
        return try {
            Log.d("VOICES", "Token - ${token.value}")
            val apiInterface = service!!.loadVoices(token.value!!)

            serverLoadVoices(apiInterface, voices)

            voices
        } catch (e: NullPointerException) {
            controller.online = false
            voices
        }
    }

    private fun serverLoadVoices(
        apiInterface: Call<AudioListResponse<VoiceFromServer>>,
        voices: MutableLiveData<AudioListResponse<VoiceFromServer>>
    ) {
        apiInterface.enqueue(object : Callback<AudioListResponse<VoiceFromServer>> {
            override fun onResponse(
                call: Call<AudioListResponse<VoiceFromServer>>,
                response: Response<AudioListResponse<VoiceFromServer>>
            ) {
                if (response.body() != null)
                    voices.postValue(response.body())
            }

            override fun onFailure(call: Call<AudioListResponse<VoiceFromServer>>, t: Throwable) {
                voices.postValue(null)
            }
        })
    }

    private fun deleteAudio(id: Int): LiveData<Any> {
        val apiInterface = service!!.delete(id, token.value!!)

        serverDeleteAudio(apiInterface, deleteResult)

        return deleteResult
    }

    private fun serverDeleteAudio(apiInterface: Call<Any>, deleteResult: MutableLiveData<Any>) {
        apiInterface.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                deleteResult.postValue(response.body())
                println("LOOK HERE DELETE RESULT "+ response.body())
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                deleteResult.postValue(null)
            }
        })
    }

    fun signInOrUp(username: String, password: String, key: String): LiveData<String> {
        Log.d("TOKEN", "Start")
        if (key == "signIn") {
            val apiInterface = service!!.authorize(LoginRequest(username, password))
            println("\n\n\nSIGN IN\n\n\n")
            serverSignIn(apiInterface, token)
        } else if (key == "signUp") {
            val request = LoginRequest(username, password)
            val apiInterface = service!!.signUp(request)
            println("\n\n\nSIGN UP\n\n\n")
            serverSignUp(apiInterface, token, request)
        }
        return token
    }

    private fun serverSignIn(apiInterface: Call<Token>, token: MutableLiveData<String>) {
        apiInterface.enqueue(object : Callback<Token> {
            override fun onResponse(call: Call<Token>, response: Response<Token>) {
                Log.d("TOKEN", "SUCCESS: ${response.body()}")
                token.postValue("Token ${response.body()?.token}")
            }

            override fun onFailure(call: Call<Token>, t: Throwable) {
                token.postValue(null)
                Log.d("TOKEN", "FAILED: ${t.message}")
            }
        })
    }

    private fun serverSignUp(
        apiInterface: Call<Any>,
        token: MutableLiveData<String>,
        request: LoginRequest
    ) {
        apiInterface.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                val loginApiInterface = service!!.authorize(request)
                serverSignIn(loginApiInterface, token)
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                token.postValue(null)
                Log.d("TOKEN", "FAILED: ${t.message}")
            }
        })
    }

    fun process(voiceID: Int): LiveData<ResultFromServer> {
        val file = File(controller.recordingPath)

        val requestFile = file.asRequestBody(
            "audio/*".toMediaTypeOrNull()
        )

        val body = MultipartBody.Part.createFormData("url", file.name, requestFile)
        val voice = "$voiceID".toRequestBody("text/plain".toMediaTypeOrNull())
        Log.d("PROCESS", "Token - ${token.value}")
        val apiInterface = service!!.process(voiceID, body, token.value!!)// TODO?: instead of voice

        serverProcess(apiInterface, result)

        return result
    }

    private fun serverProcess(
        apiInterface: Call<ResultFromServer>,
        result: MutableLiveData<ResultFromServer>
    ) {
        apiInterface.enqueue(object : Callback<ResultFromServer> {
            override fun onResponse(
                call: Call<ResultFromServer>,
                response: Response<ResultFromServer>
            ) {
                Log.d("PROCESS", "SUCCESS - ${response.body()?.url}")
                if (response.body() != null)
                    result.postValue(response.body())
            }

            override fun onFailure(call: Call<ResultFromServer>, t: Throwable) {
                Log.d("PROCESS", "FAIL - ${t.message}")
                result.postValue(null)
            }
        })
    }

    fun addToLibrary(audioPath: String): LiveData<Any> {
        val file = File(audioPath)
        val requestFile = file.asRequestBody("audio/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("audio", file.name, requestFile)
        val apiInterface = service!!.save(body, token.value!!)
        serverAddToLibrary(apiInterface, saveResult)

        return saveResult
    }

    private fun serverAddToLibrary(apiInterface: Call<Any>, saveResult: MutableLiveData<Any>) {
        apiInterface.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                saveResult.postValue(response.body())
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                saveResult.postValue(null)
            }
        })
    }

    fun sendRequest(requestName: String): LiveData<Any> {

        val file: File =
            if (requestName.startsWith("archive")) {
                File(controller.requestArchivePath)

            } else if (requestName.startsWith("audio")) {
                File(controller.requestRecordingPath)
            } else {
                File("")
            }

        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val body =
            MultipartBody.Part.createFormData("archive", file.name, requestFile)
        val apiInterface = service!!.request(body, token.value!!)
        serverRequest(apiInterface, requestResult)

        return requestResult
    }

    private fun serverRequest(apiInterface: Call<Any>, requestResult: MutableLiveData<Any>) {
        apiInterface.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                requestResult.postValue(response.body())
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                requestResult.postValue(null)
            }
        })
    }


    fun downloadAudio(sourcePath: String, filename: String) {
        try {
            val source = File(sourcePath)
            val data = source.readBytes()
            val uri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(uri.absolutePath + "/" + filename)

            if (!file.exists()) {
                file.createNewFile()
            }
            val fos = FileOutputStream(file)
            fos.write(data)
            fos.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteLibrary(): Boolean {
        try {
            var result = true
            val copy = user.audios.toArray()
            for (i in 0 until user.audios.size) {
                result = result && deleteAudio(copy[i] as Audio)
            }
            user.audios = ArrayList()

            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun deleteAudio(audio: Audio): Boolean {
        try {
            user.audios.remove(audio)
            val file = File(audio.url)
            val uri = FileProvider.getUriForFile(
                this.context!!,
                BuildConfig.APPLICATION_ID + ".provider", file
            )

            val contentResolver: ContentResolver =
                this.context!!.contentResolver
            contentResolver.delete(uri, null, null)

            controller.deleteAudio(audio.ID)
            return true
        } catch (e: Exception) {
            println("Error while deleting audio")
            e.printStackTrace()
            return false
        }
    }
}