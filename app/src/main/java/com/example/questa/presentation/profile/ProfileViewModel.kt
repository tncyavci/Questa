package com.example.questa.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.questa.domain.profile.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Initial : ProfileState()
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class ProfileEvent {
    data class UpdateProfile(
        val userId: String,
        val username: String,
        val bio: String = ""
    ) : ProfileEvent()
}

class ProfileViewModel(
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {
    
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()
    
    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.UpdateProfile -> updateProfile(event.userId, event.username, event.bio)
        }
    }
    
    private fun updateProfile(userId: String, username: String, bio: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            
            updateProfileUseCase(userId, username, bio)
                .onSuccess {
                    _profileState.value = ProfileState.Success
                }
                .onFailure { exception ->
                    _profileState.value = ProfileState.Error(exception.message ?: "Failed to update profile")
                }
        }
    }
} 