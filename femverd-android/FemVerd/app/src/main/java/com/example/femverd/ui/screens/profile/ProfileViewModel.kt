package com.example.femverd.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.femverd.data.RetrofitClient
import com.example.femverd.data.TokenManager
import com.example.femverd.model.UserMe
import com.example.femverd.model.UserUpdateRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _userProfile = MutableStateFlow<UserMe?>(null)
    val userProfile: StateFlow<UserMe?> = _userProfile

    fun fetchProfile(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.instance.getMe("Bearer $token")
                if (response.isSuccessful) {
                    _userProfile.value = response.body()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load profile."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(token: String, newUserName: String, newEmail: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = UserUpdateRequest(user_name = newUserName, email = newEmail)
                val response = RetrofitClient.instance.updateProfile("Bearer $token", request)

                if (response.isSuccessful) {
                    fetchProfile(token)
                    onSuccess()
                } else {
                    _errorMessage.value = "Failed to update profile."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error during update."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAccount(tokenManager: TokenManager, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.instance.deleteAccount("Bearer $token")
                if (response.isSuccessful) {
                    performLogout(tokenManager, onSuccess)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error while deleting account."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun performLogout(tokenManager: TokenManager, onSuccess: () -> Unit) {
        tokenManager.clearToken()
        onSuccess()
    }
}