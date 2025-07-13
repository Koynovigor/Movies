package com.l3on1kl.movies.presentation.main

import com.l3on1kl.movies.domain.model.Movie

sealed interface UiState {
    object Loading : UiState

    data class Success(
        val movies: List<Movie>
    ) : UiState

    data class Error(
        val message: String
    ) : UiState
}
