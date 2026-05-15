package com.example.femverd.ui.screens.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.femverd.data.RetrofitClient
import com.example.femverd.model.RedeemRequest
import com.example.femverd.model.RedemptionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RewardsViewModel : ViewModel() {

    private val _myRewards = MutableStateFlow<List<RedemptionItem>>(emptyList())
    val myRewards: StateFlow<List<RedemptionItem>> = _myRewards

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Static catalog for the MVP
    val catalog = listOf(
        Pair("Local Bus Ticket", 100.0),
        Pair("Cinema 2x1 Voucher", 250.0),
        Pair("Eco-Tote Bag", 500.0),
        Pair("Electric Scooter 15min", 750.0),
        Pair("Organic Market -5€", 1000.0)
    )

    fun fetchMyRewards(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.instance.getMyRewards("Bearer $token")
                if (response.isSuccessful) _myRewards.value = response.body() ?: emptyList()
            } catch (e: Exception) { /* Log error */ }
            finally { _isLoading.value = false }
        }
    }

    fun redeem(token: String, name: String, cost: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.instance.redeemReward(
                    "Bearer $token", RedeemRequest(name, cost)
                )
                if (response.isSuccessful) {
                    fetchMyRewards(token) // Refresh wallet
                    onSuccess()
                }
            } catch (e: Exception) { /* Log error */ }
            finally { _isLoading.value = false }
        }
    }
}