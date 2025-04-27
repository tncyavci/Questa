package com.example.questa.domain.auth

import com.example.questa.data.model.User
import com.example.questa.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SignInUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<FirebaseUser> {
        return authRepository.signIn(email, password)
    }
}

class SignUpUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, username: String): Result<FirebaseUser> {
        return authRepository.signUp(email, password, username)
    }
}

class SignOutUseCase(private val authRepository: AuthRepository) {
    operator fun invoke() {
        authRepository.signOut()
    }
}

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }
}

class ResetPasswordUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return authRepository.resetPassword(email)
    }
}

class GetAuthUserIdUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): String {
        return authRepository.getCurrentUser()?.uid ?: ""
    }
} 