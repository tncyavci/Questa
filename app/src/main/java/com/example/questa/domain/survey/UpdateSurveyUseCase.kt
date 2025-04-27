package com.example.questa.domain.survey

import com.example.questa.data.model.Survey
import com.example.questa.data.repository.SurveyRepository

class UpdateSurveyUseCase(private val repository: SurveyRepository) {
    suspend operator fun invoke(survey: Survey): Result<Boolean> {
        return repository.updateSurvey(survey)
    }
} 