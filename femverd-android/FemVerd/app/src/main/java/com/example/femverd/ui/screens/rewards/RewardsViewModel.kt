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

    // State flow for the user's redeemed codes
    private val _myRewards = MutableStateFlow<List<RedemptionItem>>(emptyList())
    val myRewards: StateFlow<List<RedemptionItem>> = _myRewards

    // State flow for loading indicators
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // State flow to manage Snackbar messages (Success / Error)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    // Static catalog for the MVP
    val catalog = listOf(
        Pair("Local Bus Ticket", 100.0),
        Pair("Cinema 2x1 Voucher", 250.0),
        Pair("Eco-Tote Bag", 500.0),
        Pair("Electric Scooter 15min", 750.0),
        Pair("Organic Market -5€", 1000.0)
    )


    fun fetchMyRewards(token: String) {
        /*
          Fetches the user's previously redeemed rewards from the server.
         */
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.instance.getMyRewards("Bearer $token")
                if (response.isSuccessful) {
                    _myRewards.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Fails silently on the UI, but could be logged locally
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun redeem(token: String, name: String, cost: Double) {
        /*
          Attempts to redeem a new reward. Triggers a Snackbar based on the API response.
         */
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.instance.redeemReward(
                    "Bearer $token", RedeemRequest(name, cost)
                )
                if (response.isSuccessful) {
                    _snackbarMessage.value = "Reward successfully redeemed! Check 'My Codes'."
                    fetchMyRewards(token) // Refresh the wallet automatically
                } else {
                    // API returns 400 Bad Request if points are insufficient
                    _snackbarMessage.value = "Error: Insufficient points."
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Network error. Please try again later."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}