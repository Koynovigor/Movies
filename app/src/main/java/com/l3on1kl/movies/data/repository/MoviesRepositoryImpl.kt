package com.l3on1kl.movies.data.repository

import com.l3on1kl.movies.BuildConfig
import com.l3on1kl.movies.data.local.dao.CategoryDao
import com.l3on1kl.movies.data.local.dao.MovieDao
import com.l3on1kl.movies.data.local.entity.CategoryEntity
import com.l3on1kl.movies.data.mapper.MovieMapper
import com.l3on1kl.movies.data.remote.TmdbApiService
import com.l3on1kl.movies.data.remote.dto.ImagesCfg
import com.l3on1kl.movies.domain.model.Movie
import com.l3on1kl.movies.domain.model.MovieCategory
import com.l3on1kl.movies.domain.model.MovieDetails
import com.l3on1kl.movies.domain.repository.MoviesRepository
import com.l3on1kl.movies.util.TmdbConfigHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRepositoryImpl @Inject constructor(
    private val api: TmdbApiService,
    private val dao: MovieDao,
    private val categoryDao: CategoryDao
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

    override fun getCategories(): Flow<List<MovieCategory>> = flow {
        runCatching {
            api.getGenres(BuildConfig.TMDB_API_KEY)
        }
            .onSuccess { result ->
                val categories = result.genres.map {
                    MovieCategory(
                        it.id,
                        it.name.replaceFirstChar { char ->
                            if (char.isLowerCase()) {
                                char.titlecase(Locale.getDefault())
                            } else {
                                char.toString()
                            }
                        }
                    )
                }
                categoryDao.clearAll()
                categoryDao.insertAll(
                    categories.map {
                        CategoryEntity(it.id, it.title)
                    }
                )
                emit(categories)
            }
            .onFailure { error ->
                val cached = categoryDao.getAll()
                if (cached.isNotEmpty()) {
                    emit(cached.map {
                        MovieCategory(it.id, it.name)
                    })
                } else {
                    throw error
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
            category.id,
            page
        )

        dao.insertAll(result.results.map {
            MovieMapper.toEntity(
                MovieMapper.fromDto(it),
                category.id,
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
            category.id
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

    override fun getMovieDetails(
        id: Long
    ): Flow<MovieDetails> = flow {
        refreshConfigIfNeeded()

        val result = api.getMovieDetails(
            id,
            BuildConfig.TMDB_API_KEY
        )

        emit(MovieMapper.fromDetailsDto(result))
    }
}
