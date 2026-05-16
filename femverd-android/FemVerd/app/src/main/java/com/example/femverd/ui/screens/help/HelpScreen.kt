package com.example.femverd.ui.screens.help

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.femverd.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navController: NavController) {

    val faqs = listOf(
        Pair(R.string.faq_q1, R.string.faq_a1),
        Pair(R.string.faq_q2, R.string.faq_a2),
        Pair(R.string.faq_q3, R.string.faq_a3),
        Pair(R.string.faq_q4, R.string.faq_a4)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_help)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.desc_go_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.padding_medium)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_medium))
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.title_faq),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
                    )
                }

                items(faqs) { faq ->
                    FaqCard(
                        question = stringResource(id = faq.first),
                        answer = stringResource(id = faq.second)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))

                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.button_height_large))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactSupport,
                            contentDescription = stringResource(id = R.string.desc_support_icon)
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_small)))
                        Text(stringResource(id = R.string.action_contact_support))
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))
                }
            }
        }
    }
}

@Composable
fun FaqCard(question: String, answer: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = stringResource(id = R.string.desc_toggle_answer),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}