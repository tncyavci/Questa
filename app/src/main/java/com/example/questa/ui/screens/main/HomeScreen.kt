package com.example.questa.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.questa.data.model.Poll
import com.example.questa.presentation.auth.AuthViewModel
import com.example.questa.presentation.poll.PollEvent
import com.example.questa.presentation.poll.PollState
import com.example.questa.presentation.poll.PollViewModel
import com.example.questa.ui.components.PollItem
import com.example.questa.ui.navigation.Destination
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

enum class PollTab {
    ALL, FEATURED, FAVORITES, MY_POLLS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToPollDetail: (String) -> Unit,
    navigateToCreatePoll: () -> Unit,
    navigateToProfile: () -> Unit = {},
    authViewModel: AuthViewModel = koinViewModel(),
    pollViewModel: PollViewModel = koinViewModel { 
        parametersOf(authViewModel.authState.value.let { 
            if (it is com.example.questa.presentation.auth.AuthState.Authenticated) it.user.uid else ""
        })
    }
) {
    val pollsState by pollViewModel.pollsState.collectAsStateWithLifecycle()
    val userFavorites by pollViewModel.userFavorites.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(PollTab.ALL) }
    
    // Load polls based on selected tab
    LaunchedEffect(selectedTab, userFavorites) {
        when (selectedTab) {
            PollTab.ALL -> pollViewModel.onEvent(PollEvent.LoadAllPolls)
            PollTab.FEATURED -> pollViewModel.onEvent(PollEvent.LoadFeaturedPolls)
            PollTab.FAVORITES -> {
                pollViewModel.onEvent(PollEvent.LoadAllPolls)
            }
            PollTab.MY_POLLS -> {
                val userId = authViewModel.authState.value.let { 
                    if (it is com.example.questa.presentation.auth.AuthState.Authenticated) it.user.uid else ""
                }
                pollViewModel.onEvent(PollEvent.LoadUserPolls(userId))
            }
        }
    }
    
    // Load user favorites
    LaunchedEffect(Unit) {
        pollViewModel.onEvent(PollEvent.LoadUserFavorites)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Questa") },
                actions = {
                    IconButton(onClick = { navigateToProfile() }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToCreatePoll,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Poll"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                PollTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { 
                            Text(
                                text = when(tab) {
                                    PollTab.ALL -> "All Polls"
                                    PollTab.FEATURED -> "Featured"
                                    PollTab.FAVORITES -> "Favorites"
                                    PollTab.MY_POLLS -> "My Polls"
                                }
                            )
                        }
                    )
                }
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = pollsState) {
                    is PollState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is PollState.Success -> {
                        val displayedPolls = if (selectedTab == PollTab.FAVORITES) {
                            // Favorilerde olan anketleri filtrele
                            state.polls.filter { poll -> userFavorites.contains(poll.id) }
                        } else {
                            state.polls
                        }
                        
                        if (displayedPolls.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = when (selectedTab) {
                                        PollTab.ALL -> "No polls available"
                                        PollTab.FEATURED -> "No featured polls"
                                        PollTab.FAVORITES -> "No favorite polls"
                                        PollTab.MY_POLLS -> "You haven't created any polls"
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        } else {
                            PollsList(
                                polls = displayedPolls,
                                userFavorites = userFavorites,
                                onPollClick = navigateToPollDetail,
                                onFavoriteClick = { pollId ->
                                    pollViewModel.onEvent(PollEvent.ToggleFavorite(pollId))
                                }
                            )
                        }
                    }
                    is PollState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PollsList(
    polls: List<Poll>,
    userFavorites: Set<String>,
    onPollClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(polls, key = { it.id }) { poll ->
            PollItem(
                poll = poll,
                isFavorite = userFavorites.contains(poll.id),
                onPollClick = onPollClick,
                onFavoriteClick = onFavoriteClick
            )
        }
    }
} 