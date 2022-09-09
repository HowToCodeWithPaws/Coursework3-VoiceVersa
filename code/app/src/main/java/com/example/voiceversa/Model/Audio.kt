package com.example.voiceversa.Model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.util.*

class Audio @RequiresApi(Build.VERSION_CODES.O) constructor(title_: String = "новое аудио", source_: String = "recording", url_: String = "",
                                                            duration_: Int = 0, date_: Date = Date.from(Instant.now()) ) {

    var title: String  = "новое аудио"
        get() {
            return field
        }
        set(value) {
            if (value.length in 1..19) {
                field = value
            }
        }

    var duration: Int = 0
        get() {
            return field
        }
        set(value) {
            if (value >= 0) field = value
        }

    var source: String = ""
        get() {
            return field
        }
        set(value) {
            field = value
        }

    var url: String = ""
        get() {
            return field
        }
        set(value) {
            field = value
        }

    @RequiresApi(Build.VERSION_CODES.O)
    var date: Date = Date.from(Instant.now())
        get() {
            return field
        }
        set(value) {
            field = value
        }

    init {
        title = title_
        duration = duration_
        source = source_
        date = date_
        url = url_
    }
}