package com.example.questa.ui.navigation

sealed class Destination(val route: String) {
    data object Login : Destination("login")
    data object Register : Destination("register")
    data object Home : Destination("home")
    data object CreatePoll : Destination("create_poll")
    data object PollDetail : Destination("poll_detail/{pollId}") {
        fun createRoute(pollId: String) = "poll_detail/$pollId"
    }
    data object Profile : Destination("profile")
    data object FavoritePolls : Destination("favorites")
    data object EditProfile : Destination("edit_profile")
} 