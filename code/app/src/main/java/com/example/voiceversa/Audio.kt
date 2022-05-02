package com.example.voiceversa

class Audio constructor(title_: String = "новое аудио", source_: String = "recording",
                        duration_: Int = 0) {

    var title: String = "новое аудио"
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


}