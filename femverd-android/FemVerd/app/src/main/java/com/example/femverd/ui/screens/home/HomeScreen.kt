package com.example.femverd.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.femverd.data.TokenManager

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val uiState by viewModel.uiState.collectAsState()

    // Fetch data when the screen is first displayed
    LaunchedEffect(Unit) {
        tokenManager.getToken()?.let { viewModel.fetchUserData(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Success -> {
                DashboardContent(state.user)
            }
            is HomeUiState.Error -> {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun DashboardContent(user: com.example.femverd.model.UserMe) {
    Column {
        Text(
            text = "Welcome back,",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Points Card (Organic Rounded Corners)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Your Current Balance", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = String.format("%.2f pts", user.current_points),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Identification Button (The "Machine Link")
        Button(
            onClick = { /* Show QR Dialog */ },
            modifier = Modifier.fillMaxWidth().height(70.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("IDENTIFY AT ECOPARK", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
