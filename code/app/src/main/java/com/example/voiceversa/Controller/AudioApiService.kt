package com.example.voiceversa.Controller

import com.example.voiceversa.Model.AudioFromServer
import com.example.voiceversa.Model.Token
import com.example.voiceversa.Model.VoiceFromServer
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface AudioApiService {

    @Multipart
    @POST("/audio/")
    fun save(@Part("audio") audio: MultipartBody.Part, @Header("Authorization") token: String): Call<Any>

    @POST("/delete/")
    fun delete(@Part("name") name: String, @Header("Authorization") token: String): Call<Any>

    @Multipart
    @POST("/audio/")
    fun request(@Part("sample") sample: MultipartBody.Part, @Header("Authorization") token: String): Call<Any>

    @Multipart
    @POST("/process/")
    fun process(@Part("voice") voice: Int, @Part("audio") audio: MultipartBody.Part, @Header("Authorization") token: String): Call<String>//TODO понять

    @POST("/api-token-auth/")
    fun authorize(@Part("username") username: String, @Part("password") password: String): Call<Token>

    @POST("//")//TODO
    fun signUp(@Part("username") login: String, @Part("password") password: String): Call<Token>

    @GET("/audio/")
    fun loadLibrary(@Header("Authorization") token: String): Call<List<AudioFromServer>>

    @GET("/voices/")
    fun loadVoices(@Header("Authorization") token: String): Call<List<VoiceFromServer>>
}
