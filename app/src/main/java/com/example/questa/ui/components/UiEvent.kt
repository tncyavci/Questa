package com.example.questa.ui.components

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    object NavigateUp : UiEvent()
    data class Navigate(val route: String) : UiEvent()
} 