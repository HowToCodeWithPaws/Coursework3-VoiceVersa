package com.example.voiceversa.Controller

import com.example.voiceversa.controller
import java.io.File

 fun readAudioNames(path: String): ArrayList<String> {
    var audioNames: ArrayList<String> = ArrayList()
    File(path).walkTopDown().forEach {
        if(it.extension == "mp3"){
            var idxDot = it.absolutePath.indexOfLast { it == '.' }
            var ext = it.absolutePath.substring(idxDot + 1, it.absolutePath.length)
            var withoutExt =  it.absolutePath.substring(0, idxDot)
            var name = withoutExt.substring(withoutExt.indexOfLast { it == '/' } + 1, withoutExt.length)

            audioNames.add(name)
        }
    }
    return audioNames
}


fun makeDirectories(path: String, name: String):String{

    val mediaStorageDir = File(path, name)

    if (!mediaStorageDir.exists()) {
        if (!mediaStorageDir.mkdirs()) {
            return ""
        }
    }

    return mediaStorageDir.absolutePath

}

class Controller(homePath_ : String = "empty") {

    var homePath : String = "empty"
        get() {
            return field
        }
        set(value) {
            field = value
        }

    var recordingPath: String = "empty"
        get() {
            return field
        }
        set(value) {
                field = value
        }

    var resultPath: String = "empty"
        get() {
            return field
        }
        set(value) {
            field = value
        }

    var voicesPath: String = "empty"
        get() {
            return field
        }
        set(value) {
            field = value
        }

    var savedPath: String = "empty"
        get() {
            return field
        }
        set(value) {
            field = value
        }

    init {
        homePath = homePath_
        recordingPath = homePath + "/recording.mp3"
        resultPath = homePath + "/result.mp3"
        voicesPath = makeDirectories(homePath,  "voices")
        savedPath = makeDirectories(homePath, "saved")
    }

    fun signIn(login: String, password: String):Boolean{
        // TODO: server request to authorize
        return true
    }

    fun signUp(login: String, password: String):Boolean{
        // TODO: server request to subscribe
        return true
    }

    fun process(voice: String){
        //TODO: server process audio than save new one
        // input: chosen voice + recordingPath
        File(controller.homePath+"/recording.mp3").copyTo(File(controller.homePath + "/result.mp3"), overwrite = true)
    }
}