package com.example.voiceversa.serverClasses

import com.google.gson.annotations.SerializedName

data class Token (
    @SerializedName("token")
    val token: String
)