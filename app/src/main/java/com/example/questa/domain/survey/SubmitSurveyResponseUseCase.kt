package com.example.questa.domain.survey

import com.example.questa.data.model.SurveyResponse
import com.example.questa.data.repository.SurveyRepository

class SubmitSurveyResponseUseCase(private val repository: SurveyRepository) {
    suspend operator fun invoke(response: SurveyResponse): Result<String> {
        return repository.submitSurveyResponse(response)
    }
} 