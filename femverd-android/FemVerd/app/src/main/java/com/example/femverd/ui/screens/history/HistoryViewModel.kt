package com.example.femverd.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.femverd.data.RetrofitClient
import com.example.femverd.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val historyList: List<HistoryItem>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class HistoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState

    fun fetchHistory(token: String) {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            try {
                val response = RetrofitClient.instance.getHistory("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = HistoryUiState.Success(response.body()!!)
                } else {
                    // Handled as plain strings intentionally for remote error parsing scalability
                    _uiState.value =
                        HistoryUiState.Error("No recycling records found or server issue.")
                }
            } catch (e: Exception) {
                _uiState.value =
                    HistoryUiState.Error("Network error: Please check your internet connection.")
            }
        }
    }
}