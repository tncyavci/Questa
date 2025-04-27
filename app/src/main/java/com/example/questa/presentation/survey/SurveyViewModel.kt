package com.example.questa.presentation.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.questa.data.model.Answer
import com.example.questa.data.model.Survey
import com.example.questa.data.model.SurveyResponse
import com.example.questa.domain.survey.AddSampleSurveysUseCase
import com.example.questa.domain.survey.CreateSurveyUseCase
import com.example.questa.domain.survey.DeleteSurveyUseCase
import com.example.questa.domain.survey.GetAllSurveysUseCase
import com.example.questa.domain.survey.GetSurveyByIdUseCase
import com.example.questa.domain.survey.GetSurveysByUserUseCase
import com.example.questa.domain.survey.LikeSurveyUseCase
import com.example.questa.domain.survey.SubmitSurveyResponseUseCase
import com.example.questa.domain.survey.UpdateSurveyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class SurveyViewModel(
    private val createSurveyUseCase: CreateSurveyUseCase,
    private val getAllSurveysUseCase: GetAllSurveysUseCase,
    private val getSurveyByIdUseCase: GetSurveyByIdUseCase,
    private val getSurveysByUserUseCase: GetSurveysByUserUseCase,
    private val updateSurveyUseCase: UpdateSurveyUseCase,
    private val deleteSurveyUseCase: DeleteSurveyUseCase,
    private val submitSurveyResponseUseCase: SubmitSurveyResponseUseCase,
    private val likeSurveyUseCase: LikeSurveyUseCase,
    private val addSampleSurveysUseCase: AddSampleSurveysUseCase
) : ViewModel() {

    // State for survey list
    private val _surveysState = MutableStateFlow<SurveysState>(SurveysState.Loading)
    val surveysState: StateFlow<SurveysState> = _surveysState.asStateFlow()

    // State for single survey
    private val _surveyState = MutableStateFlow<SurveyState>(SurveyState.Loading)
    val surveyState: StateFlow<SurveyState> = _surveyState.asStateFlow()

    // State for survey creation/modification
    private val _surveyOperationState = MutableStateFlow<SurveyOperationState>(SurveyOperationState.Idle)
    val surveyOperationState: StateFlow<SurveyOperationState> = _surveyOperationState.asStateFlow()

    // State for survey response submission
    private val _responseState = MutableStateFlow<ResponseState>(ResponseState.Idle)
    val responseState: StateFlow<ResponseState> = _responseState.asStateFlow()

    // Current user's responses for the active survey
    private val _userResponses = MutableStateFlow<Map<String, Answer>>(mapOf())
    val userResponses: StateFlow<Map<String, Answer>> = _userResponses.asStateFlow()

    // Load all surveys
    fun getAllSurveys() {
        _surveysState.value = SurveysState.Loading
        viewModelScope.launch {
            getAllSurveysUseCase().collect { result ->
                _surveysState.value = result.fold(
                    onSuccess = { surveys ->
                        if (surveys.isEmpty()) {
                            SurveysState.Empty
                        } else {
                            SurveysState.Success(surveys)
                        }
                    },
                    onFailure = { error ->
                        Timber.e(error, "Failed to get surveys")
                        SurveysState.Error(error.message ?: "Failed to load surveys")
                    }
                )
            }
        }
    }

    // Get survey by ID
    fun getSurveyById(surveyId: String) {
        _surveyState.value = SurveyState.Loading
        viewModelScope.launch {
            getSurveyByIdUseCase(surveyId).collect { result ->
                _surveyState.value = result.fold(
                    onSuccess = { survey ->
                        if (survey != null) {
                            SurveyState.Success(survey)
                        } else {
                            SurveyState.NotFound
                        }
                    },
                    onFailure = { error ->
                        Timber.e(error, "Failed to get survey")
                        SurveyState.Error(error.message ?: "Failed to load survey")
                    }
                )
            }
        }
    }

    // Get surveys by user
    fun getSurveysByUser(userId: String) {
        _surveysState.value = SurveysState.Loading
        viewModelScope.launch {
            getSurveysByUserUseCase(userId).collect { result ->
                _surveysState.value = result.fold(
                    onSuccess = { surveys ->
                        if (surveys.isEmpty()) {
                            SurveysState.Empty
                        } else {
                            SurveysState.Success(surveys)
                        }
                    },
                    onFailure = { error ->
                        Timber.e(error, "Failed to get user surveys")
                        SurveysState.Error(error.message ?: "Failed to load user surveys")
                    }
                )
            }
        }
    }

    // Create new survey
    fun createSurvey(survey: Survey) {
        _surveyOperationState.value = SurveyOperationState.Loading
        viewModelScope.launch {
            val result = createSurveyUseCase(survey)
            _surveyOperationState.value = result.fold(
                onSuccess = { surveyId ->
                    SurveyOperationState.Success("Survey created successfully")
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to create survey")
                    SurveyOperationState.Error(error.message ?: "Failed to create survey")
                }
            )
        }
    }

    // Update existing survey
    fun updateSurvey(survey: Survey) {
        _surveyOperationState.value = SurveyOperationState.Loading
        viewModelScope.launch {
            val result = updateSurveyUseCase(survey)
            _surveyOperationState.value = result.fold(
                onSuccess = { success ->
                    SurveyOperationState.Success("Survey updated successfully")
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to update survey")
                    SurveyOperationState.Error(error.message ?: "Failed to update survey")
                }
            )
        }
    }

    // Delete survey
    fun deleteSurvey(surveyId: String) {
        _surveyOperationState.value = SurveyOperationState.Loading
        viewModelScope.launch {
            val result = deleteSurveyUseCase(surveyId)
            _surveyOperationState.value = result.fold(
                onSuccess = { success ->
                    SurveyOperationState.Success("Survey deleted successfully")
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to delete survey")
                    SurveyOperationState.Error(error.message ?: "Failed to delete survey")
                }
            )
        }
    }

    // Like survey
    fun likeSurvey(surveyId: String) {
        viewModelScope.launch {
            likeSurveyUseCase(surveyId).fold(
                onSuccess = { success ->
                    // Optional: Update local state if needed
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to like survey")
                }
            )
        }
    }

    // Add a response to the current survey
    fun addResponse(questionId: String, answer: Answer) {
        _userResponses.update { currentResponses ->
            val updatedResponses = currentResponses.toMutableMap()
            updatedResponses[questionId] = answer
            updatedResponses
        }
    }

    // Submit all responses for a survey
    fun submitSurveyResponse(surveyId: String, userId: String) {
        if (_userResponses.value.isEmpty()) {
            _responseState.value = ResponseState.Error("No responses to submit")
            return
        }

        _responseState.value = ResponseState.Loading
        viewModelScope.launch {
            val response = SurveyResponse(
                surveyId = surveyId,
                userId = userId,
                answers = _userResponses.value
            )

            val result = submitSurveyResponseUseCase(response)
            _responseState.value = result.fold(
                onSuccess = { responseId ->
                    // Clear responses after successful submission
                    _userResponses.value = mapOf()
                    ResponseState.Success("Response submitted successfully")
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to submit response")
                    ResponseState.Error(error.message ?: "Failed to submit response")
                }
            )
        }
    }

    // Clear user responses
    fun clearResponses() {
        _userResponses.value = mapOf()
    }

    // Reset operation state
    fun resetOperationState() {
        _surveyOperationState.value = SurveyOperationState.Idle
    }

    // Reset response state
    fun resetResponseState() {
        _responseState.value = ResponseState.Idle
    }

    // Add sample surveys (for testing)
    fun addSampleSurveys() {
        viewModelScope.launch {
            addSampleSurveysUseCase().fold(
                onSuccess = { surveyIds ->
                    Timber.d("Added ${surveyIds.size} sample surveys")
                    getAllSurveys() // Refresh the surveys list
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to add sample surveys")
                }
            )
        }
    }
}

// State class for the list of surveys
sealed class SurveysState {
    data object Loading : SurveysState()
    data object Empty : SurveysState()
    data class Success(val surveys: List<Survey>) : SurveysState()
    data class Error(val message: String) : SurveysState()
}

// State class for a single survey
sealed class SurveyState {
    data object Loading : SurveyState()
    data object NotFound : SurveyState()
    data class Success(val survey: Survey) : SurveyState()
    data class Error(val message: String) : SurveyState()
}

// State for survey creation/modification
sealed class SurveyOperationState {
    data object Idle : SurveyOperationState()
    data object Loading : SurveyOperationState()
    data class Success(val message: String) : SurveyOperationState()
    data class Error(val message: String) : SurveyOperationState()
}

// State for survey response submission
sealed class ResponseState {
    data object Idle : ResponseState()
    data object Loading : ResponseState()
    data class Success(val message: String) : ResponseState()
    data class Error(val message: String) : ResponseState()
} 