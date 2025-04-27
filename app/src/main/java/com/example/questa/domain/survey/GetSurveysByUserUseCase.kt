package com.example.questa.domain.survey

import com.example.questa.data.model.Survey
import com.example.questa.data.repository.SurveyRepository
import kotlinx.coroutines.flow.Flow

class GetSurveysByUserUseCase(private val repository: SurveyRepository) {
    operator fun invoke(userId: String): Flow<Result<List<Survey>>> {
        return repository.getSurveysByUser(userId)
    }
} 