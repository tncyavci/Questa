package com.example.questa.presentation.poll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.questa.data.model.Poll
import com.example.questa.data.model.PollOption
import com.example.questa.data.model.QuestionItem
import com.example.questa.domain.auth.GetAuthUserIdUseCase
import com.example.questa.domain.poll.CreatePollUseCase
import com.example.questa.domain.poll.GetAllPollsUseCase
import com.example.questa.domain.poll.GetFeaturedPollsUseCase
import com.example.questa.domain.poll.GetPollByIdUseCase
import com.example.questa.domain.poll.GetUserPollsUseCase
import com.example.questa.domain.poll.LikePollUseCase
import com.example.questa.domain.poll.ToggleFavoriteUseCase
import com.example.questa.domain.poll.VotePollUseCase
import com.example.questa.domain.poll.HasUserVotedUseCase
import com.example.questa.domain.poll.GetUserFavoritesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class PollState {
    data object Loading : PollState()
    data class Success(val polls: List<Poll>) : PollState()
    data class Error(val message: String) : PollState()
}

sealed class PollDetailState {
    data object Loading : PollDetailState()
    data class Success(val poll: Poll) : PollDetailState()
    data class Error(val message: String) : PollDetailState()
}

sealed class PollEvent {
    data class LoadPoll(val pollId: String) : PollEvent()
    data class CreatePoll(
        val questions: List<QuestionItem>,
        val category: String
    ) : PollEvent()
    data class VotePoll(val pollId: String, val optionId: String) : PollEvent()
    data class ToggleFavorite(val pollId: String) : PollEvent()
    data object LoadAllPolls : PollEvent()
    data object LoadFeaturedPolls : PollEvent()
    data class LoadUserPolls(val userId: String) : PollEvent()
    data class GetPolls(val questions: List<QuestionItem>) : PollEvent()
    data class GetPollById(val id: String) : PollEvent()
    data class LikePoll(val pollId: String) : PollEvent()
    data class CheckUserVoted(val pollId: String, val userId: String) : PollEvent()
    data object LoadUserFavorites : PollEvent()
}

class PollViewModel(
    private val getAllPollsUseCase: GetAllPollsUseCase,
    private val getPollByIdUseCase: GetPollByIdUseCase,
    private val createPollUseCase: CreatePollUseCase,
    private val votePollUseCase: VotePollUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getFeaturedPollsUseCase: GetFeaturedPollsUseCase,
    private val getUserPollsUseCase: GetUserPollsUseCase,
    private val currentUserId: String,
    private val getAuthUserIdUseCase: GetAuthUserIdUseCase,
    private val likePollUseCase: LikePollUseCase,
    private val hasUserVotedUseCase: HasUserVotedUseCase,
    private val getUserFavoritesUseCase: GetUserFavoritesUseCase
) : ViewModel() {

    private val _pollsState = MutableStateFlow<PollState>(PollState.Loading)
    val pollsState: StateFlow<PollState> = _pollsState.asStateFlow()
    
    private val _currentPollId = MutableStateFlow<String?>(null)
    
    private val _userFavorites = MutableStateFlow<Set<String>>(emptySet())
    val userFavorites: StateFlow<Set<String>> = _userFavorites.asStateFlow()
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val pollDetailState: StateFlow<PollDetailState> = _currentPollId
        .flatMapLatest { pollId ->
            if (pollId != null) {
                getPollByIdUseCase(pollId).flatMapLatest { poll ->
                    if (poll != null) {
                        MutableStateFlow(PollDetailState.Success(poll))
                    } else {
                        MutableStateFlow(PollDetailState.Error("Poll not found"))
                    }
                }
            } else {
                MutableStateFlow(PollDetailState.Loading)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PollDetailState.Loading
        )
    
    private val _createPollState = MutableStateFlow<CreatePollState>(CreatePollState.Initial)
    val createPollState: StateFlow<CreatePollState> = _createPollState.asStateFlow()
    
    private val _voteState = MutableStateFlow<VoteState>(VoteState.Initial)
    val voteState: StateFlow<VoteState> = _voteState.asStateFlow()
    
    init {
        loadAllPolls()
        if (currentUserId.isNotEmpty()) {
            loadUserFavorites()
        }
    }
    
    fun onEvent(event: PollEvent) {
        when (event) {
            is PollEvent.LoadPoll -> loadPoll(event.pollId)
            is PollEvent.CreatePoll -> createPoll(event.questions, event.category)
            is PollEvent.VotePoll -> votePoll(event.pollId, event.optionId)
            is PollEvent.ToggleFavorite -> toggleFavorite(event.pollId)
            is PollEvent.LoadAllPolls -> loadAllPolls()
            is PollEvent.LoadFeaturedPolls -> loadFeaturedPolls()
            is PollEvent.LoadUserPolls -> loadUserPolls(event.userId)
            is PollEvent.GetPolls -> getPolls(event.questions)
            is PollEvent.GetPollById -> getPollById(event.id)
            is PollEvent.LikePoll -> likePoll(event.pollId)
            is PollEvent.CheckUserVoted -> checkUserVoted(event.pollId, event.userId)
            is PollEvent.LoadUserFavorites -> loadUserFavorites()
        }
    }
    
    private fun loadPoll(pollId: String) {
        _currentPollId.value = pollId
    }
    
    private fun createPoll(questions: List<QuestionItem>, category: String) {
        viewModelScope.launch {
            _createPollState.value = CreatePollState.Loading
            val userId = getAuthUserIdUseCase()
            if (userId.isBlank()) {
                _createPollState.value = CreatePollState.Error("User not authenticated")
                return@launch
            }

            try {
                val poll = Poll(
                    userId = userId,
                    questions = questions,
                    category = category,
                    likes = emptyList(),
                    createdAt = System.currentTimeMillis()
                )
                
                val result = createPollUseCase(poll)
                result.onSuccess { pollId ->
                    _createPollState.value = CreatePollState.Success(pollId)
                }.onFailure { throwable ->
                    _createPollState.value = CreatePollState.Error(throwable.message ?: "Failed to create poll")
                }
            } catch (e: Exception) {
                _createPollState.value = CreatePollState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
    
    private fun votePoll(pollId: String, optionId: String) {
        viewModelScope.launch {
            _voteState.value = VoteState.Loading
            
            votePollUseCase(pollId, optionId, currentUserId)
                .onSuccess {
                    _voteState.value = VoteState.Success
                }
                .onFailure { exception ->
                    _voteState.value = VoteState.Error(exception.message ?: "Failed to vote")
                }
        }
    }
    
    private fun toggleFavorite(pollId: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(pollId, currentUserId).onSuccess { isNowFavorite ->
                // Handle UI update optimistically
                if (isNowFavorite) {
                    _userFavorites.value = _userFavorites.value + pollId
                } else {
                    _userFavorites.value = _userFavorites.value - pollId
                }
            }
        }
    }
    
    private fun loadAllPolls() {
        viewModelScope.launch {
            try {
                _pollsState.value = PollState.Loading
                
                getAllPollsUseCase()
                    .collect { polls ->
                        _pollsState.value = PollState.Success(polls)
                    }
            } catch (e: Exception) {
                _pollsState.value = PollState.Error(e.message ?: "Failed to load polls")
            }
        }
    }
    
    private fun loadFeaturedPolls() {
        viewModelScope.launch {
            try {
                _pollsState.value = PollState.Loading
                
                getFeaturedPollsUseCase()
                    .collect { polls ->
                        _pollsState.value = PollState.Success(polls)
                    }
            } catch (e: Exception) {
                _pollsState.value = PollState.Error(e.message ?: "Failed to load featured polls")
            }
        }
    }
    
    private fun loadUserPolls(userId: String) {
        viewModelScope.launch {
            try {
                _pollsState.value = PollState.Loading
                
                getUserPollsUseCase(userId)
                    .collect { polls ->
                        _pollsState.value = PollState.Success(polls)
                    }
            } catch (e: Exception) {
                _pollsState.value = PollState.Error(e.message ?: "Failed to load user polls")
            }
        }
    }
    
    private fun getPolls(questions: List<QuestionItem>) {
        // Implementation of getPolls method
    }
    
    private fun getPollById(id: String) {
        // Implementation of getPollById method
    }
    
    private fun likePoll(pollId: String) {
        viewModelScope.launch {
            try {
                likePollUseCase(pollId, currentUserId)
                // Refresh the poll details to show updated like status
                loadPoll(pollId)
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }
    
    private fun checkUserVoted(pollId: String, userId: String) {
        viewModelScope.launch {
            try {
                val hasVoted = hasUserVotedUseCase(pollId, userId)
                
                if (hasVoted) {
                    _voteState.value = VoteState.AlreadyVoted
                }
            } catch (e: Exception) {
                // Hata oluşursa loglayıp devam edebiliriz
                // Burada bir şey yapmaya gerek yok, oy verilmemiş gibi davranacak
            }
        }
    }
    
    private fun loadUserFavorites() {
        viewModelScope.launch {
            try {
                val favorites = getUserFavoritesUseCase(currentUserId)
                _userFavorites.value = favorites.toSet()
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }
    
    fun resetCreatePollState() {
        _createPollState.value = CreatePollState.Initial
    }
    
    fun resetVoteState() {
        _voteState.value = VoteState.Initial
    }
}

sealed class CreatePollState {
    data object Initial : CreatePollState()
    data object Loading : CreatePollState()
    data class Success(val pollId: String) : CreatePollState()
    data class Error(val message: String) : CreatePollState()
}

sealed class VoteState {
    data object Initial : VoteState()
    data object Loading : VoteState()
    data object Success : VoteState()
    data class Error(val message: String) : VoteState()
    data object AlreadyVoted : VoteState()
} 