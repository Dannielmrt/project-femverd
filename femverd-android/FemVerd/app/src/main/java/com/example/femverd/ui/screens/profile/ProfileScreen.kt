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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.femverd.data.TokenManager

@OptIn(ExperimentalMaterial3Api::class) // Required for TopAppBar
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    // UI States
    val isLoading by viewModel.isLoading.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    // Dialog and Editing states
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var isEditing by rememberSaveable { mutableStateOf(false) }

    // Text field states
    var userNameField by rememberSaveable { mutableStateOf("") }
    var emailField by rememberSaveable { mutableStateOf("") }

    // Fetch profile data on initialization
    LaunchedEffect(Unit) {
        tokenManager.getToken()?.let { viewModel.fetchProfile(it) }
    }

    // Populate fields when profile data arrives
    LaunchedEffect(userProfile) {
        userProfile?.let {
            userNameField = it.name
            emailField = it.email ?: ""
        }
    }

    // Navigation logic to clear backstack and return to login
    val navigateToLogin = {
        navController.navigate("login") {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
        }
    }

    // Scaffold provides the TopAppBar for back navigation
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Avatar Placeholder
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Avatar",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (userProfile == null) {
                CircularProgressIndicator()
            } else {
                // DNI Field: Always Read-Only for legibility
                OutlinedTextField(
                    value = userProfile!!.dni,
                    onValueChange = {},
                    label = { Text("DNI / Eco-ID") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // User Name Field: Uses readOnly to maintain text contrast when disabled
                OutlinedTextField(
                    value = userNameField,
                    onValueChange = { userNameField = it },
                    label = { Text("User Name") },
                    readOnly = !isEditing,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Field
                OutlinedTextField(
                    value = emailField,
                    onValueChange = { emailField = it },
                    label = { Text("Email Address") },
                    readOnly = !isEditing,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Toggle between Save and Edit states
                if (isEditing) {
                    Button(
                        onClick = {
                            tokenManager.getToken()?.let {
                                viewModel.updateProfile(it, userNameField, emailField) {
                                    isEditing = false // Exit edit mode on success
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SAVE CHANGES")
                    }
                } else {
                    FilledTonalButton(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EDIT PROFILE")
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(48.dp))

            // Session Management Actions
            OutlinedButton(
                onClick = { viewModel.performLogout(tokenManager, onSuccess = navigateToLogin) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout Icon")
                Spacer(modifier = Modifier.width(12.dp))
                Text("LOG OUT")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Delete Icon")
                Spacer(modifier = Modifier.width(12.dp))
                Text("DELETE ACCOUNT")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Critical Action Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Are you absolutely sure?") },
            text = { Text("This action cannot be undone. All your recycling history and points will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount(tokenManager, onSuccess = navigateToLogin)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("YES, DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("CANCEL") }
            }
        )
    }
}