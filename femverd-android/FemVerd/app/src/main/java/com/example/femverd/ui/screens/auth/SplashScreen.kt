package com.example.femverd.ui.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import com.example.femverd.data.TokenManager
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.ContentScale
import com.example.femverd.R

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "fadeAnimation"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)

        val token = tokenManager.getToken()
        if (!token.isNullOrEmpty()) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceBright),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_v2),
            contentDescription = stringResource(id = R.string.desc_logo),
            contentScale = ContentScale.Crop, // DI
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .alpha(alphaAnim)
        )
    }
}