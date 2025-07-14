package com.l3on1kl.movies.data.repository

import com.l3on1kl.movies.BuildConfig
import com.l3on1kl.movies.data.local.MovieDao
import com.l3on1kl.movies.data.mapper.MovieMapper
import com.l3on1kl.movies.data.remote.TmdbApiService
import com.l3on1kl.movies.domain.model.Movie
import com.l3on1kl.movies.domain.model.MovieCategory
import com.l3on1kl.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRepositoryImpl @Inject constructor(
    private val api: TmdbApiService,
    private val dao: MovieDao
) : MoviesRepository {
    override fun getMovies(
        category: MovieCategory,
        page: Int
    ): Flow<List<Movie>> = flow {
        val result = api.discoverMovies(
            BuildConfig.TMDB_API_KEY,
            page,
            category.id
        )

        dao.clearCategoryPage(
            category.name,
            page
        )

        dao.insertAll(result.results.map {
            MovieMapper.toEntity(
                MovieMapper.fromDto(it),
                category.name,
                page
            )
        })

        emit(
            result.results.map(
                MovieMapper::fromDto
            )
        )
    }.catch {
        val cached = dao.getByCategory(
            category.name
        )

        if (cached.isNotEmpty()) {
            emit(
                cached.map(
                    MovieMapper::fromEntity
                )
            )
        } else {
            throw it
        }
    }
}
