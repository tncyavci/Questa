package com.example.questa.util

/**
 * Uygulama genelinde kullanılan sabit değerler
 */
object Constants {
    // Firebase Database yolları
    const val USERS_REF = "users"
    const val POLLS_REF = "polls"
    const val COMMENTS_REF = "comments"
    const val SURVEYS_REF = "surveys"
    const val SURVEY_RESPONSES_REF = "survey_responses"
    
    // Anket kategorileri
    val POLL_CATEGORIES = listOf(
        "General",
        "Technology",
        "Sports",
        "Education",
        "Entertainment",
        "Politics",
        "Health"
    )
    
    // Anket kategorileri (Türkçe)
    val SURVEY_CATEGORIES = listOf(
        "Genel",
        "Teknoloji",
        "Spor",
        "Eğitim",
        "Eğlence",
        "Politika",
        "Sağlık",
        "İş Dünyası",
        "Seyahat",
        "Yaşam Tarzı"
    )
    
    // Arayüz sabitleri
    const val MIN_POLL_OPTIONS = 2
    const val MAX_POLL_OPTIONS = 10
    const val MAX_QUESTION_LENGTH = 200
    const val MAX_OPTION_LENGTH = 100
    const val MAX_COMMENT_LENGTH = 500
    
    // Anket sabitleri
    const val MIN_SURVEY_QUESTIONS = 1
    const val MAX_SURVEY_QUESTIONS = 20
    const val MAX_SURVEY_TITLE_LENGTH = 100
    const val MAX_SURVEY_DESCRIPTION_LENGTH = 500
    
    // Zamanlama sabitleri (milisaniye)
    const val ONE_HOUR_MILLIS = 60 * 60 * 1000L
    const val ONE_DAY_MILLIS = 24 * ONE_HOUR_MILLIS
    const val ONE_WEEK_MILLIS = 7 * ONE_DAY_MILLIS
    
    // Bildirim kanalları
    const val NOTIFICATION_CHANNEL_ID = "questa_notifications"
    const val NOTIFICATION_CHANNEL_NAME = "Questa Bildirimleri"
} 