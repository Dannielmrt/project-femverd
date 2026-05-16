package com.example.femverd.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.femverd.data.RetrofitClient
import com.example.femverd.model.GreenPointMarker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MapUiState {
    object Loading : MapUiState()
    data class Success(val points: List<GreenPointMarker>) : MapUiState()
    data class Error(val message: String) : MapUiState()
}

class MapViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState

    /*
     * Dispatches an asynchronous network request to fetch the spatial coordinate nodes
     * from the RESTful backend infrastructure.
     */
    fun fetchMarkers(token: String) {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading
            try {
                val response = RetrofitClient.instance.getGreenPoints("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = MapUiState.Success(response.body()!!)
                } else {
                    _uiState.value = MapUiState.Error("Server returned an empty markers payload.")
                }
            } catch (e: Exception) {
                _uiState.value = MapUiState.Error("Failed to synchronize map.")
            }
        }
    }
}