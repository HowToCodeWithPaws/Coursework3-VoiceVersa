package com.example.voiceversa.model


import java.io.Serializable
import java.util.ArrayList

class User(var name: String = "", var audios: ArrayList<Audio> = ArrayList(), var voices: ArrayList<Audio> = ArrayList()) : Serializable {
    var autoSaveRec : Boolean = false

    var autoSaveRes : Boolean = false

    override fun toString():String{
        return "name "+ name+"\n\naudios" + audios.joinToString { el->"\n"+el.title }
    }
}