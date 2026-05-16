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
    val material_name: String,
    val location: String
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

data class MaterialBreakdown(
    val material: String,
    val total_quantity: Double,
    val unit: String
)

data class CertificateResponse(
    val certificate_year: Int,
    val citizen_name: String,
    val citizen_dni: String,
    val member_since: String?,
    val total_points_generated: Double,
    val materials_breakdown: List<MaterialBreakdown>
)

data class RegisterRequest(
    val dni: String,
    val user_name: String,
    val email: String,
    val password: String
)

data class GreenPointMarker(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double
)