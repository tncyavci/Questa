package com.example.questa.domain.comment

import com.example.questa.data.model.Comment
import com.example.questa.data.repository.CommentRepository
import kotlinx.coroutines.flow.Flow

class GetCommentsForPollUseCase(private val commentRepository: CommentRepository) {
    operator fun invoke(pollId: String): Flow<List<Comment>> {
        return commentRepository.getCommentsForPoll(pollId)
    }
}

class AddCommentUseCase(private val commentRepository: CommentRepository) {
    suspend operator fun invoke(comment: Comment): Result<String> {
        return commentRepository.addComment(comment)
    }
}

class DeleteCommentUseCase(private val commentRepository: CommentRepository) {
    suspend operator fun invoke(commentId: String): Result<Unit> {
        return commentRepository.deleteComment(commentId)
    }
} 