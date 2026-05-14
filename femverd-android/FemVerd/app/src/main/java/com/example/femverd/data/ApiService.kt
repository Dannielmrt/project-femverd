package com.example.femverd.data

import com.example.femverd.model.HistoryItem
import com.example.femverd.model.LoginResponse
import com.example.femverd.model.UserMe
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") dni: String,
        @Field("password") pass: String
    ): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<UserMe>

    @GET("auth/me/history")
    suspend fun getHistory(@Header("Authorization") token: String): Response<List<HistoryItem>>
}