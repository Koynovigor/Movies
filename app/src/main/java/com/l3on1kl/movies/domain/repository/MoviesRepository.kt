package com.l3on1kl.movies.domain.repository

import com.l3on1kl.movies.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MoviesRepository {
    fun getPopularMovies(): Flow<List<Movie>>
}
