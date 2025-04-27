package com.example.questa.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.questa.domain.auth.GetCurrentUserUseCase
import com.example.questa.domain.auth.ResetPasswordUseCase
import com.example.questa.domain.auth.SignInUseCase
import com.example.questa.domain.auth.SignOutUseCase
import com.example.questa.domain.auth.SignUpUseCase
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class AuthEvent {
    data class SignIn(val email: String, val password: String) : AuthEvent()
    data class SignUp(val email: String, val password: String, val username: String) : AuthEvent()
    object SignOut : AuthEvent()
    data class ResetPassword(val email: String) : AuthEvent()
}

class AuthViewModel(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Initial)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        val currentUser = getCurrentUserUseCase()
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.SignIn -> signIn(event.email, event.password)
            is AuthEvent.SignUp -> signUp(event.email, event.password, event.username)
            is AuthEvent.SignOut -> signOut()
            is AuthEvent.ResetPassword -> resetPassword(event.email)
        }
    }
    
    private fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            signInUseCase(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Sign in failed")
                }
        }
    }
    
    private fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            signUpUseCase(email, password, username)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Sign up failed")
                }
        }
    }
    
    private fun signOut() {
        signOutUseCase()
        _authState.value = AuthState.Unauthenticated
    }
    
    private fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = ResetPasswordState.Loading
            
            resetPasswordUseCase(email)
                .onSuccess {
                    _resetPasswordState.value = ResetPasswordState.Success
                }
                .onFailure { exception ->
                    _resetPasswordState.value = ResetPasswordState.Error(exception.message ?: "Password reset failed")
                }
        }
    }
    
    fun resetPasswordStateConsumed() {
        _resetPasswordState.value = ResetPasswordState.Initial
    }
}

sealed class ResetPasswordState {
    object Initial : ResetPasswordState()
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
} 