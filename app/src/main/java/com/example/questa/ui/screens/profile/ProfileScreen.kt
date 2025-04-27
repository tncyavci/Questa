package com.example.questa.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.questa.presentation.auth.AuthEvent
import com.example.questa.presentation.auth.AuthState
import com.example.questa.presentation.auth.AuthViewModel
import com.example.questa.presentation.poll.PollEvent
import com.example.questa.presentation.poll.PollState
import com.example.questa.presentation.poll.PollViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navigateToLogin: () -> Unit,
    navigateToPollDetail: (String) -> Unit,
    navigateToEditProfile: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel(),
    pollViewModel: PollViewModel = koinViewModel { 
        parametersOf(authViewModel.authState.value.let { 
            if (it is AuthState.Authenticated) it.user.uid else ""
        })
    }
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val pollsState by pollViewModel.pollsState.collectAsStateWithLifecycle()
    
    // Get current user info
    val currentUser = (authState as? AuthState.Authenticated)?.user
    
    // Load user polls
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { userId ->
            pollViewModel.onEvent(PollEvent.LoadUserPolls(userId))
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.onEvent(AuthEvent.SignOut)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign out"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (currentUser == null) {
            // Handle unauthenticated state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "You are not logged in",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(onClick = navigateToLogin) {
                    Text("Sign In")
                }
            }
        } else {
            // Show user profile
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // User info card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile image
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // User name
                        Text(
                            text = currentUser.displayName ?: "User",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // User email
                        Text(
                            text = currentUser.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = navigateToEditProfile,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile"
                            )
                            
                            Spacer(modifier = Modifier.size(8.dp))
                            
                            Text("Edit Profile")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // User polls section
                Text(
                    text = "My Polls",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                when (val state = pollsState) {
                    is PollState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is PollState.Success -> {
                        val userPolls = state.polls
                        
                        if (userPolls.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "You haven't created any polls yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column {
                                userPolls.forEach { poll ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        onClick = { navigateToPollDetail(poll.id) }
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = poll.question,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Category: ${poll.category}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                
                                                Text(
                                                    text = "Votes: ${poll.totalVotes}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is PollState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }
    }
} 