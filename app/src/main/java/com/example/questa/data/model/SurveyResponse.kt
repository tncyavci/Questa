package com.example.questa.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.UUID

@IgnoreExtraProperties
data class SurveyResponse(
    val id: String = UUID.randomUUID().toString(),
    val surveyId: String = "",
    val userId: String = "",
    val completedAt: Long = System.currentTimeMillis(),
    val answers: Map<String, Answer> = mapOf()  // questionId -> Answer
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "surveyId" to surveyId,
            "userId" to userId,
            "completedAt" to completedAt,
            "answers" to answers
        )
    }
}

@IgnoreExtraProperties
data class Answer(
    val questionId: String = "",
    val selectedOptionIds: List<String> = listOf(), // Çoklu seçim için liste
    val textAnswer: String? = null,                 // Metin soruları için
    val ratingValue: Int? = null                    // Değerlendirme soruları için
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "questionId" to questionId,
            "selectedOptionIds" to selectedOptionIds,
            "textAnswer" to textAnswer,
            "ratingValue" to ratingValue
        )
    }
} 