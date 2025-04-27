package com.example.questa.domain.survey

import com.example.questa.data.model.Survey
import com.example.questa.data.repository.SurveyRepository

class CreateSurveyUseCase(private val repository: SurveyRepository) {
    suspend operator fun invoke(survey: Survey): Result<String> {
        return repository.createSurvey(survey)
    }
} 