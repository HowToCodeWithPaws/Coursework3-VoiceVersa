package com.example.voiceversa.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.util.*

class Audio @RequiresApi(Build.VERSION_CODES.O) constructor( var ID : Int = -1, title_: String = "новое аудио", var source: String = "recording", var url: String = "",
                                                            duration_: Int = 0, date_: Date = Date.from(Instant.now()) ) {

    var title: String  = "новое аудио"
        set(value) {
            if (value.length in 1..19) {
                field = value
            }
        }

    var duration: Int = 0
        set(value) {
            if (value >= 0) field = value
        }


    @RequiresApi(Build.VERSION_CODES.O)
    var date: Date = Date.from(Instant.now())

    init {
        title = title_
        duration = duration_
        date = date_
    }
}