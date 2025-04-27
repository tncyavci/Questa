package com.example.questa.data.model

import java.util.Date

data class Comment(
    val id: String = "",
    val pollId: String = "",
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val createdAt: Long = Date().time
) 