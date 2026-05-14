package com.example.femverd.model

data class LoginResponse(val access_token: String, val token_type: String)
data class UserMe(val dni: String, val name: String, val current_points: Double)
data class HistoryItem(
    val id: Int,
    val date: String,
    val quantity: Double,
    val generated_points: Double,
    val material_id: Int,
    val green_point_id: Int
)