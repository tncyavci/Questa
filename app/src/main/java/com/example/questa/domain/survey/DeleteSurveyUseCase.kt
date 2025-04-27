package com.example.questa.domain.survey

import com.example.questa.data.repository.SurveyRepository

class DeleteSurveyUseCase(private val repository: SurveyRepository) {
    suspend operator fun invoke(surveyId: String): Result<Boolean> {
        return repository.deleteSurvey(surveyId)
    }
} 