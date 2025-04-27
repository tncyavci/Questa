package com.example.questa.data.repository

import com.example.questa.data.model.Comment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CommentRepository {
    private val database = FirebaseDatabase.getInstance().reference

    fun getCommentsForPoll(pollId: String): Flow<List<Comment>> = callbackFlow {
        val commentsRef = database.child("comments").orderByChild("pollId").equalTo(pollId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = snapshot.children.mapNotNull { it.getValue(Comment::class.java) }
                    .sortedByDescending { it.createdAt }
                trySend(comments)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        commentsRef.addValueEventListener(listener)
        awaitClose { commentsRef.removeEventListener(listener) }
    }
    
    suspend fun addComment(comment: Comment): Result<String> {
        return try {
            val commentId = UUID.randomUUID().toString()
            val newComment = comment.copy(id = commentId)
            
            database.child("comments").child(commentId).setValue(newComment).await()
            Result.success(commentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteComment(commentId: String): Result<Unit> {
        return try {
            database.child("comments").child(commentId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 