package com.example.questa.ui.screens.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.questa.data.model.Comment
import com.example.questa.data.model.Poll
import com.example.questa.data.model.PollOption
import com.example.questa.presentation.auth.AuthViewModel
import com.example.questa.presentation.auth.AuthState
import com.example.questa.presentation.comment.AddCommentState
import com.example.questa.presentation.comment.CommentEvent
import com.example.questa.presentation.comment.CommentState
import com.example.questa.presentation.comment.CommentViewModel
import com.example.questa.presentation.poll.PollDetailState
import com.example.questa.presentation.poll.PollEvent
import com.example.questa.presentation.poll.PollViewModel
import com.example.questa.presentation.poll.VoteState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollDetailScreen(
    pollId: String,
    onBack: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel(),
    pollViewModel: PollViewModel = koinViewModel { 
        parametersOf(authViewModel.authState.value.let { 
            if (it is AuthState.Authenticated) it.user.uid else ""
        })
    },
    commentViewModel: CommentViewModel = koinViewModel {
        parametersOf(pollId)
    }
) {
    val pollDetailState by pollViewModel.pollDetailState.collectAsStateWithLifecycle()
    val voteState by pollViewModel.voteState.collectAsStateWithLifecycle()
    val commentState by commentViewModel.commentState.collectAsStateWithLifecycle()
    val addCommentState by commentViewModel.addCommentState.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var hasVoted by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var showComments by remember { mutableStateOf(false) }
    var activeQuestionIndex by remember { mutableStateOf(0) }
    var showResults by remember { mutableStateOf(false) }
    var anketBittiMi by remember { mutableStateOf(false) }
    
    // Kullanıcı kimliğini al
    val userId = authViewModel.authState.value.let { 
        if (it is AuthState.Authenticated) it.user.uid else ""
    }
    
    // Get user favorites
    val userFavorites by pollViewModel.userFavorites.collectAsStateWithLifecycle()
    
    // Check if poll is favorited
    LaunchedEffect(Unit) {
        pollViewModel.onEvent(PollEvent.LoadUserFavorites)
    }
    
    // Favori durumunu güncelle
    LaunchedEffect(userFavorites, pollId) {
        isFavorite = userFavorites.contains(pollId)
    }
    
    // Load poll data
    LaunchedEffect(pollId, userId) {
        pollViewModel.onEvent(PollEvent.LoadPoll(pollId))
        commentViewModel.onEvent(CommentEvent.LoadComments(pollId))
        
        // Kullanıcının daha önce oy verip vermediğini kontrol et
        if (userId.isNotEmpty()) {
            pollViewModel.onEvent(PollEvent.CheckUserVoted(pollId, userId))
        }
    }
    
    // Handle voting state
    LaunchedEffect(voteState) {
        when (voteState) {
            is VoteState.Success -> {
                hasVoted = true
                pollViewModel.resetVoteState()
                
                // Eğer son soru ise
                val poll = (pollDetailState as? PollDetailState.Success)?.poll
                if (poll != null && activeQuestionIndex == poll.questions.size - 1) {
                    anketBittiMi = true
                } else {
                    // Bir sonraki soruya geç
                    activeQuestionIndex++
                    selectedOption = null // Seçimi sıfırla
                    hasVoted = false // Oy verme durumunu sıfırla
                }
            }
            is VoteState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar((voteState as VoteState.Error).message)
                }
                pollViewModel.resetVoteState()
            }
            is VoteState.AlreadyVoted -> {
                // Kullanıcı zaten oy vermiş
                hasVoted = true
                showResults = true
                pollViewModel.resetVoteState()
            }
            else -> {}
        }
    }
    
    // Handle comment addition state
    LaunchedEffect(addCommentState) {
        when (addCommentState) {
            is AddCommentState.Success -> {
                commentText = ""
                commentViewModel.resetAddCommentState()
            }
            is AddCommentState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar((addCommentState as AddCommentState.Error).message)
                }
                commentViewModel.resetAddCommentState()
            }
            else -> {}
        }
    }
    
    // Set user info for comment section
    LaunchedEffect(authViewModel.authState.value) {
        val authState = authViewModel.authState.value
        if (authState is AuthState.Authenticated) {
            commentViewModel.setUserInfo(
                userId = authState.user.uid,
                username = authState.user.displayName ?: "Anonymous"
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Poll Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isFavorite = !isFavorite
                        pollViewModel.onEvent(PollEvent.ToggleFavorite(pollId))
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val detail = pollDetailState) {
                is PollDetailState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is PollDetailState.Success -> {
                    val poll = detail.poll
                    
                    // Aktif soru indeksini kontrol et ve gerekirse düzelt
                    if (poll.questions.isNotEmpty() && activeQuestionIndex >= poll.questions.size) {
                        activeQuestionIndex = 0
                    }
                    
                    if (showResults) {
                        // Sonuçları Göster
                        PollResultsContent(
                            poll = poll,
                            onFinish = {
                                onBack()
                            }
                        )
                    } else if (anketBittiMi) {
                        // Anket Bittiğinde Gosterilecek Ekran
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Anket tamamlandı!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            
                            Button(
                                onClick = { showResults = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text("Sonuçları Görüntüle")
                            }
                            
                            Button(
                                onClick = onBack,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text("Ana Sayfaya Dön")
                            }
                        }
                    } else {
                        // Normal Anket Ekranı
                        PollQuestionContent(
                            poll = poll,
                            selectedOption = selectedOption,
                            hasVoted = hasVoted,
                            onOptionSelect = { optionId -> selectedOption = optionId },
                            onVote = { 
                                selectedOption?.let { optionId ->
                                    pollViewModel.onEvent(PollEvent.VotePoll(pollId, optionId))
                                }
                            },
                            activeQuestionIndex = activeQuestionIndex,
                            totalQuestions = poll.questions.size
                        )
                    }
                }
                is PollDetailState.Error -> {
                    Text(
                        text = detail.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PollQuestionContent(
    poll: Poll,
    selectedOption: String?,
    hasVoted: Boolean,
    onOptionSelect: (String) -> Unit,
    onVote: () -> Unit,
    activeQuestionIndex: Int,
    totalQuestions: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Anket başlığı
        Text(
            text = "Anket: ${poll.category}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "by ${poll.creatorName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = formatDate(poll.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Soru numarası ve ilerleme
        LinearProgressIndicator(
            progress = { (activeQuestionIndex.toFloat() + 1) / totalQuestions.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        
        Text(
            text = "Soru ${activeQuestionIndex + 1}/$totalQuestions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Aktif soru
        if (poll.questions.isNotEmpty() && activeQuestionIndex < poll.questions.size) {
            val activeQuestion = poll.questions[activeQuestionIndex]
            
            Text(
                text = activeQuestion.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Seçenekler
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(activeQuestion.options) { option ->
                    PollOptionItem(
                        option = option,
                        isSelected = selectedOption == option.id,
                        hasVoted = hasVoted,
                        onSelect = { onOptionSelect(option.id) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            
            // Oy verme butonu
            Button(
                onClick = onVote,
                enabled = selectedOption != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = if (activeQuestionIndex == totalQuestions - 1) "Anketi Tamamla" else "Sonraki Soru"
                )
            }
        }
    }
}

@Composable
fun PollResultsContent(
    poll: Poll,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Başlık
        Text(
            text = "Anket Sonuçları: ${poll.category}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tüm soruların sonuçları
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            poll.questions.forEachIndexed { index, question ->
                item {
                    Text(
                        text = "Soru ${index + 1}: ${question.question}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    PieChart(
                        options = question.options,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(vertical = 8.dp)
                    )
                    
                    question.options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text(
                                text = "${option.votes} oy (${option.percentage.toInt()}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                }
            }
            
            item {
                Text(
                    text = "Toplam oy sayısı: ${poll.totalVotes}",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
        
        // Ana sayfaya dönme butonu
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Ana Sayfaya Dön")
        }
    }
}

@Composable
fun PollOptionItem(
    option: PollOption,
    isSelected: Boolean,
    hasVoted: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected && !hasVoted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            if (hasVoted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "${option.percentage.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { option.percentage / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "${option.votes} votes",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Surface(
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = option.text,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(
    options: List<PollOption>,
    modifier: Modifier = Modifier
) {
    if (options.isEmpty() || options.all { it.votes == 0 }) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No votes yet",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }
    
    val totalVotes = options.sumOf { it.votes }.toFloat()
    
    // Generate colors for chart
    val colors = remember {
        options.map {
            Color(
                red = Random.nextFloat() * 0.8f + 0.2f,
                green = Random.nextFloat() * 0.8f + 0.2f,
                blue = Random.nextFloat() * 0.8f + 0.2f
            )
        }
    }
    
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        Canvas(modifier = Modifier.fillMaxSize().align(Alignment.CenterEnd)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = minOf(canvasWidth, canvasHeight) / 2
            val center = Offset(canvasWidth / 2, canvasHeight / 2)
            
            var startAngle = 0f
            
            options.forEachIndexed { index, option ->
                if (option.votes > 0) {
                    val sweepAngle = (option.votes / totalVotes) * 360f
                    
                    drawArc(
                        color = colors[index],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    
                    startAngle += sweepAngle
                }
            }
        }
        
        // Legend
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart),
            verticalArrangement = Arrangement.Center
        ) {
            options.forEachIndexed { index, option ->
                if (option.votes > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(colors[index], CircleShape)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "${option.text} (${option.percentage.toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.username,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = formatDate(comment.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
} 