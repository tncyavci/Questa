package com.example.questa.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.UUID

@IgnoreExtraProperties
data class Option(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val votes: Int = 0,
    val image: String? = null  // İsteğe bağlı görsel URL'si
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "text" to text,
            "votes" to votes,
            "image" to image
        )
    }
} 