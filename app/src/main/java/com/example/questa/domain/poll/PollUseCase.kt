package com.example.questa.domain.poll

import com.example.questa.data.model.Poll
import com.example.questa.data.repository.PollRepository
import kotlinx.coroutines.flow.Flow

class GetAllPollsUseCase(private val pollRepository: PollRepository) {
    operator fun invoke(): Flow<List<Poll>> {
        return pollRepository.getAllPolls()
    }
}

class GetPollByIdUseCase(private val pollRepository: PollRepository) {
    operator fun invoke(pollId: String): Flow<Poll?> {
        return pollRepository.getPollById(pollId)
    }
}

class CreatePollUseCase(private val pollRepository: PollRepository) {
    suspend operator fun invoke(poll: Poll): Result<String> {
        return pollRepository.createPoll(poll)
    }
}

class VotePollUseCase(private val pollRepository: PollRepository) {
    suspend operator fun invoke(pollId: String, optionId: String, userId: String): Result<Unit> {
        return pollRepository.votePoll(pollId, optionId, userId)
    }
}

class ToggleFavoriteUseCase(private val pollRepository: PollRepository) {
    suspend operator fun invoke(pollId: String, userId: String): Result<Boolean> {
        return pollRepository.toggleFavorite(pollId, userId)
    }
}

class GetFeaturedPollsUseCase(private val pollRepository: PollRepository) {
    operator fun invoke(): Flow<List<Poll>> {
        return pollRepository.getFeaturedPolls()
    }
}

class GetUserPollsUseCase(private val pollRepository: PollRepository) {
    operator fun invoke(userId: String): Flow<List<Poll>> {
        return pollRepository.getUserPolls(userId)
    }
}

class LikePollUseCase(private val pollRepository: PollRepository) {
    suspend operator fun invoke(pollId: String, userId: String): Result<Unit> {
        return pollRepository.likePoll(pollId, userId)
    }
}

/**
 * Belirli bir kategorideki anketleri getiren kullanım durumu.
 */
class GetCategoryPollsUseCase(private val pollRepository: PollRepository) {
    operator fun invoke(category: String): Flow<List<Poll>> {
        return pollRepository.getPollsByCategory(category)
    }
}

/**
 * Anketleri metin aramasına göre filtreleme kullanım durumu.
 */
class SearchPollsUseCase(private val pollRepository: PollRepository) {
    operator fun invoke(query: String): Flow<List<Poll>> {
        return pollRepository.searchPolls(query)
    }
}

/**
 * Kullanıcının bir ankete oy verip vermediğini kontrol eden use case
 */
class HasUserVotedUseCase(private val pollRepository: PollRepository) {
    suspend operator fun invoke(pollId: String, userId: String): Boolean {
        return pollRepository.hasUserVotedPoll(pollId, userId)
    }
}

/**
 * Kullanıcının favori anketlerinin ID'lerini getiren use case
 */
class GetUserFavoritesUseCase(private val userRepository: com.example.questa.data.repository.UserRepository) {
    suspend operator fun invoke(userId: String): List<String> {
        return userRepository.getUserFavorites(userId)
    }
} 