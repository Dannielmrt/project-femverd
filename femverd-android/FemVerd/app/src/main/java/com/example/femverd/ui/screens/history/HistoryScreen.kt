package com.example.femverd.ui.screens.history

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.femverd.R
import com.example.femverd.data.TokenManager
import com.example.femverd.model.HistoryItem

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = viewModel()) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        tokenManager.getToken()?.let { viewModel.fetchHistory(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.padding_medium))
    ) {
        Text(
            text = stringResource(id = R.string.title_history),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
        )

        when (val state = uiState) {
            is HistoryUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is HistoryUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is HistoryUiState.Success -> {
                if (state.historyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.msg_empty_history),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_medium))
                    ) {
                        items(state.historyList) { item ->
                            ExpandableHistoryCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableHistoryCard(item: HistoryItem) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val containerColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        label = "cardColorAnimation"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (expanded) dimensionResource(id = R.dimen.card_elevation_high) else dimensionResource(
                id = R.dimen.card_elevation_low
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Recycling,
                        contentDescription = stringResource(id = R.string.desc_recycling_icon),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_large))
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                    Column {
                        Text(
                            text = item.material_name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(id = R.string.format_kg_recycled, item.quantity),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(
                            id = R.string.format_points_earned,
                            String.format("%.1f", item.generated_points)
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_small)))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = stringResource(id = R.string.desc_toggle_details),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

                val rawDate = item.date ?: ""
                val cleanDate = if (rawDate.length >= 16) {
                    val datePart = rawDate.substring(0, 10)
                    val timePart = rawDate.substring(11, 16)
                    "$datePart at $timePart"
                } else {
                    stringResource(id = R.string.msg_date_unavailable)
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.title_receipt_details),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_small)))

                    Text(
                        text = stringResource(id = R.string.format_where, item.location),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = stringResource(id = R.string.format_when, cleanDate),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = stringResource(id = R.string.format_ticket_id, item.id),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}