package com.l3on1kl.movies

import com.l3on1kl.movies.data.local.dao.CategoryDao
import com.l3on1kl.movies.data.local.dao.MovieDao
import com.l3on1kl.movies.data.local.dao.MovieDetailsDao
import com.l3on1kl.movies.data.local.entity.CategoryEntity
import com.l3on1kl.movies.data.remote.TmdbApiService
import com.l3on1kl.movies.data.remote.dto.GenreDto
import com.l3on1kl.movies.data.remote.dto.GenresDto
import com.l3on1kl.movies.data.repository.MoviesRepositoryImpl
import com.l3on1kl.movies.domain.model.MovieCategory
import com.l3on1kl.movies.util.NetworkMonitor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MoviesRepositoryImplTest {
    @Mock
    lateinit var api: TmdbApiService

    @Mock
    lateinit var movieDao: MovieDao

    @Mock
    lateinit var categoryDao: CategoryDao

    @Mock
    lateinit var detailsDao: MovieDetailsDao

    @Mock
    lateinit var networkMonitor: NetworkMonitor

    private lateinit var repository: MoviesRepositoryImpl

    @Before
    fun setUp() {
        repository = MoviesRepositoryImpl(
            api,
            movieDao,
            categoryDao,
            detailsDao,
            networkMonitor
        )
    }


    @Test
    fun `getCategories returns data from api`() = runTest {
        `when`(networkMonitor.checkConnected()).thenReturn(true)

        val genres = listOf(
            GenreDto(1, "Action"),
            GenreDto(2, "Comedy")
        )

        `when`(
            api.getGenres(
                anyString(),
                anyString()
            )
        ).thenReturn(GenresDto(genres))

        val result = repository.getCategories().first()

        assertEquals(
            listOf(
                MovieCategory(1, "Action"),
                MovieCategory(2, "Comedy")
            ), result
        )

        verify(categoryDao).replaceAll(
            listOf(
                CategoryEntity(1, "Action"),
                CategoryEntity(2, "Comedy")
            )
        )
    }

    @Test
    fun `getCategories offline uses dao`() = runTest {
        `when`(networkMonitor.checkConnected()).thenReturn(false)

        val cached = listOf(
            CategoryEntity(3, "Drama")
        )
        `when`(categoryDao.getAll()).thenReturn(cached)

        val result = repository.getCategories().first()

        assertEquals(listOf(MovieCategory(3, "Drama")), result)

        verify(api, never()).getGenres(
            anyString(),
            anyString()
        )

        verify(categoryDao, never()).replaceAll(anyList())

        verify(categoryDao, times(1)).getAll()
    }

    @Test
    fun `getCategories propagates api exception`() = runTest {
        val exception = RuntimeException("boom")
        `when`(networkMonitor.checkConnected()).thenReturn(true)

        `when`(api.getGenres(anyString(), anyString()))
            .thenThrow(exception)

        `when`(categoryDao.getAll()).thenReturn(emptyList())

        try {
            repository.getCategories().first()
        } catch (e: RuntimeException) {
            assertEquals(exception, e)
        }

        verify(categoryDao).getAll()

        verify(
            categoryDao,
            never()
        ).replaceAll(anyList())
    }

    @Test
    fun `getCategories returns data from dao when api fails`() = runTest {
        `when`(networkMonitor.checkConnected()).thenReturn(true)

        `when`(
            api.getGenres(
                anyString(),
                anyString()
            )
        ).thenThrow(RuntimeException("boom"))

        val cached = listOf(CategoryEntity(5, "Thriller"))

        `when`(categoryDao.getAll()).thenReturn(cached)

        val result = repository.getCategories().first()

        assertEquals(listOf(MovieCategory(5, "Thriller")), result)

        verify(categoryDao, never()).replaceAll(anyList())
    }

    @Test
    fun `getCategories saves empty list`() = runTest {
        `when`(networkMonitor.checkConnected()).thenReturn(true)

        `when`(api.getGenres(anyString(), anyString()))
            .thenReturn(GenresDto(emptyList()))

        val result = repository.getCategories().first()

        assertTrue(result.isEmpty())

        verify(categoryDao).replaceAll(emptyList())
    }
}
