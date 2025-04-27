package com.example.questa.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.UUID

@IgnoreExtraProperties
data class Survey(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val category: String = "",
    val questions: Map<String, Question> = mapOf(),
    val totalResponses: Int = 0,
    val likes: Int = 0
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "createdBy" to createdBy,
            "createdAt" to createdAt,
            "category" to category,
            "questions" to questions,
            "totalResponses" to totalResponses,
            "likes" to likes
        )
    }
} 