package com.example.voiceversa.Controller

import com.example.voiceversa.Model.AudioFromServer
import com.example.voiceversa.Model.LoginRequest
import com.example.voiceversa.Model.Token
import com.example.voiceversa.Model.VoiceFromServer
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface AudioApiService {

    @Multipart
    @POST("/audio/")
    fun save(@Part audio: MultipartBody.Part, @Header("Authorization") token: String): Call<Any>

    @DELETE("/audio/{id}")
    fun delete(@Path("id") id: Int, @Header("Authorization") token: String): Call<Any>

    @Multipart
    @POST("/request/")
    fun request(@Part sample: MultipartBody.Part, @Header("Authorization") token: String): Call<Any>

    @Multipart
    @POST("/process/")
    fun process(@Part("voice") voice: RequestBody, @Part audio: MultipartBody.Part,
                @Header("Authorization") token: String): Call<String>//TODO понять

    @POST("/login/")
    fun authorize(@Body loginRequest: LoginRequest): Call<Token>

    @POST("/register/")
    fun signUp(@Body loginRequest: LoginRequest): Call<Any>

    @GET("/audio/")
    fun loadLibrary(@Header("Authorization") token: String): Call<List<AudioFromServer>>

    @GET("/voices/")
    fun loadVoices(@Header("Authorization") token: String): Call<List<VoiceFromServer>>
}
