package com.l3on1kl.movies.presentation.details

import com.l3on1kl.movies.domain.model.MovieDetails

sealed interface DetailsUiState {
    object Loading : DetailsUiState

    data class Success(
        val movie: MovieDetails
    ) : DetailsUiState

    data class Error(
        val message: String
    ) : DetailsUiState
}
