package com.example.femverd.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.launch
import com.example.femverd.R
import com.example.femverd.data.RetrofitClient
import com.example.femverd.model.RegisterRequest

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var dni by rememberSaveable { mutableStateOf("") }
    var userName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val handleRegister: () -> Unit = {
        val dniRegex = "^[0-9]{8}[A-Za-z]$".toRegex()
        val emailPattern = android.util.Patterns.EMAIL_ADDRESS

        if (!dni.matches(dniRegex)) {
            errorMessage = "Invalid DNI format (e.g., 12345678A)"
        } else if (userName.trim().length < 3) {
            errorMessage = "Name must be at least 3 characters long"
        } else if (!emailPattern.matcher(email).matches()) {
            errorMessage = "Please enter a valid email address"
        } else if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters long"
        } else {
            coroutineScope.launch {
                isLoading = true
                errorMessage = null
                try {
                    val request = RegisterRequest(dni, userName, email, password)
                    val response = RetrofitClient.instance.registerUser(request)
                    if (response.isSuccessful) {
                        onRegisterSuccess()
                    } else {
                        errorMessage = "Registration failed. DNI or Email might already exist."
                    }
                } catch (e: Exception) {
                    errorMessage = "Network error. Please try again."
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(id = R.dimen.padding_large)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))

        Text(
            text = stringResource(id = R.string.register_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(id = R.string.register_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text(stringResource(id = R.string.prompt_fullname)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        OutlinedTextField(
            value = dni,
            onValueChange = { dni = it.uppercase() },
            label = { Text(stringResource(id = R.string.prompt_dni)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(id = R.string.prompt_email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.prompt_password)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image =
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = stringResource(id = R.string.desc_toggle_password)
                    )
                }
            }
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))

        Button(
            onClick = handleRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.button_height)),
            enabled = !isLoading && dni.isNotBlank() && userName.isNotBlank() && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) CircularProgressIndicator(
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size)),
                color = MaterialTheme.colorScheme.onPrimary
            )
            else Text(stringResource(id = R.string.action_register))
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))

        TextButton(onClick = onNavigateBack) {
            Text(stringResource(id = R.string.nav_to_login))
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))
    }
}