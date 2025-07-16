package com.l3on1kl.movies

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.l3on1kl.movies.domain.model.MovieDetails
import com.l3on1kl.movies.domain.usecase.GetMovieDetailsUseCase
import com.l3on1kl.movies.presentation.details.DetailsUiState
import com.l3on1kl.movies.presentation.details.MovieDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class MovieDetailsViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var useCase: GetMovieDetailsUseCase

    private lateinit var application: Application
    private lateinit var dispatcher: TestDispatcher
    private lateinit var viewModel: MovieDetailsViewModel

    @Before
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        application = Mockito.mock(Application::class.java)

        val movie = MovieDetails(
            id = 1L,
            title = "title",
            overview = "overview",
            voteAverage = 5.0,
            posterPath = null,
            backdropPath = null,
            runtime = 120,
            releaseDate = "",
            tagline = null,
            genres = emptyList(),
            originalTitle = null,
            status = null,
            budget = null,
            revenue = null,
            originalLanguage = null
        )
        `when`(useCase.invoke(1L)).thenReturn(flowOf(movie))

        val handle = SavedStateHandle(mapOf("movieId" to 1L))
        viewModel = MovieDetailsViewModel(
            application,
            handle,
            useCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load emits success state`() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.first()
        assertTrue(state is DetailsUiState.Success)

        state as DetailsUiState.Success
        assertEquals(1L, state.movie.id)
        assertEquals("title", state.movie.title)
    }

    @Test
    fun `use case returns empty movie results in error`() = runTest {
        val emptyMovie = MovieDetails(
            id = 0L,
            title = "",
            overview = "",
            voteAverage = 0.0,
            posterPath = null,
            backdropPath = null,
            runtime = null,
            releaseDate = null,
            tagline = null,
            genres = emptyList(),
            originalTitle = null,
            status = null,
            budget = null,
            revenue = null,
            originalLanguage = null
        )
        `when`(useCase.invoke(1L)).thenReturn(flowOf(emptyMovie))

        val handle = SavedStateHandle(mapOf("movieId" to 1L))
        viewModel = MovieDetailsViewModel(
            application,
            handle,
            useCase
        )

        advanceUntilIdle()

        val state = viewModel.state.first()
        assertTrue(state is DetailsUiState.Error)
    }

    @Test
    fun `missing movieId puts viewmodel in error`() = runTest {
        val handle = SavedStateHandle()
        viewModel = MovieDetailsViewModel(
            application,
            handle,
            useCase
        )

        advanceUntilIdle()

        val state = viewModel.state.first()
        assertTrue(state is DetailsUiState.Error)
    }

    @Test
    fun `use case error moves state to error`() = runTest {
        `when`(useCase.invoke(1L)).thenReturn(flow {
            throw RuntimeException("boom")
        })

        val handle = SavedStateHandle(mapOf("movieId" to 1L))
        viewModel = MovieDetailsViewModel(
            application,
            handle,
            useCase
        )

        advanceUntilIdle()

        val state = viewModel.state.first()
        assertTrue(state is DetailsUiState.Error)
    }
}