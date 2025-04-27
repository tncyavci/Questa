package com.example.questa.data.repository

import com.example.questa.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    suspend fun getUserProfile(userId: String): User? {
        return try {
            val snapshot = database.child("users").child(userId).get().await()
            snapshot.getValue(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun updateUserProfile(userId: String, username: String, bio: String): Boolean {
        return try {
            // Update display name in Firebase Auth
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                currentUser.updateProfile(profileUpdates).await()
            }
            
            // Update user profile in Firebase Database
            val userUpdates = mapOf(
                "username" to username,
                "bio" to bio
            )
            
            database.child("users").child(userId).updateChildren(userUpdates).await()
            true
        } catch (e: Exception) {
            throw e
        }
    }
    
    suspend fun getUserFavorites(userId: String): List<String> {
        return try {
            val favoritesSnapshot = database.child("users").child(userId).child("favoritePolls").get().await()
            favoritesSnapshot.getValue(object : com.google.firebase.database.GenericTypeIndicator<List<String>>() {}) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
} 