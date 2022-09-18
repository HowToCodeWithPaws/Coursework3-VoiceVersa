package com.example.voiceversa.Controller

import android.content.ContentResolver
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.voiceversa.BuildConfig
import com.example.voiceversa.Model.*
import com.example.voiceversa.controller
import com.example.voiceversa.user
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*


fun readAudioNames(path: String): ArrayList<String> {
    var audioNames: ArrayList<String> = ArrayList()
    File(path).walkTopDown().forEach {
        if (it.extension == "mp3") {
            var idxDot = it.absolutePath.indexOfLast { it == '.' }
            var ext = it.absolutePath.substring(idxDot + 1, it.absolutePath.length)
            var withoutExt = it.absolutePath.substring(0, idxDot)
            var name =
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
        private const val BASE_URL = "http://192.168.1.150:8080" //TODO change to ours
    }

    private var service: AudioApiService? = null

    private val token: MutableLiveData<String> by lazy {
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

    private val requestResult : MutableLiveData<Any> by lazy {
        MutableLiveData<Any>()
    }

    private val deleteResult : MutableLiveData<Any> by lazy {
        MutableLiveData<Any>()
    }

    private val saveResult : MutableLiveData<Any> by lazy {
        MutableLiveData<Any>()
    }

    private val downloadBody : MutableLiveData<Boolean> by lazy {
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
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            service = retrofit.create(AudioApiService::class.java)
            online = true
        } catch (e: Exception) {
            println("\n\nEXCEPTION WHILE CONNECTING TO SERVER\n\n" + e.printStackTrace())
        }
    }

    fun downloadAudioByURL(url: String, path: String):LiveData<Boolean>{
        val apiInterface = service!!.downloadFileWithDynamicUrlSync(token.toString(), url)

        serverDownloadAudioByURL(apiInterface, downloadBody, path)

        return downloadBody
    }

    fun serverDownloadAudioByURL(apiInterface: Call<ResponseBody>,
                                 downloadBody: MutableLiveData<Boolean>, path: String){

        apiInterface.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                downloadBody.postValue( writeResponseBodyToDisk(response.body()!!, path))
                Log.d("LIB", "SUCCESS: ${response.body()}")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
               downloadBody.postValue(false)
                Log.d("LIB", "ERROR: ${t.message}")
            }
        })
    }

    private fun writeResponseBodyToDisk(body: ResponseBody, path:String): Boolean {
        return try {
            val file: File = File(path)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
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
                if (inputStream != null) {
                    inputStream.close()
                }
                if (outputStream != null) {
                    outputStream.close()
                }
            }
        } catch (e: IOException) {
            false
        }
    }

    fun loadLibrary():LiveData<AudioListResponse<AudioFromServer>>{
        val apiInterface = service!!.loadLibrary(token.toString())

        serverLoadLibrary(apiInterface, library)

        return library
    }

    fun serverLoadLibrary(apiInterface: Call<AudioListResponse<AudioFromServer>>,
                         library: MutableLiveData<AudioListResponse<AudioFromServer>>){
        apiInterface.enqueue(object : Callback<AudioListResponse<AudioFromServer>> {
            override fun onResponse(call: Call<AudioListResponse<AudioFromServer>>, response: Response<AudioListResponse<AudioFromServer>>) {
                library.postValue(response.body())
                Log.d("LIB", "SUCCESS: ${response.body()}")
            }

            override fun onFailure(call: Call<AudioListResponse<AudioFromServer>>, t: Throwable) {
                library.postValue(null)
                Log.d("LIB", "ERROR: ${t.message}")
            }
        })
    }

    fun loadVoices():LiveData<AudioListResponse<VoiceFromServer>>{
        val apiInterface = service!!.loadVoices(token.toString())

        serverLoadVoices(apiInterface, voices)

        return voices
    }

    fun serverLoadVoices(apiInterface: Call<AudioListResponse<VoiceFromServer>>,
                         voices: MutableLiveData<AudioListResponse<VoiceFromServer>>){
        apiInterface.enqueue(object : Callback<AudioListResponse<VoiceFromServer>> {
            override fun onResponse(call: Call<AudioListResponse<VoiceFromServer>>,
                                    response: Response<AudioListResponse<VoiceFromServer>>) {
                voices.postValue(response.body())
            }

            override fun onFailure(call: Call<AudioListResponse<VoiceFromServer>>, t: Throwable) {
                voices.postValue(null)
            }
        })
    }

    fun deleteAudio(id: Int): LiveData<Any> {
        val apiInterface = service!!.delete(id, token.toString())

        serverDeleteAudio(apiInterface, deleteResult)

        return deleteResult
    }

    private fun serverDeleteAudio(apiInterface: Call<Any>, deleteResult: MutableLiveData<Any>) {
        apiInterface.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                deleteResult.postValue(response.body())
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                deleteResult.postValue(null)
            }
        })
    }

    fun signInOrUp(username: String, password: String, key: String): LiveData<String> {
        Log.d("TOKEN", "Start")
        if (key == "signin") {
            val apiInterface = service!!.authorize(LoginRequest(username, password))
            serverSignIn(apiInterface, token)
        } else if (key == "signup") {
            val request = LoginRequest(username, password)
            val apiInterface = service!!.signUp(request)
            serverSignUp(apiInterface, token, request)
        }

        return token
    }

    private fun serverSignIn(apiInterface: Call<Token>, token: MutableLiveData<String>) {
        apiInterface.enqueue(object : Callback<Token> {
            override fun onResponse(call: Call<Token>, response: Response<Token>) {
                token.postValue(response.body()?.token)
            }

            override fun onFailure(call: Call<Token>, t: Throwable) {
                token.postValue(null)
                Log.d("TOKEN", "FAILED: ${t.message}")
            }
        })
    }

    private fun serverSignUp(apiInterface: Call<Any>, token: MutableLiveData<String>, request: LoginRequest) {
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
        println("\n\n\n" + file.absolutePath + "\n\n\n")

        val requestFile = file
            .asRequestBody("audio/*".toMediaTypeOrNull()//or "audio/mp3" idk
            )

        val body = MultipartBody.Part.createFormData("recording", file.name, requestFile)
        val voice = "${230}".toRequestBody("text/plain".toMediaTypeOrNull())
        val apiInterface = service!!.process(voice, body, token.toString())//replace 230 with voice code

        serverProcess(apiInterface, result)

     //   File(this.homePath + "/recording.mp3").copyTo(// delete this, on finish save result to result
       //     File(this.homePath + "/result.mp3"),
         //   overwrite = true
        //)

        return result
    }

    private fun serverProcess(apiInterface: Call<ResultFromServer>, result: MutableLiveData<ResultFromServer>) {
        apiInterface.enqueue(object : Callback<ResultFromServer> {
            override fun onResponse(call: Call<ResultFromServer>, response: Response<ResultFromServer>) {
                result.postValue(response.body())
            }

            override fun onFailure(call: Call<ResultFromServer>, t: Throwable) {
                result.postValue(null)
            }
        })
    }

    fun addToLibrary(audioPath: String): LiveData<Any> {

        var file: File = File(audioPath)
        println("\n\n\n" + file.absolutePath + "\n\n\n")

        val requestFile = file.asRequestBody("audio/*".toMediaTypeOrNull())

        val body = MultipartBody.Part.createFormData("library", file.name, requestFile)
        val apiInterface = service!!.save(body, token.toString())
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

        var file: File =
            if (requestName.startsWith("archive")) {
                File(controller.requestArchivePath)

            } else if (requestName.startsWith("audio")) {
                File(controller.requestRecordingPath)
            } else {
                File("")
            }

        println("\n\n\n" + file.absolutePath + "\n\n\n")

        val requestFile = file
            .asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val body =
            MultipartBody.Part.createFormData("request" + requestName, file.name, requestFile)
        val apiInterface = service!!.request(body, token.toString())
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
            println("\n\n" + user.audios.size)
            var copy = user.audios.toArray()
            for (i in 0 until user.audios.size) {
                result = result && deleteAudio(copy[i] as Audio)
                println("\n\n" + user.audios.size + " " + i)
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
            return true
        } catch (e: Exception) {
            println("Error while deleting audio")
            e.printStackTrace()
            return false
        }
    }
}