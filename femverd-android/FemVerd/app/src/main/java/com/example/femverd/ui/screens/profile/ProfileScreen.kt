package com.example.femverd.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.femverd.R
import com.example.femverd.data.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    val isLoading by viewModel.isLoading.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var isEditing by rememberSaveable { mutableStateOf(false) }

    var userNameField by rememberSaveable { mutableStateOf("") }
    var emailField by rememberSaveable { mutableStateOf("") }

    // Binds data fetching to the initial composition lifecycle
    LaunchedEffect(Unit) {
        tokenManager.getToken()?.let { viewModel.fetchProfile(it) }
    }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            userNameField = it.name
            emailField = it.email
        }
    }

    // Clears the navigation backstack to prevent unauthorized return after session termination
    val navigateToLogin = {
        navController.navigate("login") {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_profile)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
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
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = dimensionResource(id = R.dimen.padding_large)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(id = R.string.desc_avatar),
                modifier = Modifier.size(dimensionResource(id = R.dimen.avatar_size)),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

            if (userProfile == null) {
                CircularProgressIndicator()
            } else {
                OutlinedTextField(
                    value = userProfile!!.dni,
                    onValueChange = {},
                    label = { Text(stringResource(id = R.string.prompt_dni)) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

                OutlinedTextField(
                    value = userNameField,
                    onValueChange = { userNameField = it },
                    label = { Text(stringResource(id = R.string.prompt_user_name)) },
                    readOnly = !isEditing,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

                OutlinedTextField(
                    value = emailField,
                    onValueChange = { emailField = it },
                    label = { Text(stringResource(id = R.string.prompt_email)) },
                    readOnly = !isEditing,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

                if (isEditing) {
                    Button(
                        onClick = {
                            tokenManager.getToken()?.let {
                                viewModel.updateProfile(it, userNameField, emailField) {
                                    isEditing = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.button_height))
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_small)))
                        Text(stringResource(id = R.string.action_save_changes))
                    }
                } else {
                    FilledTonalButton(
                        onClick = { isEditing = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.button_height))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_small)))
                        Text(stringResource(id = R.string.action_edit_profile))
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_huge)))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_huge)))

            OutlinedButton(
                onClick = { viewModel.performLogout(tokenManager, onSuccess = navigateToLogin) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.button_height_large)),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_medium)))
                Text(stringResource(id = R.string.action_logout))
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.button_height_large)),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_medium)))
                Text(stringResource(id = R.string.action_delete_account))
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(id = R.string.dialog_delete_title)) },
            text = { Text(stringResource(id = R.string.dialog_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount(tokenManager, onSuccess = navigateToLogin)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(id = R.string.action_yes_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(id = R.string.action_cancel))
                }
            }
        )
    }
}