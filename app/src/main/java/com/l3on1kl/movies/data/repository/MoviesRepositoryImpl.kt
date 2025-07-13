package com.l3on1kl.movies.data.repository

import com.l3on1kl.movies.BuildConfig
import com.l3on1kl.movies.data.mapper.MovieMapper
import com.l3on1kl.movies.data.remote.TmdbApiService
import com.l3on1kl.movies.domain.model.Movie
import com.l3on1kl.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRepositoryImpl @Inject constructor(
    private val api: TmdbApiService
) : MoviesRepository {
    override fun getPopularMovies(): Flow<List<Movie>> = flow {
        val dto = api.getPopularMovies(
            apiKey = BuildConfig.TMDB_API_KEY
        )

        emit(
            dto.results.map(MovieMapper::fromDto)
        )
    }
}
