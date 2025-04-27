package com.example.questa.domain.comment

import com.example.questa.data.model.Comment
import com.example.questa.data.repository.CommentRepository
import kotlinx.coroutines.flow.Flow

class GetCommentsUseCase(private val commentRepository: CommentRepository) {
    operator fun invoke(pollId: String): Flow<List<Comment>> {
        return commentRepository.getCommentsForPoll(pollId)
    }
} 