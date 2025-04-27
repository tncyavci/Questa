package com.example.questa.data.repository

import com.example.questa.data.model.Answer
import com.example.questa.data.model.Option
import com.example.questa.data.model.Question
import com.example.questa.data.model.QuestionType
import com.example.questa.data.model.Survey
import com.example.questa.data.model.SurveyResponse
import com.example.questa.util.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID

class SurveyRepository {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val surveysRef: DatabaseReference = database.getReference(Constants.SURVEYS_REF)
    private val responsesRef: DatabaseReference = database.getReference(Constants.SURVEY_RESPONSES_REF)

    // Anket oluştur
    suspend fun createSurvey(survey: Survey): Result<String> {
        return try {
            val surveyId = survey.id.ifEmpty { UUID.randomUUID().toString() }
            val newSurvey = survey.copy(id = surveyId)
            
            surveysRef.child(surveyId).setValue(newSurvey).await()
            Result.success(surveyId)
        } catch (e: Exception) {
            Timber.e(e, "Anket oluşturulurken hata")
            Result.failure(e)
        }
    }

    // Tüm anketleri getir
    fun getAllSurveys(): Flow<Result<List<Survey>>> = callbackFlow {
        val surveysListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val surveys = mutableListOf<Survey>()
                
                for (surveySnapshot in snapshot.children) {
                    try {
                        val survey = surveySnapshot.getValue(Survey::class.java)
                        survey?.let { surveys.add(it) }
                    } catch (e: Exception) {
                        Timber.e(e, "Anket verisini çevirirken hata")
                    }
                }
                
                trySend(Result.success(surveys))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        
        surveysRef.addValueEventListener(surveysListener)
        
        awaitClose {
            surveysRef.removeEventListener(surveysListener)
        }
    }

    // Belirli bir anketi ID'ye göre getir
    fun getSurveyById(surveyId: String): Flow<Result<Survey?>> = callbackFlow {
        val surveyListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val survey = snapshot.getValue(Survey::class.java)
                    trySend(Result.success(survey))
                } catch (e: Exception) {
                    Timber.e(e, "Anket verisi çevirirken hata")
                    trySend(Result.failure(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        
        surveysRef.child(surveyId).addValueEventListener(surveyListener)
        
        awaitClose {
            surveysRef.child(surveyId).removeEventListener(surveyListener)
        }
    }

    // Kullanıcının oluşturduğu anketleri getir
    fun getSurveysByUser(userId: String): Flow<Result<List<Survey>>> = callbackFlow {
        val userSurveysListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val surveys = mutableListOf<Survey>()
                
                for (surveySnapshot in snapshot.children) {
                    try {
                        val survey = surveySnapshot.getValue(Survey::class.java)
                        if (survey != null && survey.createdBy == userId) {
                            surveys.add(survey)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Anket verisini çevirirken hata")
                    }
                }
                
                trySend(Result.success(surveys))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        
        surveysRef.orderByChild("createdBy").equalTo(userId).addValueEventListener(userSurveysListener)
        
        awaitClose {
            surveysRef.orderByChild("createdBy").equalTo(userId).removeEventListener(userSurveysListener)
        }
    }

    // Ankete cevap gönder
    suspend fun submitSurveyResponse(response: SurveyResponse): Result<String> {
        return try {
            val responseId = response.id.ifEmpty { UUID.randomUUID().toString() }
            val newResponse = response.copy(id = responseId)
            
            // Yanıtı kaydet
            responsesRef.child(responseId).setValue(newResponse).await()
            
            // Anketin toplam yanıt sayısını güncelle
            val survey = surveysRef.child(response.surveyId).get().await().getValue(Survey::class.java)
            
            survey?.let {
                val updatedSurvey = it.copy(totalResponses = it.totalResponses + 1)
                surveysRef.child(response.surveyId).setValue(updatedSurvey).await()
                
                // Her bir cevapta seçilen seçeneğin oy sayısını artır
                response.answers.forEach { (questionId, answer) ->
                    answer.selectedOptionIds.forEach { optionId ->
                        val optionRef = surveysRef.child("${response.surveyId}/questions/$questionId/options/$optionId")
                        val option = optionRef.get().await().getValue(Option::class.java)
                        
                        option?.let { opt ->
                            val updatedOption = opt.copy(votes = opt.votes + 1)
                            optionRef.setValue(updatedOption).await()
                        }
                    }
                }
            }
            
            Result.success(responseId)
        } catch (e: Exception) {
            Timber.e(e, "Anket yanıtı gönderilirken hata")
            Result.failure(e)
        }
    }

    // Anketi beğen
    suspend fun likeSurvey(surveyId: String): Result<Boolean> {
        return try {
            val survey = surveysRef.child(surveyId).get().await().getValue(Survey::class.java)
            
            survey?.let {
                val updatedSurvey = it.copy(likes = it.likes + 1)
                surveysRef.child(surveyId).setValue(updatedSurvey).await()
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Timber.e(e, "Anket beğenilirken hata")
            Result.failure(e)
        }
    }

    // Anketi güncelle
    suspend fun updateSurvey(survey: Survey): Result<Boolean> {
        return try {
            surveysRef.child(survey.id).setValue(survey).await()
            Result.success(true)
        } catch (e: Exception) {
            Timber.e(e, "Anket güncellenirken hata")
            Result.failure(e)
        }
    }

    // Anketi sil
    suspend fun deleteSurvey(surveyId: String): Result<Boolean> {
        return try {
            surveysRef.child(surveyId).removeValue().await()
            
            // İlgili yanıtları da sil
            val responses = responsesRef.orderByChild("surveyId").equalTo(surveyId).get().await()
            
            for (responseSnapshot in responses.children) {
                responseSnapshot.ref.removeValue().await()
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Timber.e(e, "Anket silinirken hata")
            Result.failure(e)
        }
    }

    // Örnek anketler ekle (test için)
    suspend fun addSampleSurveys(): Result<List<String>> {
        return try {
            val surveyIds = mutableListOf<String>()
            
            // Örnek Anket 1: Teknoloji Kullanımı
            val techSurvey = createTechnologySurveySample()
            val techSurveyId = createSurvey(techSurvey).getOrThrow()
            surveyIds.add(techSurveyId)
            
            // Örnek Anket 2: Seyahat Tercihleri
            val travelSurvey = createTravelSurveySample()
            val travelSurveyId = createSurvey(travelSurvey).getOrThrow()
            surveyIds.add(travelSurveyId)
            
            // Örnek Anket 3: Eğitim Memnuniyeti
            val educationSurvey = createEducationSurveySample()
            val educationSurveyId = createSurvey(educationSurvey).getOrThrow()
            surveyIds.add(educationSurveyId)
            
            Result.success(surveyIds)
        } catch (e: Exception) {
            Timber.e(e, "Örnek anketler eklenirken hata")
            Result.failure(e)
        }
    }

    // Örnek teknoloji anketi oluştur
    private fun createTechnologySurveySample(): Survey {
        val questions = mutableMapOf<String, Question>()
        
        // Soru 1
        val q1Options = mapOf(
            UUID.randomUUID().toString() to Option(text = "0-2 saat"),
            UUID.randomUUID().toString() to Option(text = "3-5 saat"),
            UUID.randomUUID().toString() to Option(text = "6-8 saat"),
            UUID.randomUUID().toString() to Option(text = "8 saatten fazla")
        )
        
        val question1 = Question(
            text = "Günde kaç saat akıllı telefon kullanıyorsunuz?",
            type = QuestionType.MULTIPLE_CHOICE,
            options = q1Options,
            order = 1
        )
        questions[question1.id] = question1
        
        // Soru 2
        val q2Options = mapOf(
            UUID.randomUUID().toString() to Option(text = "Instagram"),
            UUID.randomUUID().toString() to Option(text = "Twitter/X"),
            UUID.randomUUID().toString() to Option(text = "Facebook"),
            UUID.randomUUID().toString() to Option(text = "TikTok"),
            UUID.randomUUID().toString() to Option(text = "LinkedIn"),
            UUID.randomUUID().toString() to Option(text = "Diğer")
        )
        
        val question2 = Question(
            text = "En çok hangi sosyal medya platformunu kullanıyorsunuz?",
            type = QuestionType.MULTIPLE_CHOICE,
            options = q2Options,
            order = 2
        )
        questions[question2.id] = question2
        
        // Soru 3
        val q3Options = mapOf(
            UUID.randomUUID().toString() to Option(text = "Evet"),
            UUID.randomUUID().toString() to Option(text = "Hayır"),
            UUID.randomUUID().toString() to Option(text = "Kararsızım")
        )
        
        val question3 = Question(
            text = "Teknoloji bağımlılığı sizce önemli bir sorun mu?",
            type = QuestionType.MULTIPLE_CHOICE,
            options = q3Options,
            order = 3
        )
        questions[question3.id] = question3
        
        // Soru 4
        val question4 = Question(
            text = "Dijital detoks yapmak için neler yapıyorsunuz?",
            type = QuestionType.TEXT,
            options = mapOf(),
            order = 4,
            required = false
        )
        questions[question4.id] = question4
        
        return Survey(
            title = "Teknoloji Kullanım Anketi",
            description = "Bu anket, günlük teknoloji kullanım alışkanlıklarını anlamak için hazırlanmıştır.",
            createdBy = "admin",
            category = "Teknoloji",
            questions = questions
        )
    }

    // Örnek seyahat anketi oluştur
    private fun createTravelSurveySample(): Survey {
        val questions = mutableMapOf<String, Question>()
        
        // Soru 1
        val q1Options = mapOf(
            UUID.randomUUID().toString() to Option(text = "Yılda 1-2 kez"),
            UUID.randomUUID().toString() to Option(text = "Yılda 3-5 kez"),
            UUID.randomUUID().toString() to Option(text = "Yılda 5 defadan fazla"),
            UUID.randomUUID().toString() to Option(text = "Hiç seyahat etmiyorum")
        )
        
        val question1 = Question(
            text = "Ne sıklıkla seyahat ediyorsunuz?",
            type = QuestionType.MULTIPLE_CHOICE,
            options = q1Options,
            order = 1
        )
        questions[question1.id] = question1
        
        // Soru 2
        val q2Options = mapOf(
            UUID.randomUUID().toString() to Option(text = "Deniz tatili"),
            UUID.randomUUID().toString() to Option(text = "Kültür turu"),
            UUID.randomUUID().toString() to Option(text = "Doğa/kamp tatili"),
            UUID.randomUUID().toString() to Option(text = "Kayak tatili"),
            UUID.randomUUID().toString() to Option(text = "Şehir turu")
        )
        
        val question2 = Question(
            text = "En çok tercih ettiğiniz tatil türü nedir?",
            type = QuestionType.CHECKBOX,
            options = q2Options,
            order = 2
        )
        questions[question2.id] = question2
        
        // Soru 3
        val question3 = Question(
            text = "Şimdiye kadar gittiğiniz en etkileyici yer neresi?",
            type = QuestionType.TEXT,
            options = mapOf(),
            order = 3
        )
        questions[question3.id] = question3
        
        // Soru 4
        val q4Options = mapOf(
            UUID.randomUUID().toString() to Option(text = "1 (Hiç memnun değilim)"),
            UUID.randomUUID().toString() to Option(text = "2"),
            UUID.randomUUID().toString() to Option(text = "3"),
            UUID.randomUUID().toString() to Option(text = "4"),
            UUID.randomUUID().toString() to Option(text = "5 (Çok memnunum)")
        )
        
        val question4 = Question(
            text = "Son tatilinizden ne kadar memnun kaldınız?",
            type = QuestionType.RATING,
            options = q4Options,
            order = 4
        )
        questions[question4.id] = question4
        
        return Survey(
            title = "Seyahat Alışkanlıkları Anketi",
            description = "Bu anket, seyahat tercihlerinizi ve deneyimlerinizi anlamak için hazırlanmıştır.",
            createdBy = "admin",
            category = "Seyahat",
            questions = questions
        )
    }

    // Örnek eğitim anketi oluştur
    private fun createEducationSurveySample(): Survey {
        val questions = mutableMapOf<String, Question>()
        
        // Soru 1
        val q1Options = mapOf(
            UUID.randomUUID().toString() to Option(text = "Lise"),
            UUID.randomUUID().toString() to Option(text = "Üniversite"),
            UUID.randomUUID().toString() to Option(text = "Yüksek Lisans"),
            UUID.randomUUID().toString() to Option(text = "Doktora"),
            UUID.randomUUID().toString() to Option(text = "Diğer")
        )
        
        val question1 = Question(
            text = "En son tamamladığınız eğitim seviyesi nedir?",
            type = QuestionType.MULTIPLE_CHOICE,
            options = q1Options,
            order = 1
        )
        questions[question1.id] = question1
        
        // Soru 2
        val q2Options = mapOf(
            UUID.randomUUID().toString() to Option(text = "Evet"),
            UUID.randomUUID().toString() to Option(text = "Hayır")
        )
        
        val question2 = Question(
            text = "Eğitim hayatınızda uzaktan eğitim aldınız mı?",
            type = QuestionType.YES_NO,
            options = q2Options,
            order = 2
        )
        questions[question2.id] = question2
        
        // Soru 3
        val q3Options = mapOf(
            UUID.randomUUID().toString() to Option(text = "Yüz yüze eğitim"),
            UUID.randomUUID().toString() to Option(text = "Uzaktan eğitim"),
            UUID.randomUUID().toString() to Option(text = "Karma (hibrit) eğitim")
        )
        
        val question3 = Question(
            text = "Sizce en etkili eğitim modeli hangisidir?",
            type = QuestionType.MULTIPLE_CHOICE,
            options = q3Options,
            order = 3
        )
        questions[question3.id] = question3
        
        // Soru 4
        val question4 = Question(
            text = "Eğitim sistemi hakkında önerileriniz nelerdir?",
            type = QuestionType.TEXT,
            options = mapOf(),
            order = 4,
            required = false
        )
        questions[question4.id] = question4
        
        return Survey(
            title = "Eğitim Memnuniyeti Anketi",
            description = "Bu anket, eğitim deneyimlerinizi ve tercihlerinizi anlamak için hazırlanmıştır.",
            createdBy = "admin",
            category = "Eğitim",
            questions = questions
        )
    }
} 