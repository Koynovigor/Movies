package com.l3on1kl.movies.data.repository

import com.l3on1kl.movies.BuildConfig
import com.l3on1kl.movies.data.local.dao.CategoryDao
import com.l3on1kl.movies.data.local.dao.MovieDao
import com.l3on1kl.movies.data.local.dao.MovieDetailsDao
import com.l3on1kl.movies.data.local.entity.CategoryEntity
import com.l3on1kl.movies.data.mapper.MovieMapper
import com.l3on1kl.movies.data.remote.TmdbApiService
import com.l3on1kl.movies.data.remote.dto.ImagesCfg
import com.l3on1kl.movies.domain.model.Movie
import com.l3on1kl.movies.domain.model.MovieCategory
import com.l3on1kl.movies.domain.model.MovieDetails
import com.l3on1kl.movies.domain.repository.MoviesRepository
import com.l3on1kl.movies.util.NetworkMonitor
import com.l3on1kl.movies.util.TmdbConfigHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class MoviesRepositoryImpl @Inject constructor(
    private val api: TmdbApiService,
    private val dao: MovieDao,
    private val categoryDao: CategoryDao,
    private val detailsDao: MovieDetailsDao,
    private val networkMonitor: NetworkMonitor
) : MoviesRepository {
    private var imagesConfig: ImagesCfg? = null
    private var configTimestamp: Long = 0L

    private suspend fun refreshConfigIfNeeded() {
        val now = System.currentTimeMillis()
        if (imagesConfig == null || now - configTimestamp > 24 * 60 * 60 * 1000) {
            if (networkMonitor.checkConnected()) {
                runCatching {
                    api.getConfiguration(BuildConfig.TMDB_API_KEY)
                }.onSuccess {
                    imagesConfig = it.images
                    configTimestamp = now
                    TmdbConfigHolder.imagesConfig = imagesConfig
                }
            }
        }
    }

    override fun getCategories(): Flow<List<MovieCategory>> = flow {
        if (networkMonitor.checkConnected()) {
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
                            CategoryEntity(
                                it.id,
                                it.title
                            )
                        }
                    )
                    emit(categories)
                }
                .onFailure { error ->
                    if (error is CancellationException)
                        throw error

                    val cached = categoryDao.getAll()
                    if (cached.isNotEmpty()) {
                        emit(
                            cached.map {
                                MovieCategory(
                                    it.id,
                                    it.name
                                )
                            }
                        )
                    } else {
                        throw error
                    }
                }
        } else {
            val cached = categoryDao.getAll()
            if (cached.isNotEmpty()) {
                emit(
                    cached.map {
                        MovieCategory(
                            it.id,
                            it.name
                        )
                    }
                )
            } else {
                throw java.io.IOException("No network and no cache")
            }
        }
    }

    override fun getMovies(
        category: MovieCategory,
        page: Int
    ): Flow<List<Movie>> = flow {
        if (networkMonitor.checkConnected()) {
            try {
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
                emit(result.results.map(MovieMapper::fromDto))
            } catch (e: Exception) {
                if (e is CancellationException)
                    throw e

                val cached = dao.getByCategory(category.id)
                if (cached.isNotEmpty()) {
                    emit(cached.map(MovieMapper::fromEntity))
                } else {
                    throw e
                }
            }
        } else {
            val cached = dao.getByCategory(category.id)
            if (cached.isNotEmpty()) {
                emit(cached.map(MovieMapper::fromEntity))
            } else {
                throw java.io.IOException("No network and no cache")
            }
        }
    }

    override fun getMovieDetails(
        id: Long
    ): Flow<MovieDetails> = flow {
        if (networkMonitor.checkConnected()) {
            try {
                refreshConfigIfNeeded()

                val result = api.getMovieDetails(
                    id,
                    BuildConfig.TMDB_API_KEY
                )
                val movie = MovieMapper.fromDetailsDto(result)
                detailsDao.insert(MovieMapper.toDetailsEntity(movie))
                emit(movie)
            } catch (e: Exception) {
                if (e is CancellationException)
                    throw e

                val cached = detailsDao.getById(id)
                if (cached != null) {
                    emit(
                        MovieMapper.fromDetailsEntity(cached)
                    )
                } else {
                    throw e
                }
            }
        } else {
            val cached = detailsDao.getById(id)
            if (cached != null) {
                emit(
                    MovieMapper.fromDetailsEntity(cached)
                )
            } else {
                throw java.io.IOException("No network and no cache")
            }
        }
    }
}
