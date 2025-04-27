package com.example.questa.domain.survey

import com.example.questa.data.repository.SurveyRepository

class AddSampleSurveysUseCase(private val repository: SurveyRepository) {
    suspend operator fun invoke(): Result<List<String>> {
        return repository.addSampleSurveys()
    }
} 