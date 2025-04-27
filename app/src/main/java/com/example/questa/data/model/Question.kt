package com.example.questa.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.UUID

@IgnoreExtraProperties
data class Question(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val options: Map<String, Option> = mapOf(),
    val order: Int = 0,  // Soruların sırasını belirlemek için
    val required: Boolean = true
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "text" to text,
            "type" to type.name,
            "options" to options,
            "order" to order,
            "required" to required
        )
    }
}

enum class QuestionType {
    MULTIPLE_CHOICE,  // Çoktan seçmeli (tek seçim)
    CHECKBOX,         // Çoktan seçmeli (çoklu seçim)
    TEXT,             // Metin girişi
    RATING,           // Değerlendirme (yıldız veya puan)
    YES_NO            // Evet/Hayır sorusu
} 