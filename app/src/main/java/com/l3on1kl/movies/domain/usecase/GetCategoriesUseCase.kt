package com.l3on1kl.movies.domain.usecase

import com.l3on1kl.movies.domain.model.MovieCategory
import com.l3on1kl.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repo: MoviesRepository
) {
    operator fun invoke(): Flow<List<MovieCategory>> =
        repo.getCategories()
}
