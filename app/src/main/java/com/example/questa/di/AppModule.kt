package com.example.questa.di

import com.example.questa.data.repository.AuthRepository
import com.example.questa.data.repository.CommentRepository
import com.example.questa.data.repository.PollRepository
import com.example.questa.data.repository.UserRepository
import com.example.questa.domain.auth.GetAuthUserIdUseCase
import com.example.questa.domain.auth.GetCurrentUserUseCase
import com.example.questa.domain.auth.ResetPasswordUseCase
import com.example.questa.domain.auth.SignInUseCase
import com.example.questa.domain.auth.SignOutUseCase
import com.example.questa.domain.auth.SignUpUseCase
import com.example.questa.domain.comment.AddCommentUseCase
import com.example.questa.domain.comment.DeleteCommentUseCase
import com.example.questa.domain.comment.GetCommentsForPollUseCase
import com.example.questa.domain.comment.GetCommentsUseCase
import com.example.questa.domain.poll.CreatePollUseCase
import com.example.questa.domain.poll.GetAllPollsUseCase
import com.example.questa.domain.poll.GetFeaturedPollsUseCase
import com.example.questa.domain.poll.GetPollByIdUseCase
import com.example.questa.domain.poll.GetUserPollsUseCase
import com.example.questa.domain.poll.LikePollUseCase
import com.example.questa.domain.poll.ToggleFavoriteUseCase
import com.example.questa.domain.poll.VotePollUseCase
import com.example.questa.domain.poll.GetPollStatsUseCase
import com.example.questa.domain.profile.UpdateProfileUseCase
import com.example.questa.domain.poll.GetCategoryPollsUseCase
import com.example.questa.domain.poll.SearchPollsUseCase
import com.example.questa.domain.poll.HasUserVotedUseCase
import com.example.questa.presentation.auth.AuthViewModel
import com.example.questa.presentation.comment.CommentViewModel
import com.example.questa.presentation.poll.PollViewModel
import com.example.questa.presentation.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Firebase instances
    single { FirebaseAuth.getInstance() }
    single { FirebaseDatabase.getInstance().reference }
    
    // Repositories
    single { AuthRepository() }
    single { PollRepository() }
    single { CommentRepository() }
    single { UserRepository() }
    
    // Authentication Use Cases
    factory { SignInUseCase(get()) }
    factory { SignUpUseCase(get()) }
    factory { SignOutUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { ResetPasswordUseCase(get()) }
    factory { GetAuthUserIdUseCase(get()) }
    
    // Poll Use Cases
    factory { GetAllPollsUseCase(get()) }
    factory { GetPollByIdUseCase(get()) }
    factory { CreatePollUseCase(get()) }
    factory { VotePollUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }
    factory { GetFeaturedPollsUseCase(get()) }
    factory { GetUserPollsUseCase(get()) }
    factory { LikePollUseCase(get()) }
    factory { GetPollStatsUseCase(get()) }
    factory { GetCategoryPollsUseCase(get()) }
    factory { SearchPollsUseCase(get()) }
    factory { HasUserVotedUseCase(get()) }
    factory { com.example.questa.domain.poll.GetUserFavoritesUseCase(get()) }
    
    // Comment Use Cases
    factory { GetCommentsUseCase(get()) }
    factory { GetCommentsForPollUseCase(get()) }
    factory { AddCommentUseCase(get()) }
    factory { DeleteCommentUseCase(get()) }
    
    // Profile Use Cases
    factory { UpdateProfileUseCase(get()) }
    
    // ViewModels
    viewModel { AuthViewModel(get(), get(), get(), get(), get()) }
    viewModel { (userId: String) -> PollViewModel(get(), get(), get(), get(), get(), get(), get(), userId, get(), get(), get(), get()) }
    viewModel { (pollId: String) -> CommentViewModel(get(), get(), get(), pollId) }
    viewModel { ProfileViewModel(get()) }
} 