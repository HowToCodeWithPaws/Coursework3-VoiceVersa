package com.example.voiceversa


import java.io.Serializable
import java.util.ArrayList

class User(name_: String = "new user", token_: String = "token", audios_: ArrayList<Audio> = ArrayList()) : Serializable {
    var name: String = "new user"
        get() {
            return field
        }
        set(value) {
            if (value.length in 1..9) {
                field = value
            }
        }

    var token: String = "token"
        get() {
            return field
        }
        set(value) {
            field = value
        }

    var audios: ArrayList<Audio> = ArrayList()
        get() {
            return field
        }
        set(value) {
            field = value
        }


    override fun toString():String{
        return "name "+ name +"\n\naudios" + audios.joinToString { el->"\n"+el.title }
    }

    init {
        name = name_
        token = token_
        audios = audios_
    }
}