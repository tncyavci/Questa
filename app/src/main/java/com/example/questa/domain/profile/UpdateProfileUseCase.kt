package com.example.questa.domain.profile

import com.example.questa.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateProfileUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(userId: String, username: String, bio: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                userRepository.updateUserProfile(userId, username, bio)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 