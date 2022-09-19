package com.example.voiceversa.serverClasses

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
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
    fun process(@Part("voice") voice: Int, @Part audio: MultipartBody.Part,
                @Header("Authorization") token: String): Call<ResultFromServer>

    @POST("/login/")
    fun authorize(@Body loginRequest: LoginRequest): Call<Token>

    @POST("/register/")
    fun signUp(@Body loginRequest: LoginRequest): Call<Any>

    @GET("/audio/")
    fun loadLibrary(@Header("Authorization") token: String): Call<AudioListResponse<AudioFromServer>>

    @GET("/voices/")
    fun loadVoices(@Header("Authorization") token: String): Call<AudioListResponse<VoiceFromServer>>

    @GET
    fun downloadFileWithDynamicUrlSync(@Url fileUrl: String?, @Header("Authorization") token: String): Call<ResponseBody>
}
