package com.example.femverd.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.femverd.data.RetrofitClient
import com.example.femverd.data.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    fun login(dni: String, pass: String, tokenManager: TokenManager) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitClient.instance.login(dni, pass)

                if (response.isSuccessful) {
                    val token = response.body()?.access_token
                    if (token != null) {
                        tokenManager.saveToken(token)
                        _loginSuccess.value = true
                    } else {
                        _errorMessage.value = "Error: Invalid token received."
                    }
                } else {
                    _errorMessage.value = "Error: Invalid credentials."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}