package com.example.femverd.ui.screens.rewards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.femverd.R
import com.example.femverd.data.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    navController: NavController,
    viewModel: RewardsViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    val myRewards by viewModel.myRewards.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        stringResource(id = R.string.tab_catalog),
        stringResource(id = R.string.tab_my_codes)
    )

    LaunchedEffect(Unit) {
        tokenManager.getToken()?.let { viewModel.fetchMyRewards(it) }
    }

    // Snackbar
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_rewards)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.desc_back_home)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
}

@Composable
fun CatalogTab(viewModel: RewardsViewModel, tokenManager: TokenManager) {
    LazyColumn(
        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_medium))
    ) {
        items(viewModel.catalog) { reward ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.card_elevation_low))
            ) {
                Row(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = reward.first,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(
                                id = R.string.format_pts_cost,
                                reward.second.toInt()
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = {
                            tokenManager.getToken()?.let {
                                viewModel.redeem(it, reward.first, reward.second)
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.action_redeem))
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
            Text(
                text = stringResource(id = R.string.msg_empty_codes),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_medium))
        ) {
            items(rewards) { item ->
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))) {
                        Text(
                            text = item.reward_name,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.code,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = dimensionResource(id = R.dimen.letter_spacing_code).value.sp,
                            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.spacing_micro))
                        )
                        Text(
                            text = stringResource(
                                id = R.string.format_redeemed_on,
                                item.date.take(10)
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}