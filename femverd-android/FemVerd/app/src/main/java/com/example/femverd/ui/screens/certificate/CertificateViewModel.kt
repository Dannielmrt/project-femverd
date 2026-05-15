package com.example.femverd.ui.screens.certificate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.femverd.data.RetrofitClient
import com.example.femverd.model.CertificateResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class CertificateViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _certificateData = MutableStateFlow<CertificateResponse?>(null)
    val certificateData: StateFlow<CertificateResponse?> = _certificateData

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchCertificate(token: String, year: Int = Calendar.getInstance().get(Calendar.YEAR)) {
        /*
          Fetches the official recycling certificate for a specific year.
         */
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.instance.getAnnualCertificate("Bearer $token", year)
                if (response.isSuccessful && response.body() != null) {
                    _certificateData.value = response.body()
                } else {
                    _errorMessage.value = "Could not retrieve certificate data."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error. Please try again later."
            } finally {
                _isLoading.value = false
            }
        }
    }
}