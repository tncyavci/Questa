package com.example.questa.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val bio: String = "",
    val createdPolls: List<String> = emptyList(),
    val votedPolls: List<String> = emptyList(),
    val favoritePolls: List<String> = emptyList(),
    val profileImageUrl: String = ""
) 