package com.example.voiceversa.serverClasses

data class AudioFromServer(val id: Int, val audio: AudioInfo, val created : String, val is_processed: Boolean)