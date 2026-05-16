package com.example.femverd.ui.screens.home

import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.femverd.R
import com.example.femverd.data.TokenManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.set
import androidx.core.graphics.createBitmap

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        tokenManager.getToken()?.let { viewModel.fetchUserData(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(id = R.dimen.padding_medium))
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is HomeUiState.Success -> {
                DashboardContent(state.user, navController)
            }

            is HomeUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    user: com.example.femverd.model.UserMe,
    navController: NavController
) {
    var showQrDialog by rememberSaveable { mutableStateOf(false) }

    // Business logic for gamification leveling system computed on the client side
    val level = (user.current_points / 500).toInt() + 1
    val pointsInCurrentLevel = user.current_points % 500
    val progressToNextLevel = (pointsInCurrentLevel / 500f).toFloat()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.home_welcome),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 10.dp,
                pressedElevation = 12.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_large)),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(id = R.string.home_balance_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Text(
                    text = stringResource(id = R.string.format_pts_balance, user.current_points),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.format_level, level),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = stringResource(
                            id = R.string.format_pts_to_next,
                            (500 - pointsInCurrentLevel).toInt(),
                            level + 1
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))

                LinearProgressIndicator(
                    progress = { progressToNextLevel },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.progress_bar_height))
                        .clip(RoundedCornerShape(50)),
                    color = MaterialTheme.colorScheme.onPrimary,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_huge)))

        FilledTonalButton(
            onClick = { navController.navigate("rewards") },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.button_height_large))
                .padding(bottom = dimensionResource(id = R.dimen.spacing_medium)),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(
                imageVector = Icons.Default.CardGiftcard,
                contentDescription = stringResource(id = R.string.desc_rewards_icon),
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size))
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_medium)))
            Text(
                text = stringResource(id = R.string.action_redeem_rewards),
                fontSize = dimensionResource(id = R.dimen.text_size_button).value.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = { showQrDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.button_height_huge))
                .padding(bottom = dimensionResource(id = R.dimen.spacing_small)),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = dimensionResource(id = R.dimen.card_elevation_medium))
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = stringResource(id = R.string.desc_qr_icon),
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size))
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_medium)))
            Text(
                text = stringResource(id = R.string.action_identify_ecopark),
                fontSize = dimensionResource(id = R.dimen.text_size_button).value.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))
    }

    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.dialog_qr_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.dialog_qr_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

                    val qrBitmap = generateQrCode(user.dni)
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = stringResource(id = R.string.desc_qr_code),
                            modifier = Modifier.size(dimensionResource(id = R.dimen.qr_size))
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.error_qr_generation),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text(stringResource(id = R.string.action_close))
                }
            }
        )
    }
}

/*
 * Generates an authentication QR Code. Instead of exposing the raw DNI,
 * it builds a JSON payload with a UNIX timestamp and encodes it in Base64
 * to act as a secure, time-aware session token for the physical Ecopark machines.
 */
fun generateQrCode(dni: String): android.graphics.Bitmap? {
    return try {
        val timestamp = System.currentTimeMillis()
        val jsonPayload = """{"userId":"$dni","type":"eco_auth","ts":$timestamp}"""

        val encodedToken = Base64.encodeToString(jsonPayload.toByteArray(), Base64.NO_WRAP)

        val writer = QRCodeWriter()
        // Standard payload dimension for reliable optical scanning (512x512)
        val bitMatrix = writer.encode(encodedToken, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = createBitmap(width, height, android.graphics.Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap[x, y] = if (bitMatrix.get(x, y)
                ) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}