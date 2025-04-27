package com.example.questa.domain.survey

import com.example.questa.data.repository.SurveyRepository

class LikeSurveyUseCase(private val repository: SurveyRepository) {
    suspend operator fun invoke(surveyId: String): Result<Boolean> {
        return repository.likeSurvey(surveyId)
    }
} 