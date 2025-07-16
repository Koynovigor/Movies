package com.l3on1kl.movies.presentation.main

import com.l3on1kl.movies.domain.model.Movie
import com.l3on1kl.movies.domain.model.MovieCategory

sealed interface UiState {
    object Loading : UiState

    data class Success(
        val categories: List<CategoryState>
    ) : UiState

    data class Error(
        val message: String
    ) : UiState
}

data class CategoryState(
    val category: MovieCategory,
    val movies: List<Movie>
)
