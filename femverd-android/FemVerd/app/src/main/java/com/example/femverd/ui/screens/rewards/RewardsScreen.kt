package com.example.femverd.ui.screens.rewards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun RewardsScreen(viewModel: RewardsViewModel = viewModel()) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val myRewards by viewModel.myRewards.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Catalog", "My Codes")

    LaunchedEffect(Unit) {
        tokenManager.getToken()?.let { viewModel.fetchMyRewards(it) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        when (selectedTab) {
            0 -> CatalogTab(viewModel, tokenManager)
            1 -> MyCodesTab(myRewards)
        }
    }
}

@Composable
fun CatalogTab(viewModel: RewardsViewModel, tokenManager: TokenManager) {
    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(viewModel.catalog) { reward ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(reward.first, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${reward.second.toInt()} pts", color = MaterialTheme.colorScheme.primary)
                    }
                    Button(onClick = {
                        tokenManager.getToken()?.let { viewModel.redeem(it, reward.first, reward.second) {} }
                    }) {
                        Text("REDEEM")
                    }
                }
            }
        }
    }
}

@Composable
fun MyCodesTab(rewards: List<com.example.femverd.model.RedemptionItem>) {
    if (rewards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No codes yet. Redeem some points!")
        }
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(rewards) { item ->
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(item.reward_name, fontWeight = FontWeight.Bold)
                        Text(
                            text = item.code,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp
                        )
                        Text("Redeemed on: ${item.date.take(10)}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}