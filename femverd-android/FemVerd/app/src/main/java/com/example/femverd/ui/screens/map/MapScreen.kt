package com.example.femverd.ui.screens.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.femverd.data.TokenManager
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(viewModel: MapViewModel = viewModel()) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val uiState by viewModel.uiState.collectAsState()

    // Position camera initially focus around Valencia region center
    val defaultLocation = LatLng(39.4699, -0.3763)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    // Refresh markers list proactively upon rendering
    LaunchedEffect(Unit) {
        tokenManager.getToken()?.let { viewModel.fetchMarkers(it) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ecopark Interactive Map",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Real-time recycling nodes pulled securely from AWS cloud",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // State Machine Interface evaluation
        when (val state = uiState) {
            is MapUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is MapUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is MapUiState.Success -> {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        compassEnabled = true
                    )
                ) {
                    // Populate markers dynamically from spatial database entries
                    state.points.forEach { park ->
                        Marker(
                            state = MarkerState(position = LatLng(park.latitude, park.longitude)),
                            title = park.name,
                            snippet = "Official FemVerd Collection Hub"
                        )
                    }
                }
            }
        }
    }
}