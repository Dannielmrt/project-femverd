package com.example.femverd.data

import com.example.femverd.model.HistoryItem
import com.example.femverd.model.LoginResponse
import com.example.femverd.model.RedeemRequest
import com.example.femverd.model.RedemptionItem
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
    suspend fun getHistory(
        @Header("Authorization") token: String
    ): retrofit2.Response<List<com.example.femverd.model.HistoryItem>>

    @GET("auth/me/rewards")
    suspend fun getMyRewards(@Header("Authorization") token: String): Response<List<RedemptionItem>>

    @POST("auth/me/rewards/redeem")
    suspend fun redeemReward(
        @Header("Authorization") token: String,
        @Body request: RedeemRequest
    ): Response<Unit> // Only imports if 200 OK

    @PUT("auth/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: com.example.femverd.model.UserUpdateRequest
    ): retrofit2.Response<Unit>

    @DELETE("auth/me")
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<Unit>

    @GET("auth/me/certificate")
    suspend fun getAnnualCertificate(
        @Header("Authorization") token: String,
        @Query("year") year: Int
    ): retrofit2.Response<com.example.femverd.model.CertificateResponse>
}