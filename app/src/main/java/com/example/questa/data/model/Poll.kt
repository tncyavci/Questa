package com.example.questa.data.model

import java.util.UUID

data class Poll(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val creatorName: String = "",
    val questions: List<QuestionItem> = listOf(),
    val totalVotes: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val category: String = "General",
    val isFeatured: Boolean = false,
    val likes: List<String> = listOf()
) {
    // For backward compatibility with single question polls
    val question: String = if (questions.isNotEmpty()) questions[0].question else ""
    val options: List<PollOption> = if (questions.isNotEmpty()) questions[0].options else listOf()
}

data class QuestionItem(
    val id: String = UUID.randomUUID().toString(),
    val question: String = "",
    val options: List<PollOption> = listOf()
)

data class PollOption(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val votes: Int = 0,
    val percentage: Float = 0f
) 