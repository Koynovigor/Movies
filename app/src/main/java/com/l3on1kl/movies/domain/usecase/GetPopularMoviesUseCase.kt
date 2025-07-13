package com.l3on1kl.movies.domain.usecase

import com.l3on1kl.movies.domain.repository.MoviesRepository
import javax.inject.Inject

class GetPopularMoviesUseCase @Inject constructor(
    private val repo: MoviesRepository
) {
    operator fun invoke() = repo.getPopularMovies()
}
