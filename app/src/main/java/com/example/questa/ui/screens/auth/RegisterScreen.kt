package com.example.questa.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.questa.presentation.auth.AuthEvent
import com.example.questa.presentation.auth.AuthState
import com.example.questa.presentation.auth.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(
    navigateToLogin: () -> Unit,
    navigateToHome: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> navigateToHome()
            is AuthState.Error -> errorMessage = (authState as AuthState.Error).message
            else -> {}
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = password != confirmPassword && confirmPassword.isNotEmpty()
            )
            
            if (password != confirmPassword && confirmPassword.isNotEmpty()) {
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
            
            Button(
                onClick = {
                    if (password == confirmPassword) {
                        viewModel.onEvent(AuthEvent.SignUp(email, password, username))
                    } else {
                        errorMessage = "Passwords do not match"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = username.isNotBlank() && email.isNotBlank() && 
                          password.isNotBlank() && confirmPassword.isNotBlank() &&
                          password == confirmPassword && authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }
                Text("Register")
            }
            
            TextButton(
                onClick = navigateToLogin,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Already have an account? Sign in")
            }
        }
        
        if (authState is AuthState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
} 