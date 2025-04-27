package com.example.questa.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.questa.presentation.auth.AuthState
import com.example.questa.presentation.auth.AuthViewModel
import com.example.questa.presentation.profile.ProfileEvent
import com.example.questa.presentation.profile.ProfileState
import com.example.questa.presentation.profile.ProfileViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.example.questa.ui.components.CustomTextField
import com.example.questa.ui.components.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val profileState by profileViewModel.profileState.collectAsStateWithLifecycle()
    
    val currentUser = (authState as? AuthState.Authenticated)?.user
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var username by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Effect to handle profile state changes
    LaunchedEffect(profileState) {
        when (profileState) {
            is ProfileState.Loading -> isLoading = true
            is ProfileState.Success -> {
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar("Profile updated successfully")
                }
                onBack()
            }
            is ProfileState.Error -> {
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar((profileState as ProfileState.Error).message)
                }
            }
            else -> isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (currentUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("You need to be logged in to edit your profile")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile picture placeholder
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // Future implementation: Add button to change profile picture
                Button(
                    onClick = { /* Add image selection functionality later */ },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Change Profile Picture")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Username field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Save button
                Button(
                    onClick = {
                        currentUser.uid.let { userId ->
                            profileViewModel.onEvent(ProfileEvent.UpdateProfile(
                                userId = userId,
                                username = username
                            ))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && username.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
} 