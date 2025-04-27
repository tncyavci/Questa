package com.example.questa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.questa.presentation.auth.AuthState
import com.example.questa.presentation.auth.AuthViewModel
import com.example.questa.ui.screens.auth.LoginScreen
import com.example.questa.ui.screens.auth.RegisterScreen
import com.example.questa.ui.screens.main.CreatePollScreen
import com.example.questa.ui.screens.main.HomeScreen
import com.example.questa.ui.screens.main.PollDetailScreen
import com.example.questa.ui.screens.profile.EditProfileScreen
import com.example.questa.ui.screens.profile.ProfileScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Destination.Login.route,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    
    val actualStartDestination = when (authState) {
        is AuthState.Authenticated -> Destination.Home.route
        else -> startDestination
    }
    
    NavHost(
        navController = navController,
        startDestination = actualStartDestination,
        modifier = modifier
    ) {
        // Auth Screens
        composable(Destination.Login.route) {
            LoginScreen(
                navigateToRegister = { navController.navigate(Destination.Register.route) },
                navigateToHome = { navController.navigate(Destination.Home.route) {
                    popUpTo(Destination.Login.route) { inclusive = true }
                }}
            )
        }
        
        composable(Destination.Register.route) {
            RegisterScreen(
                navigateToLogin = { navController.navigate(Destination.Login.route) {
                    popUpTo(Destination.Register.route) { inclusive = true }
                }},
                navigateToHome = { navController.navigate(Destination.Home.route) {
                    popUpTo(Destination.Register.route) { inclusive = true }
                }}
            )
        }
        
        // Main App Screens
        composable(Destination.Home.route) {
            HomeScreen(
                navigateToPollDetail = { pollId ->
                    navController.navigate(Destination.PollDetail.createRoute(pollId))
                },
                navigateToCreatePoll = {
                    navController.navigate(Destination.CreatePoll.route)
                },
                navigateToProfile = {
                    navController.navigate(Destination.Profile.route)
                }
            )
        }
        
        composable(Destination.CreatePoll.route) {
            CreatePollScreen(
                navController = navController
            )
        }
        
        composable(Destination.PollDetail.route) { backStackEntry ->
            val pollId = backStackEntry.arguments?.getString("pollId") ?: ""
            PollDetailScreen(
                pollId = pollId,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Destination.Profile.route) {
            ProfileScreen(
                navigateToLogin = {
                    navController.navigate(Destination.Login.route) {
                        popUpTo(Destination.Home.route) { inclusive = true }
                    }
                },
                navigateToPollDetail = { pollId ->
                    navController.navigate(Destination.PollDetail.createRoute(pollId))
                },
                navigateToEditProfile = {
                    navController.navigate(Destination.EditProfile.route)
                }
            )
        }
        
        composable(Destination.EditProfile.route) {
            EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Destination.FavoritePolls.route) {
            // FavoritePollsScreen will be implemented later
        }
    }
} 