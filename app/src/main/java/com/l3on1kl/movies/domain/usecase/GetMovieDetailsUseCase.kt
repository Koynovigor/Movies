package com.l3on1kl.movies.domain.usecase

import com.l3on1kl.movies.domain.model.MovieDetails
import com.l3on1kl.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMovieDetailsUseCase @Inject constructor(
    private val repo: MoviesRepository
) {
    operator fun invoke(
        id: Long
    ): Flow<MovieDetails> =
        repo.getMovieDetails(id)
}
