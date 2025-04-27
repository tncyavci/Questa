package com.example.questa.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.questa.data.model.PollOption
import com.example.questa.data.model.QuestionItem
import com.example.questa.presentation.auth.AuthState
import com.example.questa.presentation.auth.AuthViewModel
import com.example.questa.presentation.poll.PollViewModel
import com.example.questa.presentation.poll.PollState
import com.example.questa.presentation.poll.PollEvent
import com.example.questa.presentation.poll.CreatePollState
import com.example.questa.ui.components.LoadingIndicator
import com.example.questa.ui.navigation.Destination
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePollScreen(
    navController: NavController,
    authViewModel: AuthViewModel = koinViewModel(),
    viewModel: PollViewModel = koinViewModel {
        parametersOf(
            (authViewModel.authState.value as? AuthState.Authenticated)?.user?.uid ?: ""
        )
    }
) {
    val state by viewModel.createPollState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // List of questions with their options
    var questions by remember { mutableStateOf(listOf(QuestionItemState())) }
    var category by remember { mutableStateOf("General") }
    
    LaunchedEffect(state) {
        when (state) {
            is CreatePollState.Success -> {
                snackbarHostState.showSnackbar("Poll created successfully!")
                // Navigate to poll detail if we have a poll ID
                val pollId = (state as CreatePollState.Success).pollId
                navController.navigate(Destination.PollDetail.createRoute(pollId)) {
                    popUpTo(Destination.CreatePoll.route) { inclusive = true }
                }
            }
            is CreatePollState.Error -> {
                val errorMsg = (state as CreatePollState.Error).message
                snackbarHostState.showSnackbar("Error: $errorMsg")
            }
            else -> {}
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create Poll") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state is CreatePollState.Loading) {
            LoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CategorySelector(
                        selectedCategory = category,
                        onCategorySelected = { category = it }
                    )
                }
                
                items(questions.indices.toList()) { index ->
                    QuestionCard(
                        questionState = questions[index],
                        onQuestionChange = { newQuestion ->
                            questions = questions.toMutableList().apply {
                                this[index] = newQuestion
                            }
                        },
                        onDeleteQuestion = {
                            if (questions.size > 1) {
                                questions = questions.toMutableList().apply {
                                    removeAt(index)
                                }
                            }
                        },
                        showDeleteButton = questions.size > 1
                    )
                }
                
                item {
                    Button(
                        onClick = {
                            questions = questions + QuestionItemState()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Question")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Another Question")
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (validateQuestions(questions)) {
                                val questionItems = questions.map { q ->
                                    QuestionItem(
                                        question = q.question,
                                        options = q.options.map { option ->
                                            PollOption(
                                                text = option
                                            )
                                        }
                                    )
                                }
                                viewModel.onEvent(
                                    PollEvent.CreatePoll(
                                        questions = questionItems,
                                        category = category
                                    )
                                )
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please fill all questions and provide at least 2 options for each question")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = validateQuestions(questions)
                    ) {
                        Text("Create Poll")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionCard(
    questionState: QuestionItemState,
    onQuestionChange: (QuestionItemState) -> Unit,
    onDeleteQuestion: () -> Unit,
    showDeleteButton: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Question", 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                if (showDeleteButton) {
                    IconButton(
                        onClick = onDeleteQuestion
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Question",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = questionState.question,
                onValueChange = { 
                    onQuestionChange(questionState.copy(question = it))
                },
                label = { Text("Enter your question") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Options",
                fontWeight = FontWeight.Bold
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                questionState.options.forEachIndexed { index, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = option,
                            onValueChange = { newValue ->
                                val newOptions = questionState.options.toMutableList()
                                newOptions[index] = newValue
                                onQuestionChange(questionState.copy(options = newOptions))
                            },
                            label = { Text("Option ${index + 1}") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(
                            onClick = {
                                if (questionState.options.size > 2) {
                                    val newOptions = questionState.options.toMutableList()
                                    newOptions.removeAt(index)
                                    onQuestionChange(questionState.copy(options = newOptions))
                                }
                            },
                            enabled = questionState.options.size > 2
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Remove Option",
                                tint = if (questionState.options.size > 2) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
            
            Button(
                onClick = {
                    val newOptions = questionState.options.toMutableList()
                    newOptions.add("")
                    onQuestionChange(questionState.copy(options = newOptions))
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Option")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Option")
            }
        }
    }
}

@Composable
fun CategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("General", "Technology", "Sports", "Politics", "Entertainment", "Education", "Science", "Health", "Fashion", "Food", "Travel", "Music", "Movies", "Gaming")
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Category",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    leadingIcon = if (category == selectedCategory) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

// Helper data class to manage the state of each question
data class QuestionItemState(
    val id: String = UUID.randomUUID().toString(),
    val question: String = "",
    val options: List<String> = listOf("", "")
)

// Validation function
fun validateQuestions(questions: List<QuestionItemState>): Boolean {
    // Check if there's at least one question
    if (questions.isEmpty()) return false
    
    // Check if each question has a text and at least 2 non-empty options
    return questions.all { question ->
        question.question.isNotEmpty() && 
        question.options.size >= 2 && 
        question.options.all { it.isNotEmpty() }
    }
} 