package com.l3on1kl.movies.domain.usecase

import com.l3on1kl.movies.domain.model.MovieCategory
import com.l3on1kl.movies.domain.repository.MoviesRepository
import javax.inject.Inject

class GetMoviesUseCase @Inject constructor(
    private val repo: MoviesRepository
) {
    operator fun invoke(
        category: MovieCategory,
        page: Int
    ) = repo.getMovies(
        category,
        page
    )
}
