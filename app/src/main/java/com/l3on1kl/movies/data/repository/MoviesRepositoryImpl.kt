package com.l3on1kl.movies.data.repository

import com.l3on1kl.movies.BuildConfig
import com.l3on1kl.movies.data.local.MovieDao
import com.l3on1kl.movies.data.mapper.MovieMapper
import com.l3on1kl.movies.data.remote.TmdbApiService
import com.l3on1kl.movies.data.remote.dto.ImagesCfg
import com.l3on1kl.movies.domain.model.Movie
import com.l3on1kl.movies.domain.model.MovieCategory
import com.l3on1kl.movies.domain.repository.MoviesRepository
import com.l3on1kl.movies.util.TmdbConfigHolder
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
    private var imagesConfig: ImagesCfg? = null
    private var configTimestamp: Long = 0L

    private suspend fun refreshConfigIfNeeded() {
        val now = System.currentTimeMillis()
        if (imagesConfig == null || now - configTimestamp > 24 * 60 * 60 * 1000) {
            runCatching {
                api.getConfiguration(BuildConfig.TMDB_API_KEY)
            }.onSuccess {
                imagesConfig = it.images
                configTimestamp = now
                TmdbConfigHolder.imagesConfig = imagesConfig
            }
        }
    }

    override fun getMovies(
        category: MovieCategory,
        page: Int
    ): Flow<List<Movie>> = flow {
        refreshConfigIfNeeded()

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
