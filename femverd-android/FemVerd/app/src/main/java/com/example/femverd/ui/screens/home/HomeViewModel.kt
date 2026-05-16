package com.example.femverd.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.femverd.data.RetrofitClient
import com.example.femverd.model.UserMe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val user: UserMe) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    fun fetchUserData(token: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val response = RetrofitClient.instance.getMe("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = HomeUiState.Success(response.body()!!)
                } else {
                    _uiState.value = HomeUiState.Error("Could not fetch user data")
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}