package com.example.questa.data.repository

import com.example.questa.data.model.DemographicData
import com.example.questa.data.model.OptionStat
import com.example.questa.data.model.Poll
import com.example.questa.data.model.PollOption
import com.example.questa.data.model.PollStats
import com.example.questa.util.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID

class PollRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val pollsRef = database.child(Constants.POLLS_REF)
    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getAllPolls(): Flow<List<Poll>> = callbackFlow {
        val pollsRef = database.child("polls")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val polls = snapshot.children.mapNotNull { it.getValue(Poll::class.java) }
                trySend(polls)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        pollsRef.addValueEventListener(listener)
        awaitClose { pollsRef.removeEventListener(listener) }
    }
    
    fun getPollById(pollId: String): Flow<Poll?> = callbackFlow {
        val pollRef = database.child("polls").child(pollId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val poll = snapshot.getValue(Poll::class.java)
                trySend(poll)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        pollRef.addValueEventListener(listener)
        awaitClose { pollRef.removeEventListener(listener) }
    }
    
    suspend fun createPoll(poll: Poll): Result<String> {
        return try {
            val pollId = poll.id ?: pollsRef.push().key ?: UUID.randomUUID().toString()
            val pollWithId = poll.copy(id = pollId)
            pollsRef.child(pollId).setValue(pollWithId).await()
            Result.success(pollId)
        } catch (e: Exception) {
            Timber.e(e, "Error creating poll")
            Result.failure(e)
        }
    }
    
    suspend fun votePoll(pollId: String, optionId: String, userId: String): Result<Unit> {
        return try {
            val pollRef = database.child("polls").child(pollId)
            val pollSnapshot = pollRef.get().await()
            val poll = pollSnapshot.getValue(Poll::class.java) ?: throw Exception("Poll not found")
            
            // Hangi sorunun hangi seçeneğini oyladığını bul
            var foundQuestionIndex = -1
            var foundOptionIndex = -1
            
            for (i in poll.questions.indices) {
                val options = poll.questions[i].options
                for (j in options.indices) {
                    if (options[j].id == optionId) {
                        foundQuestionIndex = i
                        foundOptionIndex = j
                        break
                    }
                }
                if (foundQuestionIndex != -1) break
            }
            
            if (foundQuestionIndex == -1 || foundOptionIndex == -1) {
                // Eski yöntemle deneme - geriye dönük uyumluluk için
                val updatedOptions = poll.options.map { option ->
                    if (option.id == optionId) {
                        option.copy(votes = option.votes + 1)
                    } else {
                        option
                    }
                }
                
                // Calculate percentages
                val totalVotes = updatedOptions.sumOf { it.votes }
                val optionsWithPercentages = updatedOptions.map { option ->
                    val percentage = if (totalVotes > 0) (option.votes.toFloat() / totalVotes) * 100 else 0f
                    option.copy(percentage = percentage)
                }
                
                // Update poll
                pollRef.child("options").setValue(optionsWithPercentages).await()
                pollRef.child("totalVotes").setValue(totalVotes).await()
            } else {
                // Çoklu soru formatı için güncelleme
                // Seçilen seçeneğin oy sayısını artır
                val updatedQuestions = poll.questions.toMutableList()
                val question = updatedQuestions[foundQuestionIndex]
                val updatedOptions = question.options.toMutableList()
                val option = updatedOptions[foundOptionIndex]
                updatedOptions[foundOptionIndex] = option.copy(votes = option.votes + 1)
                
                // Yüzdeleri güncelle
                val totalVotes = updatedOptions.sumOf { it.votes }
                val optionsWithPercentages = updatedOptions.map { opt ->
                    val percentage = if (totalVotes > 0) (opt.votes.toFloat() / totalVotes) * 100 else 0f
                    opt.copy(percentage = percentage)
                }
                
                // Güncellenmiş soruyu kaydet
                updatedQuestions[foundQuestionIndex] = question.copy(options = optionsWithPercentages)
                
                // Anketi güncelle
                pollRef.child("questions").setValue(updatedQuestions).await()
                pollRef.child("totalVotes").setValue(poll.totalVotes + 1).await()
            }
            
            // Update user's voted polls
            database.child("users").child(userId).child("votedPolls")
                .get().await().getValue(object : com.google.firebase.database.GenericTypeIndicator<List<String>>() {})
                ?.let { currentPolls ->
                    if (!currentPolls.contains(pollId)) {
                        val updatedPolls = currentPolls.toMutableList().apply { add(pollId) }
                        database.child("users").child(userId).child("votedPolls")
                            .setValue(updatedPolls).await()
                    }
                } ?: database.child("users").child(userId).child("votedPolls")
                    .setValue(listOf(pollId)).await()
                    
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleFavorite(pollId: String, userId: String): Result<Boolean> {
        return try {
            val userRef = database.child("users").child(userId)
            val favoritesSnapshot = userRef.child("favoritePolls").get().await()
            
            val currentFavorites = favoritesSnapshot.getValue(object : com.google.firebase.database.GenericTypeIndicator<List<String>>() {}) ?: emptyList()
            
            val isFavorite = currentFavorites.contains(pollId)
            val updatedFavorites = if (isFavorite) {
                currentFavorites.filter { it != pollId }
            } else {
                currentFavorites.toMutableList().apply { add(pollId) }
            }
            
            userRef.child("favoritePolls").setValue(updatedFavorites).await()
            
            Result.success(!isFavorite) // Return new favorite state
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getFeaturedPolls(): Flow<List<Poll>> = callbackFlow {
        val pollsRef = database.child("polls").orderByChild("isFeatured").equalTo(true)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val polls = snapshot.children.mapNotNull { it.getValue(Poll::class.java) }
                trySend(polls)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        pollsRef.addValueEventListener(listener)
        awaitClose { pollsRef.removeEventListener(listener) }
    }
    
    fun getUserPolls(userId: String): Flow<List<Poll>> = callbackFlow {
        val pollsRef = database.child("polls").orderByChild("creatorId").equalTo(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val polls = snapshot.children.mapNotNull { it.getValue(Poll::class.java) }
                trySend(polls)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        pollsRef.addValueEventListener(listener)
        awaitClose { pollsRef.removeEventListener(listener) }
    }
    
    suspend fun likePoll(pollId: String, userId: String): Result<Unit> {
        return try {
            val pollRef = database.child("polls").child(pollId)
            val pollSnapshot = pollRef.get().await()
            val poll = pollSnapshot.getValue(Poll::class.java) ?: throw Exception("Poll not found")
            
            // Get current likes
            val currentLikes = poll.likes ?: listOf()
            
            // Toggle like: add userId if not present, remove if present
            val updatedLikes = if (currentLikes.contains(userId)) {
                currentLikes.filter { it != userId }
            } else {
                currentLikes + userId
            }
            
            // Update poll with new likes
            pollRef.child("likes").setValue(updatedLikes).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPollStats(pollId: String): Flow<PollStats?> = callbackFlow {
        val pollRef = database.child("polls").child(pollId)
        val statsRef = database.child("pollStats").child(pollId)
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                repoScope.launch {
                    try {
                        // Temel anket bilgilerini al
                        val pollSnapshot = pollRef.get().await()
                        val poll = pollSnapshot.getValue(Poll::class.java)
                        
                        if (poll == null) {
                            trySend(null)
                            return@launch
                        }
                        
                        // İstatistik verilerini al veya oluştur
                        val statsSnapshot = snapshot
                        val demographicData = statsSnapshot.child("demographics")
                            .getValue(DemographicData::class.java) ?: DemographicData()
                        
                        // Seçenek istatistiklerini oluştur
                        val optionStats = poll.options.map { option ->
                            OptionStat(
                                optionId = option.id,
                                optionText = option.text,
                                votes = option.votes,
                                percentage = option.percentage
                            )
                        }
                        
                        // Zamana göre katılım verilerini al
                        val participationSnapshot = statsSnapshot.child("participationOverTime")
                        val participationMap = mutableMapOf<String, Int>()
                        
                        participationSnapshot.children.forEach { dateSnapshot ->
                            participationMap[dateSnapshot.key ?: ""] = 
                                (dateSnapshot.getValue(Int::class.java) ?: 0)
                        }
                        
                        // PollStats nesnesini oluştur
                        val pollStats = PollStats(
                            pollId = pollId,
                            totalVotes = poll.totalVotes,
                            optionStats = optionStats,
                            demographicData = demographicData,
                            participationOverTime = participationMap
                        )
                        
                        trySend(pollStats)
                    } catch (e: Exception) {
                        Timber.e(e, "Anket istatistikleri alınırken hata oluştu")
                        trySend(null)
                    }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "Anket istatistikleri sorgusu iptal edildi")
                close(error.toException())
            }
        }
        
        // İstatistikleri dinle
        statsRef.addValueEventListener(listener)
        
        awaitClose {
            statsRef.removeEventListener(listener)
        }
    }

    fun getPollsByCategory(category: String): Flow<List<Poll>> = callbackFlow {
        val categoryRef = database.child("polls").orderByChild("category").equalTo(category)
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val polls = snapshot.children.mapNotNull { it.getValue(Poll::class.java) }
                trySend(polls)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        categoryRef.addValueEventListener(listener)
        awaitClose { categoryRef.removeEventListener(listener) }
    }
    
    fun searchPolls(query: String): Flow<List<Poll>> = callbackFlow {
        val pollsRef = database.child("polls")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allPolls = snapshot.children.mapNotNull { it.getValue(Poll::class.java) }
                
                // Metin içinde arama yap (question veya options.text alanlarında)
                val searchQuery = query.lowercase()
                val filteredPolls = allPolls.filter { poll ->
                    // Ana soru metninde ara
                    if (poll.question.lowercase().contains(searchQuery)) {
                        return@filter true
                    }
                    
                    // Seçeneklerde ara
                    if (poll.options.any { it.text.lowercase().contains(searchQuery) }) {
                        return@filter true
                    }
                    
                    // Tüm sorularda ara (birden fazla soru olabilir)
                    poll.questions.any { question ->
                        question.question.lowercase().contains(searchQuery) ||
                        question.options.any { it.text.lowercase().contains(searchQuery) }
                    }
                }
                
                trySend(filteredPolls)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        pollsRef.addValueEventListener(listener)
        awaitClose { pollsRef.removeEventListener(listener) }
    }

    // Kullanıcının belirli bir ankete daha önce oy verip vermediğini kontrol eder
    suspend fun hasUserVotedPoll(pollId: String, userId: String): Boolean {
        return try {
            val userRef = database.child("users").child(userId)
            val votedPollsSnapshot = userRef.child("votedPolls").get().await()
            
            val votedPolls = votedPollsSnapshot.getValue(object : com.google.firebase.database.GenericTypeIndicator<List<String>>() {}) ?: emptyList()
            
            votedPolls.contains(pollId)
        } catch (e: Exception) {
            Timber.e(e, "Error checking if user voted poll")
            false
        }
    }
    
    // Kullanıcının oyladığı tüm anketleri getirir
    suspend fun getUserVotedPolls(userId: String): List<String> {
        return try {
            val userRef = database.child("users").child(userId)
            val votedPollsSnapshot = userRef.child("votedPolls").get().await()
            
            votedPollsSnapshot.getValue(object : com.google.firebase.database.GenericTypeIndicator<List<String>>() {}) ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Error getting user voted polls")
            emptyList()
        }
    }
} 