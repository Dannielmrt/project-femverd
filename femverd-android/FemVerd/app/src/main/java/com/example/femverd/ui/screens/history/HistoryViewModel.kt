package com.example.femverd.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.femverd.data.RetrofitClient
import com.example.femverd.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// UI States representing the operational lifecycle of the history request
sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val historyList: List<HistoryItem>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class HistoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState

    fun fetchHistory(token: String) {
    /*
      Connects to the AWS backend to retrieve the user's secure recycling logs.
     */
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            try {
                // Execute network request with Bearer JWT Token authentication
                val response = RetrofitClient.instance.getHistory("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = HistoryUiState.Success(response.body()!!)
                } else {
                    _uiState.value = HistoryUiState.Error("No recycling records found or server issue.")
                }
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error("Network error: Please check your internet connection.")
            }
        }
    }
}