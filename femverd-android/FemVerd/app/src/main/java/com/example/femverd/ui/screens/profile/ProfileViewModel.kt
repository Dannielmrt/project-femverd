package com.example.femverd.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.femverd.data.RetrofitClient
import com.example.femverd.data.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    // UI States
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun deleteAccount(tokenManager: TokenManager, onSuccess: () -> Unit) {
        /*
          Performs a hard delete of the user account in the backend (AWS),
          clears the local token, and triggers a successful logout state.
         */
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = tokenManager.getToken()
                if (token != null) {
                    val response = RetrofitClient.instance.deleteAccount("Bearer $token")
                    if (response.isSuccessful) {
                        performLogout(tokenManager, onSuccess)
                    } else {
                        _errorMessage.value = "Failed to delete account. Server returned an error."
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun performLogout(tokenManager: TokenManager, onSuccess: () -> Unit) {
        /*
          Clears the JWT from SharedPreferences and triggers navigation to Login.
         */
        tokenManager.clearToken()
        onSuccess()
    }
}