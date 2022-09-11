package com.example.voiceversa.Model


import java.io.Serializable
import java.util.ArrayList

class User(name_: String = "", var token: String = "token", var audios: ArrayList<Audio> = ArrayList()) : Serializable {
    var name: String = ""
        get() {
            return field
        }
        set(value) {
            if (value.length in 1..9) {
                field = value
            }
        }

    var autosaveRec : Boolean = false

    var autosaveRes : Boolean = false

    override fun toString():String{
        return "name "+ name +"\n\naudios" + audios.joinToString { el->"\n"+el.title }
    }

    init {
        name = name_
    }
}