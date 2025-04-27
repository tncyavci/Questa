package com.example.questa.data.repository

import com.example.questa.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, username: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user!!
            
            // Create user profile in the database
            val userProfile = User(
                uid = user.uid,
                email = email,
                username = username
            )
            
            database.child("users").child(user.uid).setValue(userProfile).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 