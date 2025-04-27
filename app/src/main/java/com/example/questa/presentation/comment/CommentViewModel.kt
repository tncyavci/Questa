package com.example.questa.presentation.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.questa.data.model.Comment
import com.example.questa.domain.comment.AddCommentUseCase
import com.example.questa.domain.comment.DeleteCommentUseCase
import com.example.questa.domain.comment.GetCommentsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CommentState {
    object Loading : CommentState()
    data class Success(val comments: List<Comment>) : CommentState()
    data class Error(val message: String) : CommentState()
}

sealed class CommentEvent {
    data class LoadComments(val pollId: String) : CommentEvent()
    data class AddComment(val pollId: String, val text: String) : CommentEvent()
    data class DeleteComment(val commentId: String) : CommentEvent()
}

class CommentViewModel(
    private val getCommentsUseCase: GetCommentsUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val pollId: String
) : ViewModel() {

    private val _commentState = MutableStateFlow<CommentState>(CommentState.Loading)
    val commentState: StateFlow<CommentState> = _commentState.asStateFlow()
    
    private val _addCommentState = MutableStateFlow<AddCommentState>(AddCommentState.Initial)
    val addCommentState: StateFlow<AddCommentState> = _addCommentState.asStateFlow()
    
    private val _currentUserId = MutableStateFlow<String>("")
    private val _currentUsername = MutableStateFlow<String>("")
    
    init {
        loadComments(pollId)
    }
    
    fun setUserInfo(userId: String, username: String) {
        _currentUserId.value = userId
        _currentUsername.value = username
    }
    
    fun onEvent(event: CommentEvent) {
        when (event) {
            is CommentEvent.LoadComments -> loadComments(event.pollId)
            is CommentEvent.AddComment -> addComment(event.pollId, event.text)
            is CommentEvent.DeleteComment -> deleteComment(event.commentId)
        }
    }
    
    private fun loadComments(pollId: String) {
        viewModelScope.launch {
            try {
                _commentState.value = CommentState.Loading
                
                getCommentsUseCase(pollId)
                    .collect { comments ->
                        _commentState.value = CommentState.Success(comments)
                    }
            } catch (e: Exception) {
                _commentState.value = CommentState.Error(e.message ?: "Failed to load comments")
            }
        }
    }
    
    private fun addComment(pollId: String, text: String) {
        viewModelScope.launch {
            _addCommentState.value = AddCommentState.Loading
            
            val comment = Comment(
                pollId = pollId,
                userId = _currentUserId.value,
                username = _currentUsername.value,
                text = text
            )
            
            addCommentUseCase(comment)
                .onSuccess { commentId ->
                    _addCommentState.value = AddCommentState.Success
                }
                .onFailure { exception ->
                    _addCommentState.value = AddCommentState.Error(exception.message ?: "Failed to add comment")
                }
        }
    }
    
    private fun deleteComment(commentId: String) {
        viewModelScope.launch {
            deleteCommentUseCase(commentId)
        }
    }
    
    fun resetAddCommentState() {
        _addCommentState.value = AddCommentState.Initial
    }
}

sealed class AddCommentState {
    object Initial : AddCommentState()
    object Loading : AddCommentState()
    object Success : AddCommentState()
    data class Error(val message: String) : AddCommentState()
} 