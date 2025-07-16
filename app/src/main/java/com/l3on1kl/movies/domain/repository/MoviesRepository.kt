package com.l3on1kl.movies.domain.repository

import com.l3on1kl.movies.domain.model.Movie
import com.l3on1kl.movies.domain.model.MovieCategory
import com.l3on1kl.movies.domain.model.MovieDetails
import kotlinx.coroutines.flow.Flow

interface MoviesRepository {
    fun getCategories(): Flow<List<MovieCategory>>

    fun getMovies(
        category: MovieCategory,
        page: Int
    ): Flow<List<Movie>>

    fun getMovieDetails(id: Long): Flow<MovieDetails>
}
