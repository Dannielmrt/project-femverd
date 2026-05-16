package com.example.femverd.ui.screens.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.femverd.R
import com.example.femverd.data.TokenManager
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(viewModel: MapViewModel = viewModel()) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val uiState by viewModel.uiState.collectAsState()

    // Base coordinate system tied to the application's core service area
    val defaultLocation = LatLng(39.4699, -0.3763)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    LaunchedEffect(Unit) {
        tokenManager.getToken()?.let { viewModel.fetchMarkers(it) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = dimensionResource(id = R.dimen.spacing_micro)
        ) {
            Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))) {
                Text(
                    text = stringResource(id = R.string.map_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(id = R.string.map_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

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
                // Official Jetpack Compose integration for Google Maps SDK
                GoogleMap(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = dimensionResource(id = R.dimen.map_bottom_padding)),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        compassEnabled = true
                    )
                ) {
                    state.points.forEach { park ->
                        Marker(
                            state = MarkerState(position = LatLng(park.latitude, park.longitude)),
                            title = park.name,
                            snippet = stringResource(id = R.string.map_marker_snippet)
                        )
                    }
                }
            }
        }
    }
}