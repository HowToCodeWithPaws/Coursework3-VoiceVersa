package com.example.voiceversa.Controller

import com.example.voiceversa.Model.AudioFromServer
import com.example.voiceversa.Model.LoginRequest
import com.example.voiceversa.Model.Token
import com.example.voiceversa.Model.VoiceFromServer
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface AudioApiService {

    @Multipart
    @POST("/audio/")
    fun save(@Part("audio") audio: MultipartBody.Part, @Header("Authorization") token: String): Call<Any>

    @DELETE("/audio/")
    fun delete(@Part("name") name: String, @Header("Authorization") token: String): Call<Any>

    @Multipart
    @POST("/audio/")
    fun request(@Part("sample") sample: MultipartBody.Part, @Header("Authorization") token: String): Call<Any>

    @Multipart
    @POST("/process/")
    fun process(@Part("voice") voice: Int, @Part("audio") audio: MultipartBody.Part, @Header("Authorization") token: String): Call<String>//TODO понять

    @Headers("Content-Type: application/json")
    @POST("/api-token-auth/")
    fun authorize(@Body loginRequest: LoginRequest): Call<Token>

    @POST("/")//TODO
    fun signUp(@Query("username") login: String, @Query("password") password: String): Call<Token>

    @GET("/audio/")
    fun loadLibrary(@Header("Authorization") token: String): Call<List<AudioFromServer>>

    @GET("/voices/")
    fun loadVoices(@Header("Authorization") token: String): Call<List<VoiceFromServer>>
}
