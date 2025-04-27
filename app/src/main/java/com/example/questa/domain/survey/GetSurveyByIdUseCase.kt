package com.example.questa.domain.survey

import com.example.questa.data.model.Survey
import com.example.questa.data.repository.SurveyRepository
import kotlinx.coroutines.flow.Flow

class GetSurveyByIdUseCase(private val repository: SurveyRepository) {
    operator fun invoke(surveyId: String): Flow<Result<Survey?>> {
        return repository.getSurveyById(surveyId)
    }
} 