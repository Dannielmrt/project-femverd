package com.example.femverd.model

data class LoginResponse(val access_token: String, val token_type: String)
data class UserMe(
    val dni: String,
    val name: String,
    val email: String,
    val current_points: Double,
    val total_points: Double
)

data class UserUpdateRequest(
    val user_name: String,
    val email: String
)

data class HistoryItem(
    val id: Int,
    val date: String,
    val quantity: Double,
    val generated_points: Double,
    val material_id: Int,
    val green_point_id: Int
)

// Model for the rewards already redeemed
data class RedemptionItem(
    val id: Int,
    val reward_name: String,
    val cost: Double,
    val code: String,
    val date: String
)

// Request for redeeming
data class RedeemRequest(
    val reward_name: String,
    val cost: Double
)